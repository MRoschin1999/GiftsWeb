package com.nectcracker.studyproject.service;

import com.nectcracker.studyproject.domain.*;
import com.nectcracker.studyproject.repos.NewsRepository;
import com.nectcracker.studyproject.repos.NewsUsersRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NewsService {
    private final NewsRepository newsRepository;
    private final NewsUsersRepository newsUsersRepository;

    public NewsService(NewsRepository newsRepository, NewsUsersRepository newsUsersRepository) {
        this.newsRepository = newsRepository;
        this.newsUsersRepository = newsUsersRepository;
    }

    private void createNew(Chat chat, User user, String text) {
        News newForFriends = new News();
        newForFriends.setText(text);
        Set<User> friends = new HashSet<>(user.getFriends().stream().filter(UserFriends::isAccept).map(UserFriends::getFriend).collect(Collectors.toSet()));
        User wishOwnerUser = chat.getWishForChat().getUser();
        friends.retainAll(wishOwnerUser.getFriends().stream().filter(UserFriends::isAccept).map(UserFriends::getFriend).collect(Collectors.toSet()));
        if (!friends.isEmpty()) {
            newForFriends.addAllUsers(friends);

            newsRepository.save(newForFriends);
            newsUsersRepository.saveAll(newForFriends.getUsers());
        }
    }

    public void creatNewChatCreated(Chat chat, User user) {
        String text = "Ваш друг " + user.getInfo().getFirstName() + " " + user.getInfo().getLastName() +
                " начал сбор средств на " + chat.getWishForChat().getWishName() + " для другого вашего друга " + chat.getWishForChat().getUser().getInfo().getFirstName() +
                " " + chat.getWishForChat().getUser().getInfo().getLastName();
        createNew(chat, user, text);
    }

    public void createNewMoneyCollected(Chat chat, User user) {
        String text = "Средства на " + chat.getWishForChat().getWishName() + " для другого вашего друга " + chat.getWishForChat().getUser().getInfo().getFirstName() +
                " " + chat.getWishForChat().getUser().getInfo().getLastName() + " были успешно собраны";
        createNew(chat, user, text);
    }

    public void createNewChatIsClosed(Chat chat, User user) {
        String text = "Пришел дедлайн и " +  user.getInfo().getFirstName() + " " + user.getInfo().getLastName() + " решил, не продолжать сбор средств на" +
                chat.getWishForChat().getWishName() + " для другого вашего друга " + chat.getWishForChat().getUser().getInfo().getFirstName() +
                " " + chat.getWishForChat().getUser().getInfo().getLastName();
        createNew(chat, user, text);
    }


    public Map<String, Set<News>> findByUser(User user) {
        Map<String, Set<News>> resultMap = new HashMap<>();
        Set<News> newNews = new HashSet<>();
        Set<News> oldNews = new HashSet<>();

        Set<NewsUsers> nu = newsUsersRepository.findAllByUsers(user);

        for (NewsUsers iterator : nu) {
            if (iterator.isSaw()) {
                oldNews.add(iterator.getNews());
            } else {
                newNews.add(iterator.getNews());
                iterator.setSaw(true);
                newsUsersRepository.save(iterator);
            }
        }

        resultMap.put("oldNews", oldNews);
        resultMap.put("newNews", newNews);

        return resultMap;
    }

    public int sizeOfNewsByUser(User user) {
        return newsUsersRepository.countAllByUsers(user);

    }

}
