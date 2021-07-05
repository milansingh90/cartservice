package com.demo.spring.demo.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
public class Cart {
    @Id
    @GeneratedValue
    private Long id;
    private String code;
    private BigDecimal total;
    private BigDecimal totalTax;
    private BigDecimal subtotal;
    private BigDecimal cartDiscount;
    private BigDecimal itemDiscount;
    private BigDecimal totalSavings;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "cart")
    @JsonManagedReference
    private List<Item> itemList;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "userinfo_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserInfo userInfo;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getCartDiscount() {
        return cartDiscount;
    }

    public void setCartDiscount(BigDecimal cartDiscount) {
        this.cartDiscount = cartDiscount;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public BigDecimal getItemDiscount() {
        return itemDiscount;
    }

    public void setItemDiscount(BigDecimal itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    public BigDecimal getTotalSavings() {
        return totalSavings;
    }

    public void setTotalSavings(BigDecimal totalSavings) {
        this.totalSavings = totalSavings;
    }
}
