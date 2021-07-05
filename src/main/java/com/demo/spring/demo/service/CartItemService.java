package com.demo.spring.demo.service;

import com.demo.spring.demo.beans.Cart;
import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.dto.ItemRequestDTO;
import com.demo.spring.demo.exception.ItemNotFoundException;
import com.demo.spring.demo.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    @Value("${cart.item.discount}")
    private BigDecimal itemDiscount;

    @Autowired
    public CartItemService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public Boolean isItemAlreadyInCart(ItemRequestDTO itemRequestDTO) {
        return cartItemRepository.findItemByItemCode(itemRequestDTO.getCode()).isPresent();
    }

    public Optional<Item> updateItemInCart(ItemRequestDTO itemRequestDTO) throws ItemNotFoundException {
        Optional<Item> item = cartItemRepository.findItemByItemCode(itemRequestDTO.getCode());
        if(!item.isPresent()) {
            throw new ItemNotFoundException("Item not found");
        }
        updateItemDetails(item.get(), itemRequestDTO);
        saveCartItem(item.get());
        return item;
    }

    public Item addItemInCart(ItemRequestDTO itemRequestDTO) {
        Cart cart = itemRequestDTO.getCart();
        Item item = new Item();
        item.setCart(cart);
        item.setItemCode(itemRequestDTO.getCode());
        updateItemDetails(item, itemRequestDTO);
        saveCartItem(item);
        return item;
    }

    public Item saveCartItem(Item item) {
        return cartItemRepository.save(item);
    }

    private Item updateItemDetails(Item item, ItemRequestDTO itemRequestDTO) {
        BigDecimal total = itemRequestDTO.getPrice().multiply(BigDecimal.valueOf(itemRequestDTO.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = total.multiply(itemDiscount).setScale(2, RoundingMode.HALF_UP);;
        item.setQuantity(itemRequestDTO.getQuantity());
        item.setPricePerQuantity(itemRequestDTO.getPrice());
        item.setTotal(total.subtract(discountAmount));
        item.setDiscount(discountAmount);
        return item;
    }
}
