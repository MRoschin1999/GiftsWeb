package com.nectcracker.studyproject.controller;

import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserWishes;
import com.nectcracker.studyproject.service.UserService;
import com.nectcracker.studyproject.service.UserWishesService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class WishController {
    private final UserWishesService userWishesService;
    private final UserService userService;

    public WishController(UserWishesService userWishesService, UserService userService) {
        this.userWishesService = userWishesService;
        this.userService = userService;
    }

    @GetMapping("/new_wish")
    public String wish(Map<String, Object> model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) userService.loadUserByUsername(auth.getName());
        Iterable<UserWishes> list = userWishesService.getUserWishes(user);
        model.put("messages", list);
        return "new_wish";
    }

    @PostMapping("/new_wish")
    public String addWish(@RequestParam String text,@RequestParam String eventsId, @RequestParam String imgURL, @RequestParam String shopURL, Map<String, Object> model) {
        userWishesService.addWish(text, eventsId, imgURL, shopURL);
        return "redirect:/cabinet";
    }


    @PostMapping("/new_wish_from_friend")
    public String addWishFromFriend(@RequestParam String name, @RequestParam String text, @RequestParam String eventsId, @RequestParam String imgURL, @RequestParam String shopURL) {
        userWishesService.addWishFromFriend(name, text, eventsId, imgURL, shopURL);
        return "redirect:/friend_page/" + name;
    }

    @PostMapping("/deleteWish/{id}")
    public String deleteUserWish(@PathVariable String id) {
        userWishesService.deleteUserWish(Long.parseLong(id));
        return "redirect:/cabinet";
    }
}
