package com.demo.spring.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Item already present in Cart. Cannot be re-added. Please use update endpoint")
public class CartItemException extends Exception {
    public CartItemException(String message) {
        super(message);
    }
}
