package com.nectcracker.studyproject.controller;

import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserWishes;
import com.nectcracker.studyproject.repos.UserRepository;
import com.nectcracker.studyproject.service.UserService;
import com.nectcracker.studyproject.service.UserWishesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-for-interests-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD )
@Sql(value = {"/delete-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails("a")
public class FriendsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserWishesService userWishesService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;


    @Test
    public void correctLastName() throws Exception {
        User user = userWishesService.findByAuthentication();
        User friend = userService.getFriends(user).get("registered").iterator().next();
        this.mockMvc.perform(get("/friend_page/" + friend.getUsername()))
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(model().attribute("info", is(friend.getInfo()))) ;
    }

    @Test
    public void correctWishOnFriendPage() throws Exception{
        userWishesService.addWishFromFriend("a", "wishFromFriend", "", "");
        userWishesService.addWish("myOwnWish", "" ,"");
        Iterable<UserWishes> wishes = userWishesService.getUserWishes(userRepository.findByUsername("a"));
        List<UserWishes> result = new ArrayList<>();
        wishes.forEach(result::add);
        this.mockMvc.perform(get("/friend_page/a"))
                .andDo(print())
                .andExpect(model().attribute("friendWishes", contains(result.get(0))))
                .andExpect(model().attribute("userWishes", contains(result.get(1))))
                .andExpect(status().isOk());
    }

}