package com.demo.spring.demo.beans;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Item {
    @Id
    @GeneratedValue
    private Long id;
    private String itemCode;
    private Long quantity;
    private String description;
    private BigDecimal pricePerQuantity;
    private BigDecimal discount;
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = true)
    @JsonBackReference
    private Cart cart;

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getPricePerQuantity() {
        return pricePerQuantity;
    }

    public void setPricePerQuantity(BigDecimal pricePerQuantity) {
        this.pricePerQuantity = pricePerQuantity;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
