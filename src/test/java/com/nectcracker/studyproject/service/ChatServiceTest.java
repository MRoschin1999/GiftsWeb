package com.nectcracker.studyproject.service;

import com.nectcracker.studyproject.domain.Chat;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.repos.ChatRepository;
import com.nectcracker.studyproject.repos.UserRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-for-chat-test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD )
@Sql(value = {"/delete-after-chat.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@WithUserDetails("b")
public class ChatServiceTest {
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserRepository userRepository;
    @Test
    @Ignore
    @WithUserDetails("a")
    public void testIfNewChatWasCreated() throws Exception {
        chatService.createNewChat(2L, "BBB", "11-11-1111","11-11-1111", "111");
        User user = userRepository.findByUsername("a");
        Chat chat = chatRepository.findByOwner(user);
        assertThat(user.getChatsOwner().size(), is(1));
    }

    @Test
    public void testIfItIsAbleToGetChatById() {
        assertThat(chatService.getById(1L).getDescription(), containsString("AAA"));
    }


}
