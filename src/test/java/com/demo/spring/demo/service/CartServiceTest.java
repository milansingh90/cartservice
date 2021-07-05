package com.demo.spring.demo.service;

import com.demo.spring.demo.beans.Address;
import com.demo.spring.demo.beans.Cart;
import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.beans.UserAuth;
import com.demo.spring.demo.beans.UserInfo;
import com.demo.spring.demo.dto.ItemRequestDTO;
import com.demo.spring.demo.exception.CartItemException;
import com.demo.spring.demo.exception.ItemNotFoundException;
import com.demo.spring.demo.repository.CartItemRepository;
import com.demo.spring.demo.repository.CartRepository;
import com.demo.spring.demo.repository.UserInfoRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RunWith( SpringRunner.class )
@TestPropertySource(properties="cart.discount=0.02,cart.tax.amount=0.18")
public class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    CartItemService cartItemService;

    @Mock
    UserInfoRepository userInfoRepository;

    @InjectMocks
    CartService cartService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(cartService, "appliedCartDiscount", new BigDecimal(0.02));
        ReflectionTestUtils.setField(cartService, "appliedTax", new BigDecimal(0.18));
    }

    @Test
    public void itemIsAddedToCartSucessfully() throws CartItemException {

        Mockito.when(cartItemService.addItemInCart(Mockito.any())).thenReturn(getItem(null));
        Mockito.when(cartRepository.save(Mockito.any())).thenReturn(getCartWithItems());

        Optional<Cart> cart = cartService.addItemToCart(getItemRequest(getCartWithoutItems()));

        Assert.assertEquals(getCartWithItems().getTotal(), cart.get().getTotal());
        Assert.assertEquals(getCartWithItems().getItemList().size(), cart.get().getItemList().size());
        Assert.assertEquals(getCartWithItems().getItemDiscount(), cart.get().getItemDiscount());
        Assert.assertEquals(getCartWithItems().getTotalTax(), cart.get().getTotalTax());

    }

    @Test
    public void itemAlreadyPresentInCartThrowsException() throws CartItemException {

        Mockito.when(cartItemService.isItemAlreadyInCart(Mockito.any())).thenReturn(true);

        exceptionRule.expect(CartItemException.class);
        exceptionRule.expectMessage("Item Already Present");
        cartService.addItemToCart(getItemRequest(getCartWithItems()));
    }

    @Test
    public void itemisUpdatedInCartSuccessfully() throws ItemNotFoundException {

        Mockito.when(cartItemService.updateItemInCart(Mockito.any())).thenReturn(Optional.of(getItem(getCartWithItems())));
        Optional<Item> item = cartService.updateItemInCart(getItemRequest(getCartWithItems()));
        Item expectedItem = getItem(getCartWithItems());
        Assert.assertEquals(expectedItem.getTotal(), item.get().getTotal());
        Assert.assertEquals(expectedItem.getQuantity(), item.get().getQuantity());
    }



    private ItemRequestDTO getItemRequest(Cart cart) {
        ItemRequestDTO itemRequestDTO = new ItemRequestDTO();
        itemRequestDTO.setCode("126");
        itemRequestDTO.setPrice(new BigDecimal(100));
        itemRequestDTO.setQuantity(5L);
        itemRequestDTO.setCart(cart);
        return itemRequestDTO;
    }

    private Cart getCartWithoutItems() {
       Cart cart = new Cart();
       List list = new ArrayList();
       cart.setItemList(list);
       cart.setUserInfo(getUserDetails());
       cart.setId(1L);
       cart.setCode("Cart-177a1111-2889-4317-8654-f488b57a7b75");
       return cart;
    }

    private Cart getCartWithItems() {
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

    private Item getItem(Cart cart) {

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

    private UserInfo getUserDetails() {

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

    private Address getAddress() {
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
