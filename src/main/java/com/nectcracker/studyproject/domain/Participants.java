package com.nectcracker.studyproject.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

@Data
@Slf4j
@Entity
public class Participants {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Chat.class)
    @JoinColumn(name = "chatForParticipants")
    private Chat chat;

    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "participantsForChat")
    private User userForChat;

    private double sumFromUser;
    private double notConfirmedSumFromUser;

    public Participants(User userForChat, Chat chat) {
        this.userForChat = userForChat;
        this.chat = chat;
        this.sumFromUser = 0.0;
    }

    public void updateSumFromUser(double sum) {
        sumFromUser += sum;
    }
    public void downgradeSumFromUser(double sum) {
        sumFromUser -= sum;
    }

    public void updateNotConfirmedSumFromUser(double sum) {
        notConfirmedSumFromUser += sum;
    }

    public Participants() {

    }

    @Override
    public String toString() {
        return "Participants{" +
                "id=" + id +
                '}';
    }
}
