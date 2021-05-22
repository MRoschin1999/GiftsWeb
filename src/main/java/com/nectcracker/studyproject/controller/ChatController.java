package com.nectcracker.studyproject.controller;

import com.nectcracker.studyproject.domain.Chat;
import com.nectcracker.studyproject.domain.Participants;
import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserWishes;
import com.nectcracker.studyproject.repos.ParticipantsRepository;
import com.nectcracker.studyproject.service.ChatService;
import com.nectcracker.studyproject.service.MessagesService;
import com.nectcracker.studyproject.service.UserService;
import com.nectcracker.studyproject.service.UserWishesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.Map;
import java.util.Set;

@Slf4j
@Controller
public class ChatController {
    private final UserWishesService userWishesService;
    private final ChatService chatService;
    private final MessagesService messagesService;
    private final ParticipantsRepository participantsRepository;
    private final UserService userService;

    public ChatController(UserWishesService userWishesService,
                          ChatService chatService, MessagesService messagesService,
                          ParticipantsRepository participantsRepository, UserService userService) {
        this.userWishesService = userWishesService;
        this.chatService = chatService;
        this.messagesService = messagesService;
        this.participantsRepository = participantsRepository;
        this.userService = userService;
    }

    @PostMapping("/join_chat/{id}")
    public ModelAndView chatStrategy(@PathVariable Long id) {
        UserWishes wishForChat = userWishesService.getById(id);
        if (chatService.findByWishForChat(wishForChat).isPresent()) {
            ModelAndView modelAndView = new ModelAndView("redirect:/chat");
            modelAndView.addObject("wishId", id);
            return modelAndView;
        } else {
            ModelAndView modelAndView = new ModelAndView("redirect:/new_chat");
            modelAndView.addObject("wishId", id);
            return modelAndView;
        }
    }

    @GetMapping("/chat")
    public String showChat(@RequestParam Long wishId, Map<String, Object> model) {
        UserWishes currentWish = userWishesService.getById(wishId);
        User wishUser = currentWish.getUser();
        model.put("username", wishUser.getInfo().getFirstName());
        model.put("userId", wishUser.getId());
        model.put("wishInfo", currentWish.getWishName());
        model.put("wishId", wishId);
        chatService.checkUser(currentWish);
        User currentUser = userWishesService.findByAuthentication();
        Chat currentChat = chatService.getById(wishId);
        model.put("chat", currentChat);
        Iterable<Participants> participants = participantsRepository.findByChat(currentChat);
        if (currentUser.getId().equals(currentChat.getOwner().getId())) {
            model.put("ownerPage", "true");
        } else {
            model.put("ownerPage", "false");
        }
        model.put("participants", participants);
        model.put("deadlinePassed", chatService.checkDeadlinePassed(currentChat));
        model.put("currentPrice", currentChat.sumCurrentPrice());
        model.put("isMoneyCollected", chatService.isMoneyCollected(wishId));
        return "chat";
    }

    @GetMapping("/new_chat")
    public ModelAndView showNewChat(@RequestParam Long wishId, Map<String, Object> model) {
        UserWishes currentWish = userWishesService.getById(wishId);
        User currentUser = currentWish.getUser();
        model.put("username", currentUser.getInfo().getFirstName());
        model.put("wishInfo", currentWish.getWishName());
        ModelAndView modelAndView = new ModelAndView("new_chat");
        modelAndView.addObject("wishId", wishId);
        return modelAndView;
    }

    @PostMapping("/new_chat/{id}")
    public ModelAndView createNewChat(@PathVariable Long id, @RequestParam String description,
                                      @RequestParam String deadline, @RequestParam String sum) {
        ModelAndView modelAndView;
        if (!chatService.createNewChat(id, description, deadline, sum)) {
            modelAndView = new ModelAndView("redirect:/new_chat");
            modelAndView.addObject("errorInfo", "Please fill again");
        } else {
            modelAndView = new ModelAndView("redirect:/chat");
        }
        modelAndView.addObject("wishId", id);
        return modelAndView;
    }

    @PostMapping("/donate/{id}")
    public ModelAndView donateMoneyForWish(@PathVariable Long id, @RequestParam String sum,  @RequestParam Long participantId) {
        Chat chat = chatService.getById(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) userService.loadUserByUsername(auth.getName());

        if(user.equals(chat.getOwner()))
            chatService.donateMoneyForWish(id, sum, participantsRepository.findById(participantId).get());
        ModelAndView modelAndView = new ModelAndView("redirect:/chat");
        modelAndView.addObject("wishId", id);
        return modelAndView;
    }

    @GetMapping("/chat_list")
    public String showAllChats(Map<String, Object> model) {
        User currentUser = userWishesService.findByAuthentication();
        Set<Participants> participants = participantsRepository.findByUserForChat(currentUser);
        model.put("chats", chatService.getChatParticipants(participants));
        return "chat_list";
    }

    @PostMapping("/leave/{id}")
    public String leaveChat(@PathVariable Long id) {
        chatService.leaveChat(id);
        return "redirect:/chat_list";
    }

    @PostMapping("/closeChatBecauseDeadline/{id}")
    public String closeChatBecauseDeadline(@PathVariable Long id){
        chatService.closeChatBecauseDeadline(id);
        return "redirect:/chat_list";
    }

    @PostMapping("/updateDeadline/{id}")
    public ModelAndView updateDeadline(@PathVariable Long id, @RequestParam String deadline) throws ParseException {
        if(chatService.updateDeadline(deadline, id))
            return new ModelAndView("redirect:/chat_list");
        else {
            ModelAndView modelAndView = new ModelAndView("redirect:/chat");
            modelAndView.addObject("wishId", id);
            return modelAndView;
        }
    }


    @PostMapping("/sendMessage/{wishId}")
    public ModelAndView sendMessageToDB(@PathVariable Long wishId, @RequestParam String sentMessage) {
        messagesService.receiveMessage(sentMessage, wishId);
        ModelAndView modelAndView = new ModelAndView("redirect:/chat");
        modelAndView.addObject("wishId", wishId);
        return modelAndView;
    }

    @PostMapping(value = "/showMessages", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> showMessages(@RequestParam Long chatId, @RequestParam Long userId) {
        return messagesService.findMessagesForChat(chatId);
    }

    @PostMapping("/moneyCollected/{wishId}")
    public ModelAndView closeChatAfterMoneyCollected(@PathVariable Long wishId) {
        chatService.closeAfterMoneyCollected(wishId);
        ModelAndView modelAndView = new ModelAndView("redirect:/cabinet");
        modelAndView.addObject("wishId", wishId);
        return modelAndView;
    }
}

