package com.nectcracker.studyproject.service;

import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.repos.UserFriendsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserFriendsService {
    final
    UserFriendsRepository userFriendsRepository;

    public UserFriendsService(UserFriendsRepository userFriendsRepository) {
        this.userFriendsRepository = userFriendsRepository;
    }

    public boolean isAccept(User user, User friend){
        if(userFriendsRepository.findByUserAndFriend(user, friend) == null)
            return false;
        return userFriendsRepository.findByUserAndFriend(user, friend).isAccept();
    }
}
