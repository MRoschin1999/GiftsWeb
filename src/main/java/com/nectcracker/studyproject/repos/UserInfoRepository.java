package com.nectcracker.studyproject.repos;

import com.nectcracker.studyproject.domain.User;
import com.nectcracker.studyproject.domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface UserInfoRepository extends JpaRepository<UserInfo,Long> {
    UserInfo findByUser(User user);
    @Query("select u from UserInfo u where u.firstName = ?1")
    Set<UserInfo> findAllByFirstName(String firstname);
    @Query("select u from UserInfo u where u.lastName = ?1")
    Set<UserInfo> findAllByLastName(String lastname);
}
