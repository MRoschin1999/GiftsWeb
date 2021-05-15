package com.nectcracker.studyproject.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Entity
@Table
@Slf4j
@Getter
@Setter
public class UserFriends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "friend_id")
    private User friend;

    private boolean accept;

    public UserFriends(){}
    public UserFriends(User user, User friend, boolean acceptRequest) {
        this.user = user;
        this.friend = friend;
        this.accept = acceptRequest;
    }

}
