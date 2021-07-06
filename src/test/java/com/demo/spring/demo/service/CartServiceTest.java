package com.demo.spring.demo.service;

import com.demo.spring.demo.beans.Cart;
import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.exception.CartItemException;
import com.demo.spring.demo.exception.ItemNotFoundException;
import com.demo.spring.demo.repository.CartItemRepository;
import com.demo.spring.demo.repository.CartRepository;
import com.demo.spring.demo.repository.UserInfoRepository;
import com.demo.spring.demo.util.TestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.security.Principal;

import java.util.Optional;

@RunWith( SpringRunner.class )
public class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    CartItemService cartItemService;

    @Mock
    UserInfoRepository userInfoRepository;

    @Mock
    Principal principal;

    @InjectMocks
    CartService cartService;

    TestData testData;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(cartService, "appliedCartDiscount", new BigDecimal(0.02));
        ReflectionTestUtils.setField(cartService, "appliedTax", new BigDecimal(0.18));
        testData = new TestData();
    }

    @Test
    public void itemIsAddedToCartSucessfully() throws CartItemException {

        Mockito.when(cartItemService.addItemInCart(Mockito.any())).thenReturn(testData.getItem(null));
        Mockito.when(cartRepository.save(Mockito.any())).thenReturn(testData.getCartWithItems());

        Optional<Cart> cart = cartService.addItemToCart(testData.getItemRequest(testData.getCartWithoutItems()));

        Assert.assertEquals(testData.getCartWithItems().getTotal(), cart.get().getTotal());
        Assert.assertEquals(testData.getCartWithItems().getItemList().size(), cart.get().getItemList().size());
        Assert.assertEquals(testData.getCartWithItems().getItemDiscount(), cart.get().getItemDiscount());
        Assert.assertEquals(testData.getCartWithItems().getTotalTax(), cart.get().getTotalTax());

    }

    @Test
    public void itemAlreadyPresentInCartThrowsException() throws CartItemException {

        Mockito.when(cartItemService.isItemAlreadyInCart(Mockito.any())).thenReturn(true);

        exceptionRule.expect(CartItemException.class);
        exceptionRule.expectMessage("Item Already Present");
        cartService.addItemToCart(testData.getItemRequest(testData.getCartWithItems()));
    }

    @Test
    public void itemisUpdatedInCartSuccessfully() throws ItemNotFoundException {

        Mockito.when(cartItemService.updateItemInCart(Mockito.any()))
                .thenReturn(Optional.of(testData.getItem(testData.getCartWithItems())));
        Optional<Item> item = cartService.updateItemInCart(testData.getItemRequest(testData.getCartWithItems()));
        Item expectedItem = testData.getItem(testData.getCartWithItems());
        Assert.assertEquals(expectedItem.getTotal(), item.get().getTotal());
        Assert.assertEquals(expectedItem.getQuantity(), item.get().getQuantity());
    }

    @Test
    public void createAndSaveCartSuccessfully() {

        Mockito.when(userInfoRepository.findByUserAuth_Username(Mockito.any()))
                .thenReturn(Optional.of(testData.getUserDetails()));
        Mockito.when(cartRepository.save(Mockito.any()))
                .thenReturn(testData.getCartWithItems());

        Cart createdCart = cartService.createAndSaveCart(principal);

        Assert.assertEquals(testData.getCartWithItems().getUserInfo().getUserAuth().getUsername(),
                createdCart.getUserInfo().getUserAuth().getUsername());


    }

}
