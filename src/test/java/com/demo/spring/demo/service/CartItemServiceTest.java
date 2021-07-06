package com.demo.spring.demo.service;

import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.exception.ItemNotFoundException;
import com.demo.spring.demo.repository.CartItemRepository;
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
import java.util.Optional;

@RunWith( SpringRunner.class )
public class CartItemServiceTest {

    @Mock
    CartItemRepository cartItemRepository;

    @InjectMocks
    CartItemService cartItemService;

    TestData testData;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(cartItemService, "itemDiscount", new BigDecimal(0.10));
        testData = new TestData();
    }

    @Test
    public void itemIsAddedInCartSuccessfully() {

        Mockito.when(cartItemRepository.save(Mockito.any())).thenReturn(testData.getItem(null));
        Item item = cartItemService.addItemInCart(testData.getItemRequest(testData.getCartWithoutItems()));
        Item expectedValue = testData.getItem(null);

        Assert.assertEquals(expectedValue.getQuantity(), item.getQuantity());
        Assert.assertEquals(expectedValue.getItemCode(), item.getItemCode());
    }

    @Test
    public void itemIsUpdatedInCartSuccessfully() throws ItemNotFoundException {

        Mockito.when(cartItemRepository.findItemByItemCode(Mockito.any()))
                .thenReturn(Optional.of(testData.getItem(null)));
        Mockito.when(cartItemRepository.save(Mockito.any())).thenReturn(testData.getItem(null));
        Optional<Item> item = cartItemService.updateItemInCart(testData.getItemRequest(testData.getCartWithItems()));

        Item expectedValue = testData.getItem(null);

        Assert.assertEquals(expectedValue.getQuantity(), item.get().getQuantity());
        Assert.assertEquals(expectedValue.getItemCode(), item.get().getItemCode());

    }

    @Test
    public void shouldThrowExceptionIfItemNotFoundForUpdate() throws ItemNotFoundException {

        expectedException.expect(ItemNotFoundException.class);
        expectedException.expectMessage("Item not found");

        cartItemService.updateItemInCart(testData.getItemRequest(testData.getCartWithoutItems()));


    }
}
