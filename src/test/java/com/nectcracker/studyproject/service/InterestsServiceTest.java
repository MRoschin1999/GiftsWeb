package com.nectcracker.studyproject.service;

import com.nectcracker.studyproject.controller.UserPageController;
import com.nectcracker.studyproject.domain.Interests;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.repos.UserRepository;
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

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-for-interests-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD )
@Sql(value = {"/delete-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails("a")
public class InterestsServiceTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InterestsService interestsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserPageController userPageController;

    @Test
    public void testIfUserCanUpdateInterests() throws Exception {
        interestsService.updateUserInterests("beer");
        this.mockMvc.perform(get("/cabinet"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("beer")));
    }

    @Test
    public void testIfWeGetAllUserInterests() throws Exception {
        interestsService.updateUserInterests("beer");
        interestsService.updateUserInterests("jogging");
        Set<Interests> list = interestsService.getUserInterests();
        assertThat(list.size(), is(2));
    }

    @Test
    public void testThatInterestAreDelayedAfterDeletion() throws Exception {
        interestsService.updateUserInterests("beer");
        interestsService.updateUserInterests("jogging");
        interestsService.deleteInterest("beer");
        Set<Interests> list = interestsService.getUserInterests();
        assertThat(list.size(), is(1));
        assertThat(list.toString(), containsString("jogging"));
    }

    @Test
    public void testThatWeCanAddInterestOnlyOneTime() throws Exception {
        interestsService.updateUserInterests("beer");
        interestsService.updateUserInterests("beer");
        Set<Interests> list = interestsService.getUserInterests();
        assertThat(list.size(), is(1));
    }

    @Test
    public void testIfWeCanGetUsersInterests() throws Exception {
        User user = userRepository.findByUsername("b");
        assertThat(interestsService.getSmbInterests(user).size(), is(0));
//        assertThat(interestsService.getSmbInterests(user).toString(), containsString("Чай"));
    }
}
