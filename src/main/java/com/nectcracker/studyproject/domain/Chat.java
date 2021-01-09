package com.nectcracker.studyproject.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @MapsId
    private UserWishes wishForChat;

    private String description;

    private Date deadline;

    private Double presentPrice;

    private Double currentPrice;

    private boolean collected;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = Participants.class, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Participants> chatForParticipants = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER ,targetEntity = Messages.class, cascade = CascadeType.ALL)
    private Set<Messages> messages = new HashSet<>();

    public Chat() {
    }

    public Chat(String description, Date deadline, Double presentPrice) {
        this.description = description;
        this.deadline = deadline;
        this.presentPrice = presentPrice;
        this.currentPrice = 0.0;
        this.collected = false;
    }


    public String getWishName(){
        return wishForChat.getWishName();
    }

    public Long getWishId(){
        return wishForChat.getId();
    }

    public int getNumberOfParticipants(){
        return chatForParticipants.size();
    }

    public void updateCurrentPrice(Double sum) {
        currentPrice += sum;
    }

    public double sumCurrentPrice() {
        double curPrice = 0.0;
        for (Participants p : chatForParticipants) {
            curPrice += p.getSumFromUser();
        }
        return curPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(id, chat.id) &&
                Objects.equals(wishForChat, chat.wishForChat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, wishForChat);
    }

    @Override
    public String toString() {
        return "Chat{" +
                ", wishForChat=" + wishForChat +
                ", description='" + description + '\'' +
                ", deadline=" + deadline +
                ", presentPrice=" + presentPrice +
                '}';
    }
}
