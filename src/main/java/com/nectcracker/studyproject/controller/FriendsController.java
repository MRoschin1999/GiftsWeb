package com.nectcracker.studyproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.nectcracker.studyproject.domain.*;
import com.nectcracker.studyproject.repos.UserInfoRepository;
import com.nectcracker.studyproject.service.EventsService;
import com.nectcracker.studyproject.service.InterestsService;
import com.nectcracker.studyproject.service.UserService;
import com.nectcracker.studyproject.service.UserWishesService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RequestMapping()
@Controller
public class FriendsController {

    private final UserService userService;
    private final InterestsService interestsService;
    private final UserWishesService userWishesService;
    private final UserInfoRepository userInfoRepository;
    private final EventsService eventsService;
    private final ObjectMapper objectMapper;

    private CacheLoader<User, Map> loader = new CacheLoader<User, Map>() {
        @Override
        public Map load(User user) throws Exception {
            return userService.getFriends(user);
        }
    };
    private LoadingCache<User, Map> cache = CacheBuilder.newBuilder().refreshAfterWrite(30, TimeUnit.MINUTES).build(loader);


    public FriendsController(UserService userService, InterestsService interestsService,
                             UserWishesService userWishesService, UserInfoRepository userInfoRepository, EventsService eventsService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.interestsService = interestsService;
        this.userWishesService = userWishesService;
        this.userInfoRepository = userInfoRepository;
        this.eventsService = eventsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/friends")
    public String friends(Model model) throws ExecutionException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) userService.loadUserByUsername(auth.getName());

        Map<String, Set> friendsMapForForm = cache.get(user);

        model.addAttribute("registeredFriends", friendsMapForForm.get("registered"));
        return "friends";
    }

    @GetMapping("/find_friends")
    public String findFriends(Map<String, Object> model) throws ExecutionException {
        model.put("founded", false);
        return "find_friends";
    }

    @PostMapping("/find_friends")
    public String findFriend(@ModelAttribute("friendInfo") UserRegistrationRequest userRegistrationRequest, Map<String, Object> model) throws IOException, GeneralSecurityException, ParseException {

        Set<User> usersSet = userService.findFriend(userRegistrationRequest);
        model.put("founded", true);
        model.put("foundedUsers", usersSet);

        return "find_friends";
    }

    @RequestMapping("/friend_page/{name}")
    public String friendPage(@PathVariable String name, Map<String, Object> model) throws IOException, ParseException {
        model.put("name", name);

        User friend = (User) userService.loadUserByUsername(name);
        UserInfo currentUserInfo = userInfoRepository.findByUser(friend);
        model.put("info", currentUserInfo);

        List<Object> friendEventsData = new ArrayList<>();
        Set<Events> friendEvents = eventsService.getUserEvents(friend);
        model.put("friendsActualEvents", eventsService.actualEvents(friendEvents));
        for (Events event : friendEvents) {
            String eventStr = eventsService.toString(event);
            friendEventsData.add(objectMapper.readTree(eventStr));
        }
        model.put("eventsData", friendEventsData);

        Iterable<Interests> list = interestsService.getSmbInterests(friend);
        model.put("interests", list);

        Set<UserWishes> friendWishes = new HashSet<>();
        Set<UserWishes> userWishes = new HashSet<>();
        Iterable<UserWishes> wishes = userWishesService.getUserWishes(friend);
        for (UserWishes wish : wishes) {
            if (wish.isFriendCreateWish())
                friendWishes.add(wish);
            else
                userWishes.add(wish);
        }
        model.put("friendWishes", friendWishes);
        model.put("userWishes", userWishes);

        return "friend_page";
    }
}
