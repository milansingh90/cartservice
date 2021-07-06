package com.demo.spring.demo.controller;

import com.demo.spring.demo.exception.CartItemException;
import com.demo.spring.demo.exception.CartNotFoundException;
import com.demo.spring.demo.beans.Cart;
import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.dto.ItemRequestDTO;
import com.demo.spring.demo.exception.ItemNotFoundException;
import com.demo.spring.demo.exception.UserNotAllowedException;
import com.demo.spring.demo.service.CartItemService;
import com.demo.spring.demo.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class CartController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    private CartService cartService;

    private CartItemService cartItemService;

    @Autowired
    public CartController(CartService cartService, CartItemService cartItemService) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
    }

    @GetMapping("/api/carts/{code}")
    public Optional<Cart> findCartById(
            @PathVariable String code,
            Principal principal) throws CartNotFoundException, UserNotAllowedException {

        Optional<Cart> cart = cartService.getCartByCode(code);
        if(!cart.isPresent()){
            throw new CartNotFoundException("Cart for the given code not found");
        }
        checkIfUserAllowedToViewOrModifyCart(principal, cart.get());
        return cart;
    }

    @GetMapping("/api/carts")
    public List<Cart> findAllCarts(Principal principal) {
        List<Cart> carts =  cartService.getCarts();
        return carts.stream()
                .filter(cart -> principal.getName() == cart.getUserInfo().getUserAuth().getUsername())
                .collect(Collectors.toList());
    }

    @PostMapping("/api/carts/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Optional<Cart> createNewCart(Principal principal) {
        Cart cart = cartService.createAndSaveCart(principal);
        return Optional.of(cart);
    }

    @PostMapping("/api/carts/{code}/entries")
    public Optional<Cart> addItemToCart(
            @PathVariable String code,
            @RequestBody ItemRequestDTO itemRequestDTO,
            Principal principal)
        throws CartNotFoundException, CartItemException, UserNotAllowedException {
        getAndPrepareCart(code, itemRequestDTO, principal);
        return cartService.addItemToCart(itemRequestDTO);

    }

    @PutMapping("/api/carts/{code}/entries")
    public Optional<Item> updateItem(
            @PathVariable String code,
            @RequestBody ItemRequestDTO itemRequestDTO,
            Principal principal)
        throws CartNotFoundException, ItemNotFoundException, UserNotAllowedException {
        getAndPrepareCart(code, itemRequestDTO, principal);
        return cartService.updateItemInCart(itemRequestDTO);
    }


    private void getAndPrepareCart(
            @PathVariable String code,
            @RequestBody ItemRequestDTO itemRequestDTO,
            Principal principal) throws CartNotFoundException, UserNotAllowedException {
        Optional<Cart> cart = cartService.getCartByCode(code);
        if (!cart.isPresent()) {
            throw new CartNotFoundException("Cart Not Found by the given cart code");
        }
        checkIfUserAllowedToViewOrModifyCart(principal, cart.get());
        itemRequestDTO.setCart(cart.get());
    }

    private void checkIfUserAllowedToViewOrModifyCart(
            Principal principal,
            Cart cart) throws UserNotAllowedException{
        if( principal.getName() != cart.getUserInfo().getUserAuth().getUsername()){
            throw new UserNotAllowedException("UserAuth cannot view/modify cart. " +
                    "The cart does not belong to the user.");
        }
    }

}

