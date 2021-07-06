package com.demo.spring.demo.service;

import com.demo.spring.demo.beans.UserAuth;
import com.demo.spring.demo.beans.UserInfo;
import com.demo.spring.demo.dto.UserAuthRequest;
import com.demo.spring.demo.dto.UserDTO;
import com.demo.spring.demo.repository.UserAuthRepository;
import com.demo.spring.demo.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserDetailsManageService implements UserDetailsService {

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private UserInfoRepository userInfoRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserAuth> user = userAuthRepository.findByUsername(username);
        if (user.isPresent()) {
            return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(),
                    new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

    }

    public UserAuth save(UserAuthRequest user) {
        UserAuth encodedUserAuth = new UserAuth();
        encodedUserAuth.setUsername(user.getUsername());
        encodedUserAuth.setPassword(bcryptEncoder.encode(user.getPassword()));
        return userAuthRepository.save(encodedUserAuth);
    }

    public UserInfo saveUser(UserDTO userDetails, Principal principal) {
        UserInfo userInfo = new UserInfo();
        Optional<UserAuth> userAuth = userAuthRepository.findByUsername(principal.getName());
        userInfo.setFirstName(userDetails.getFirstName());
        userInfo.setLastName(userDetails.getLastName());
        userInfo.setBillingAddress(userDetails.getBillingAddress());
        userInfo.setEmailId(userDetails.getEmail());
        userInfo.setShippingAddress(userDetails.getShippingAddress());
        userInfo.setUserAuth(userAuth.get());
        return userInfoRepository.save(userInfo);
    }
}
