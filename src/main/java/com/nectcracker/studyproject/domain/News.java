package com.nectcracker.studyproject.domain;

import lombok.Data;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "news")
@Data
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @OneToMany(mappedBy = "news")
    private Set<NewsUsers> users = new HashSet<>();

    public News(){}

    public void addUser(User user){
        NewsUsers newsUsers = new NewsUsers(this, user);
        users.add(newsUsers);
        user.getNews().add(newsUsers);
    }

    public void addAllUsers(Set<User> setUsers){
        NewsUsers newsUsers;
        for(User user : setUsers){
            newsUsers = new NewsUsers(this, user);
            users.add(newsUsers);
            user.getNews().add(newsUsers);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        News news = (News) o;
        return Objects.equals(id, news.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString(){
        return "id: " + id;
    }

}
