package com.demo.spring.demo.repository;

import com.demo.spring.demo.beans.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {


    Optional<UserInfo> findByUserAuth_Username(String username);

}
