package com.nectcracker.studyproject.service;

import com.nectcracker.studyproject.domain.Chat;
import com.nectcracker.studyproject.domain.Participants;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserWishes;
import com.nectcracker.studyproject.repos.ChatRepository;
import com.nectcracker.studyproject.repos.ParticipantsRepository;
import com.nectcracker.studyproject.repos.UserRepository;
import com.nectcracker.studyproject.repos.UserWishesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class ChatService {
    private final UserWishesService userWishesService;
    private final UserWishesRepository userWishesRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final NewsService newsService;
    private final ParticipantsRepository participantsRepository;

    public ChatService(UserWishesService userWishesService,
                       UserWishesRepository userWishesRepository,
                       ChatRepository chatRepository,
                       NewsService newsService,
                       UserRepository userRepository,
                       ParticipantsRepository participantsRepository) {
        this.userWishesService = userWishesService;
        this.userWishesRepository = userWishesRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.newsService = newsService;
        this.participantsRepository = participantsRepository;
    }

    public boolean createNewChat(Long id, String description, String deadline, String sum) {
        try {
            UserWishes wishForChat = userWishesService.getById(id);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            if (!deadline.equals("") && !sum.equals("") && sum.matches("([\\d]*[.]?[\\d]+)") && new Date().before(formatter.parse(deadline))) {
                Chat tmp = new Chat(description, formatter.parse(deadline), Double.parseDouble(sum));
                tmp.setWishForChat(wishForChat);
                User user = userWishesService.findByAuthentication();
                if (chatRepository.findByWishForChat(wishForChat) == null) {
                    tmp.setOwner(user);
                }

                chatRepository.save(tmp);
                userRepository.save(user);
                Participants participants = new Participants(user, tmp);
                participantsRepository.save(participants);
                tmp.getChatForParticipants().add(participants);
                user.getParticipantsForChat().add(participants);
                chatRepository.save(tmp);
                userRepository.save(user);

                newsService.creatNewChatCreated(tmp, user);

                wishForChat.setChatForWish(tmp);
                userWishesRepository.save(wishForChat);

                return true;
            } else return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Set<User> getChatParticipants(Long wishId) {
        UserWishes userWishes = userWishesService.getById(wishId);
        Chat currentChat = chatRepository.findByWishForChat(userWishes);
        Set<Participants> participantsForCurrentChat = participantsRepository.findByChat(currentChat);
        Set<User> users = new HashSet<>();
        for (Participants i : participantsForCurrentChat) {
            users.add(i.getUserForChat());
        }
        return users;
    }

    public Optional<Chat> findByWishForChat(UserWishes wishForChat) {
        return chatRepository.findById(wishForChat.getId());
    }

    public Chat findByOwner(User user) {
        return chatRepository.findByOwner(user);
    }

    public boolean checkDeadlinePassed(Chat chat){
        return new Date().after(chat.getDeadline());
    }

    public void checkUser(UserWishes wish) {
        User currentUser = userWishesService.findByAuthentication();
        Chat currentChat = chatRepository.findByWishForChat(wish);
        if (participantsRepository.findByUserForChatAndChat(currentUser, currentChat) == null) {
            Participants participants = new Participants(currentUser, currentChat);
            currentChat.getChatForParticipants().add(participants);
            currentUser.getParticipantsForChat().add(participants);
            participantsRepository.save(participants);
            chatRepository.save(currentChat);
            userRepository.save(currentUser);
        }
    }

    public void donateMoneyForWish(Long wishId, String sum) {
        if(sum.isEmpty() || !sum.matches("([\\d]*[.]?[\\d]+)")) {
            return;
        }
        UserWishes wish = userWishesService.getById(wishId);
        Chat currentChat = chatRepository.findByWishForChat(wish);
        User currentUser = userWishesService.findByAuthentication();
        Participants currentParticipant = participantsRepository.findByUserForChatAndChat(currentUser, currentChat);
        currentParticipant.updateSumFromUser(Double.parseDouble(sum));
        participantsRepository.save(currentParticipant);
        chatRepository.save(currentChat);
    }

    public Chat getById(Long wishId) {
        UserWishes wish = userWishesService.getById(wishId);
        return chatRepository.findByWishForChat(wish);
    }

    public Set<Chat> getChatParticipants(Set<Participants> participants) {
        Set<Chat> chatSet = new HashSet<>();
        for (Participants i : participants) {
            chatSet.add(i.getChat());
        }
        return chatSet;
    }

    public void leaveChat(Long wishId) {
        UserWishes wish = userWishesService.getById(wishId);
        User currentUser = userWishesService.findByAuthentication();
        Chat currentChat = chatRepository.findByWishForChat(wish);
        Participants participants = participantsRepository.findByUserForChatAndChat(currentUser, currentChat);
        currentChat.getChatForParticipants().remove(participants);
        currentUser.getParticipantsForChat().remove(participants);
        participantsRepository.save(participants);
        chatRepository.save(currentChat);
        userRepository.save(currentUser);
    }

    public void closeChatBecauseDeadline(Long id){
        UserWishes userWishes = userWishesRepository.getOne(id);
        Chat chat = chatRepository.findByWishForChat(userWishes);
        newsService.createNewChatIsClosed(chat, chat.getOwner());
        userWishes.setChatForWish(null);
        chatRepository.deleteById(id);
    }

    public boolean updateDeadline(String deadline, Long id) throws ParseException {
        if (!deadline.equals("")) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Chat chat = getById(id);
            Date deadlineDate = formatter.parse(deadline);
            if(deadlineDate.before(new Date()))
                return false;
            chat.setDeadline(deadlineDate);
            chatRepository.save(chat);
            return true;
        }
        return false;
    }

    public void closeAfterMoneyCollected(Long wishId) {
        UserWishes userWishes = userWishesRepository.getOne(wishId);
        Chat chat = chatRepository.findByWishForChat(userWishes);
        chat.setCollected(true);
        Chat chatAfterSave = chatRepository.save(chat);
        newsService.createNewMoneyCollected(chatAfterSave, chatAfterSave.getOwner());
        userWishes.setChatForWish(null);
        userWishes.setClosed(true);
        userWishesRepository.save(userWishes);
    }

    public Boolean isMoneyCollected(Long wishId) {
        UserWishes wish = userWishesService.getById(wishId);
        Chat currentChat = chatRepository.findByWishForChat(wish);
        return wish.getCurrentSum() > currentChat.getPresentPrice();
    }
}
