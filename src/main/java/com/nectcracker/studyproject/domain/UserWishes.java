package com.nectcracker.studyproject.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table
@Slf4j
@Data
public class UserWishes implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    private Events eventForWish;

    private String wishName;
    private boolean friendCreateWish;
    private String imgURL;
    private boolean isClosed;
    private boolean deleted;

    @OneToOne(mappedBy = "wishForChat", cascade = CascadeType.ALL)
    private Chat chatForWish;

    public UserWishes(User user, String wish, String imgURL) {
        this.user = user;
        this.wishName = wish;
        this.imgURL = imgURL;
        this.isClosed = false;
        this.deleted = false;
    }

    public UserWishes() {
    }

    public int getNumberOfChatParticipants() {
        if (chatForWish != null) {
            return chatForWish.getChatForParticipants().size();
        } else {
            return 0;
        }
    }

    public double getCurrentSum() {
        if (chatForWish != null) {
            return chatForWish.sumCurrentPrice();
        } else {
            return 0;
        }
    }

    public double getPrice() {
        if (chatForWish != null) {
            return chatForWish.getPresentPrice();
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWishes that = (UserWishes) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Wish " + wishName + " for user " + user.getInfo().getFirstName();
    }
}
