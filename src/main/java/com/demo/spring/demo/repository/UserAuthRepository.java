package com.demo.spring.demo.repository;

import com.demo.spring.demo.beans.UserAuth;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends CrudRepository<UserAuth, Long> {

    Optional<UserAuth> findByUsername(String username);
}
