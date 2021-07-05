package com.demo.spring.demo.service;

import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.beans.UserAuth;
import com.demo.spring.demo.beans.UserInfo;
import com.demo.spring.demo.exception.CartItemException;
import com.demo.spring.demo.beans.Cart;
import com.demo.spring.demo.dto.ItemRequestDTO;
import com.demo.spring.demo.exception.ItemNotFoundException;
import com.demo.spring.demo.repository.CartItemRepository;
import com.demo.spring.demo.repository.CartRepository;
import com.demo.spring.demo.repository.UserAuthRepository;
import com.demo.spring.demo.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    @Value("${cart.discount}")
    private BigDecimal appliedCartDiscount;

    @Value("${cart.tax.amount}")
    private BigDecimal appliedTax;


    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final CartItemService cartItemService;

    private final UserInfoRepository userInfoRepository;

    @Autowired
    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       CartItemService cartItemService,
                       UserInfoRepository userInfoRepository) {
       this.cartRepository = cartRepository;
       this.cartItemRepository = cartItemRepository;
       this.cartItemService = cartItemService;
       this.userInfoRepository = userInfoRepository;
    }

    public List<Cart> getCarts() {
        return cartRepository.findAll();
    }

    public Optional<Cart> getCartById(Long id) {
        return cartRepository.findById(id);
    }

    public Optional<Cart> getCartByCode(String code) {
        return cartRepository.findCartByCode(code);
    }

    public Optional<Item> updateItemInCart(ItemRequestDTO itemRequestDTO) throws ItemNotFoundException {
        Optional<Item> item =  cartItemService.updateItemInCart(itemRequestDTO);
        recalculateCart(item.get().getCart(), item.get().getCart().getItemList());
        saveCart(item.get().getCart());
        return item;
    }

    public Optional<Cart> addItemToCart(ItemRequestDTO itemRequestDTO) throws CartItemException {
        if(cartItemService.isItemAlreadyInCart(itemRequestDTO)) {
            throw new CartItemException("Item Already Present");
        }
        Cart cart = itemRequestDTO.getCart();
        List<Item> items = cart.getItemList();
        items.add(cartItemService.addItemInCart(itemRequestDTO));
        cart.setItemList(items);
        recalculateCart(cart, items);
        saveCart(cart);
        return Optional.of(cart);
    }

    public void recalculateCart(Cart cart, List<Item> items) {
        BigDecimal totalWithoutDiscount = items.stream()
                .map(e-> e.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal cartDiscount = totalWithoutDiscount.multiply(appliedCartDiscount)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal itemDiscount = items.stream().map(i -> i.getDiscount()).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = totalWithoutDiscount.subtract(cartDiscount)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = total.multiply(appliedTax).setScale(2, RoundingMode.HALF_UP);

        cart.setCartDiscount(cartDiscount);
        cart.setItemDiscount(itemDiscount);
        cart.setTotalSavings(cartDiscount.add(itemDiscount));
        cart.setTotalTax(tax);
        cart.setSubtotal(total);
        cart.setTotal(total.add(tax));

    }

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public Cart createAndSaveCart(Principal principal) {
        Cart cart = new Cart();
        Optional<UserInfo> user = userInfoRepository.findByUserAuth_Username(principal.getName());
        cart.setCode("Cart-"+ UUID.randomUUID().toString());
        cart.setUserInfo(user.get());
        return cartRepository.save(cart);
    }


}
