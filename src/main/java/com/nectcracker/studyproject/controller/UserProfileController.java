package com.nectcracker.studyproject.controller;

import com.nectcracker.studyproject.domain.Interests;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.service.InterestsService;
import com.nectcracker.studyproject.service.UserInfoService;
import com.nectcracker.studyproject.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class UserProfileController {
    private final UserInfoService userInfoService;
    private final InterestsService interestsService;
    private final UserService userService;

    public UserProfileController(UserInfoService userInfoService, InterestsService interestsService, UserService userService) {
        this.userInfoService = userInfoService;
        this.interestsService = interestsService;
        this.userService = userService;
    }


    @GetMapping("/redactor")
    public String redactor(Map<String, Object> model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) userService.loadUserByUsername(auth.getName());
        model.put("fromVk", user.getVkId() != null);
        Iterable<Interests> list = interestsService.getUserInterests();
        model.put("interests", list);
        List<Interests> iList = interestsService.getAllInterests();
        String[] str = new String[iList.size()];
        for (int i = 0; i < iList.size(); i++) {
            str[i] = iList.get(i).getInterestName();
        }
        model.put("data", str);
        return "redactor";
    }

    @PostMapping("/redactor")
    public String updateInfo(@RequestParam String name, @RequestParam String surname, @RequestParam String oldPassword,
                             @RequestParam String newPassword,
                             Map<String, Object> model) {
        if (userInfoService.updateUserInfo(name, surname, oldPassword, newPassword)) {
            model.put("result", "Success!");
        } else {
            model.put("result", "Something wrong");
        }
        return "redactor";
    }

    @GetMapping("/delete/{name}")
    public String deleteInterest(@PathVariable String name) {
        interestsService.deleteInterest(name);
        return "redirect:/redactor";
    }
}
