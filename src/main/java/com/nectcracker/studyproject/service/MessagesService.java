package com.nectcracker.studyproject.service;

import com.nectcracker.studyproject.domain.Chat;
import com.nectcracker.studyproject.domain.Messages;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserWishes;
import com.nectcracker.studyproject.repos.ChatRepository;
import com.nectcracker.studyproject.repos.MessagesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessagesService {
    private final MessagesRepository messagesRepository;
    private final ChatRepository chatRepository;
    private final UserWishesService userWishesService;

    public MessagesService(MessagesRepository messagesRepository, ChatRepository chatRepository, UserWishesService userWishesService) {
        this.messagesRepository = messagesRepository;
        this.chatRepository = chatRepository;
        this.userWishesService = userWishesService;
    }

    public Map<String, String> findMessagesForChat(Long chatId) {
        UserWishes currentWish = userWishesService.getById(chatId);
        Chat currentChat = chatRepository.findByWishForChat(currentWish);
        List<Messages> messages = messagesRepository.findByChat(currentChat);
        Map<String, String> result = new HashMap<>();
        for (Messages m : messages) {
            result.put(m.getId().toString(), m.getAuthor().getInfo().getFirstName() + " " + m.getText());
        }
        currentChat.getMessages().addAll(messages);
        chatRepository.save(currentChat);
        return result;
    }

    public void receiveMessage(String sentMessage, Long wishId) {
        User currentUser = userWishesService.findByAuthentication();
        Chat currentChat = chatRepository.findByWishForChat(userWishesService.getById(wishId));
        Messages message = new Messages(currentChat, currentUser, ": " + sentMessage);
        messagesRepository.save(message);
    }
}
