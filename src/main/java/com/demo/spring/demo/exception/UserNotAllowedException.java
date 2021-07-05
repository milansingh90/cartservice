package com.demo.spring.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "UserAuth does has not access to view/modify cart")
public class UserNotAllowedException extends Exception {
    public UserNotAllowedException(String message) {
        super(message);
    }
}
