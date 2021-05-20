package com.nectcracker.studyproject.repos;

import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserFriends;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface UserFriendsRepository extends JpaRepository<UserFriends, Long> {
    Set<UserFriends> findAllByUser(User user);
    Set<UserFriends> findAllByFriend(User user);
    UserFriends findByUserAndFriend(User user, User friend);
}
