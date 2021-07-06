package com.demo.spring.demo.util;

import com.demo.spring.demo.beans.Address;
import com.demo.spring.demo.beans.Cart;
import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.beans.UserAuth;
import com.demo.spring.demo.beans.UserInfo;
import com.demo.spring.demo.dto.ItemRequestDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TestData {

    public  ItemRequestDTO getItemRequest(Cart cart) {
        ItemRequestDTO itemRequestDTO = new ItemRequestDTO();
        itemRequestDTO.setCode("126");
        itemRequestDTO.setPrice(new BigDecimal(100));
        itemRequestDTO.setQuantity(5L);
        itemRequestDTO.setCart(cart);
        return itemRequestDTO;
    }

    public Cart getCartWithoutItems() {
        Cart cart = new Cart();
        List list = new ArrayList();
        cart.setItemList(list);
        cart.setUserInfo(getUserDetails());
        cart.setId(1L);
        cart.setCode("Cart-177a1111-2889-4317-8654-f488b57a7b75");
        return cart;
    }

    public Cart getCartWithItems() {
        Cart cart = getCartWithoutItems();
        List list = new ArrayList();
        list.add(getItem(null));
        cart.setItemList(list);
        cart.setTotal(new BigDecimal(520.38).setScale(2, RoundingMode.HALF_UP));
        cart.setItemDiscount(new BigDecimal(50).setScale(2, RoundingMode.HALF_UP));
        cart.setCartDiscount(new BigDecimal(9).setScale(2, RoundingMode.HALF_UP));
        cart.setSubtotal(new BigDecimal(441).setScale(2, RoundingMode.HALF_UP));
        cart.setTotalSavings(new BigDecimal(59).setScale(2, RoundingMode.HALF_UP));
        cart.setTotalTax(new BigDecimal(79.38).setScale(2, RoundingMode.HALF_UP));
        return cart;
    }

    public Item getItem(Cart cart) {

        Item item = new Item();
        item.setId(4L);
        item.setItemCode("126");
        item.setQuantity(5L);
        item.setDescription("Product1");
        item.setPricePerQuantity(new BigDecimal(100));
        item.setDiscount(new BigDecimal(50));
        item.setTotal(new BigDecimal(450));
        if(cart != null){
            item.setCart(cart);
        }
        return item;
    }

    public UserInfo getUserDetails() {

        UserInfo user= new UserInfo();
        UserAuth userAuth = new UserAuth();
        userAuth.setUsername("user1");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmailId("john.doe@test.com");
        user.setShippingAddress(getAddress());
        user.setUserAuth(userAuth);
        return user;
    }

    public Address getAddress() {
        Address address = new Address();
        address.setId(1L);
        address.setCountry("India");
        address.setName("Home");
        address.setPostalCode("70463");
        address.setStreetName("Lane1");
        address.setStreetNumber("25");
        return address;
    }
}
