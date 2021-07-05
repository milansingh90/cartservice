package com.demo.spring.demo.repository;

import com.demo.spring.demo.beans.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {


    public Optional<UserInfo> findByUserAuth_Username(String username);

}
