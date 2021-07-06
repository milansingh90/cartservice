package com.demo.spring.demo.dto;

import com.demo.spring.demo.beans.Cart;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class ItemRequestDTO {
    private String code;
    private Long quantity;
    private BigDecimal price;
    @JsonIgnore
    private Cart cart;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
