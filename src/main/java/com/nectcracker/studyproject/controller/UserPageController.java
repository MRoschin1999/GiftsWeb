package com.nectcracker.studyproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.nectcracker.studyproject.domain.*;
import com.nectcracker.studyproject.repos.UserInfoRepository;
import com.nectcracker.studyproject.repos.UserRepository;
import com.nectcracker.studyproject.service.EventsService;
import com.nectcracker.studyproject.service.InterestsService;
import com.nectcracker.studyproject.service.UserService;
import com.nectcracker.studyproject.service.NewsService;
import com.nectcracker.studyproject.service.UserWishesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class UserPageController {
    private final UserWishesService userWishesService;
    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;
    private final InterestsService interestsService;
    private final NewsService newsService;
    private final EventsService eventsService;
    private final UserService userService;



    private CacheLoader<User, Map> loader = new CacheLoader<User, Map>() {
        @Override
        public Map load(User user) throws Exception {
            return userService.takeFriendFromVk(user);
        }
    };
    private LoadingCache<User, Map> cache = CacheBuilder.newBuilder().refreshAfterWrite(30, TimeUnit.MINUTES).build(loader);;

    public UserPageController(UserWishesService userWishesService, UserInfoRepository userInfoRepository,
                              UserRepository userRepository, InterestsService interestsService,
                              NewsService newsService,
                              EventsService eventsService, UserService userService) {
        this.userWishesService = userWishesService;
        this.userInfoRepository = userInfoRepository;
        this.userRepository = userRepository;
        this.interestsService = interestsService;
        this.newsService = newsService;
        this.eventsService = eventsService;
        this.userService = userService;
    }

    @GetMapping("/cabinet")
    public String cabinet(Map<String, Object> model) throws ExecutionException, IOException, ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        UserInfo currentUserInfo = userInfoRepository.findByUser(user);
        model.put("info", currentUserInfo);
        Iterable<UserWishes> list = userWishesService.getUserWishes(user);
        model.put("wishes", list);
        Iterable<Interests> interests = interestsService.getUserInterests();
        model.put("interests", interests);
        model.put("newsNumber", newsService.findByUser(user).get("newNews").size());

        List<Object> friendsEventsData = new ArrayList<>();
        List<Object> currentUserData = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        Set<Events> currentUserEvents = eventsService.getUserEvents(user);
        model.put("currentUserActualEvents", eventsService.actualEvents(currentUserEvents));
        for (Events event : currentUserEvents) {
            String eventStr = eventsService.toString(event);
            currentUserData.add(objectMapper.readTree(eventStr));
        }

        Set<User> friends = user.getFriends();
        for (User friend : friends) {
            Set<Events> friendEvents = eventsService.getUserEvents(friend);
            for (Events event : friendEvents) {
                String eventStr = eventsService.toString(event);
                friendsEventsData.add(objectMapper.readTree(eventStr));
            }
        }


        model.put("eventsData", currentUserData);
        model.put("friendsEventsData", friendsEventsData);
        return "cabinet";
    }

    @PostMapping("/setInterest")
    public void setInterestByVk(){
    //TODO send call to microservice
    }
}
