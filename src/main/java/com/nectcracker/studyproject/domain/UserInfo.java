package com.nectcracker.studyproject.domain;


import lombok.Builder;
import lombok.Data;


import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.*;

@Entity
@Table
@Data
public class UserInfo implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Temporal(TemporalType.DATE)
    private Date birthday;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(toBuilder = true)
    public UserInfo(String firstName, String lastName, Date birthday, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.user = user;
    }

    public UserInfo(){
    }

    @Override
    public String toString(){
        return "id: " + id + ";firstName: " + firstName + ";lastName: " + lastName + ";birthday: " + birthday + ";user id: " + user.getId();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return id.equals(userInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
