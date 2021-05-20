package com.nectcracker.studyproject.service;


import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserWishes;
import com.nectcracker.studyproject.repos.UserRepository;
import com.nectcracker.studyproject.repos.UserWishesRepository;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-for-wishes-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/delete-wishes-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails("a")
public class UserWishesServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserWishesService userWishesService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserWishesRepository userWishesRepository;

    @Test
    public void testIfAllWishesAreShown() throws Exception {
        this.mockMvc.perform(get("/cabinet"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("chocolate")))
                .andExpect(content().string(containsString("phone")))
                .andExpect(content().string(not(containsString("guitar"))));
    }

    @Test
    public void testIfWeCanGetAllUserWishes() throws Exception {
        User user = userRepository.findByUsername("a");
        Iterable<UserWishes> iterable = userWishesService.getUserWishes(user);
        List<UserWishes> result = new ArrayList<UserWishes>();
        iterable.forEach(result::add);
        assertThat(result.size(), is(3));
    }

    @Test
    public void testIfWeCanAddUserWish() throws Exception {
        userWishesService.addWish("compas", "", "", "");
        User user = userRepository.findByUsername("a");
        List<UserWishes> result = userWishesRepository.findByUserId(user.getId());
        assertThat(result.size(), is(4));
        this.mockMvc.perform(get("/cabinet"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("chocolate")))
                .andExpect(content().string(containsString("phone")))
                .andExpect(content().string(containsString("compas")));
    }

    @Test
    public void testIfWeCanAddUserWishFromFriend() throws Exception {
        userWishesService.addWishFromFriend("b", "compas", "", "", "");
        User userA = userRepository.findByUsername("a");
        List<UserWishes> result = userWishesRepository.findByUserId(userA.getId());
        this.mockMvc.perform(get("/cabinet"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("compas"))));
    }
}
