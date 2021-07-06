package com.demo.spring.demo;

import com.demo.spring.demo.beans.Address;
import com.demo.spring.demo.dto.ItemRequestDTO;
import com.demo.spring.demo.dto.UserAuthRequest;
import com.demo.spring.demo.dto.UserDTO;
import com.demo.spring.demo.exception.CartItemException;
import com.demo.spring.demo.util.TestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest()
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CartIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    TestData testData;

    @Before
    public void setUp() {
        testData = new TestData();
    }

    @Test
    public void cartIntegrationTest() throws Exception {


        //Register an user
        mockMvc.perform(
                MockMvcRequestBuilders.post("/register")
                .contentType("application/json")
                .content(asJsonString(getRegisterRequestInput())))
                .andExpect( MockMvcResultMatchers.status().isOk());

        //Authenticate the user to get the token
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.post("/authenticate")
                .contentType("application/json")
                .content(asJsonString(getRegisterRequestInput())))
                .andExpect( MockMvcResultMatchers.status().isOk())
                .andReturn();

        String tokenResponseAsString = result.getResponse().getContentAsString();
        String token = (String) convertJSONStringToObject(tokenResponseAsString).get("token");


        //Add the user details
        mockMvc.perform(
                MockMvcRequestBuilders.post("/user/details")
                        .contentType("application/json")
                        .content(asJsonString(userDetailsInputRequest()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                .andExpect( MockMvcResultMatchers.status().isOk());

        //Create the cart for the user
        MvcResult createCartResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/carts/create")
                        .contentType("application/json")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                .andExpect( MockMvcResultMatchers.status().isCreated())
                .andReturn();

        String cartCreateResponseAsString = createCartResult.getResponse().getContentAsString();
        String cartCode = (String) convertJSONStringToObject(cartCreateResponseAsString).get("code");

        //Add items to the cart of the user
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/carts/" +cartCode + "/entries")
                        .contentType("application/json")
                        .content(asJsonString(testData.getItemRequest(null)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                .andExpect( MockMvcResultMatchers.status().isOk())
                .andExpect( MockMvcResultMatchers.jsonPath("$.code", Matchers.is(cartCode)))
                .andExpect( MockMvcResultMatchers.jsonPath("$.itemList[0].quantity",
                        Matchers.is(5)));

        //Update the item to increase the quantity
        ItemRequestDTO itemRequestDTO = testData.getItemRequest(null);
        itemRequestDTO.setQuantity(6L);

        //Perform the PUT operation to update the item in the cart
        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/carts/" +cartCode + "/entries")
                        .contentType("application/json")
                        .content(asJsonString(itemRequestDTO))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                .andExpect( MockMvcResultMatchers.status().isOk())
                .andExpect( MockMvcResultMatchers.jsonPath("$.quantity",
                        Matchers.is(6)));

        //Get the cart which is created for the user. User is fetched based on Token
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/carts")
                        .contentType("application/json")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                .andExpect( MockMvcResultMatchers.jsonPath("$[0].code", Matchers.is(cartCode)))
                .andExpect( MockMvcResultMatchers.jsonPath("$[0].itemList[0].quantity",
                        Matchers.is(6)));
    }

    @Test
    public void itemAdditionToCartWhenAlreadyPresentExceptionScenario() throws Exception {

        //Authenticate the user to get the token
        MvcResult tokenResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/authenticate")
                        .contentType("application/json")
                        .content(asJsonString(getRegisterRequestInput())))
                .andExpect( MockMvcResultMatchers.status().isOk())
                .andReturn();

        String tokenResponseAsString = tokenResult.getResponse().getContentAsString();
        String token = (String) convertJSONStringToObject(tokenResponseAsString).get("token");


        //Get the cart which is created for the user.
        MvcResult getCartResult =mockMvc.perform(
                MockMvcRequestBuilders.get("/api/carts")
                        .contentType("application/json")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                .andReturn();

        String getCartResponseAsString = getCartResult.getResponse().getContentAsString();
        Map<String, Object> map = (Map) convertJSONListStringToObject(getCartResponseAsString).get(0);
        String cartCode = (String) map.get("code");


        //Add same item again for the user. Should throw error
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/carts/" +cartCode + "/entries")
                        .contentType("application/json")
                        .content(asJsonString(testData.getItemRequest(null)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                .andExpect( MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(result -> Assert.assertTrue(result.getResolvedException() instanceof CartItemException));

    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String,Object>convertJSONStringToObject(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String,Object> map = mapper.readValue(json, Map.class);
        return map;
    }


    public static List convertJSONListStringToObject(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List list = mapper.readValue(json, ArrayList.class);
        return list;
    }
    private UserAuthRequest getRegisterRequestInput() {
        UserAuthRequest userAuthRequest = new UserAuthRequest();
        userAuthRequest.setUsername("customer");
        userAuthRequest.setPassword("password");
        return userAuthRequest;
    }

    private UserDTO userDetailsInputRequest() {
        UserDTO user = new UserDTO();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@test.com");
        user.setShippingAddress(getUserAddress());
        user.setBillingAddress(getUserAddress());
        return user;
    }

    private Address getUserAddress() {

        Address address = new Address();
        address.setCountry("India");
        address.setName("Home");
        address.setPostalCode("70463");
        address.setStreetName("Lane1");
        address.setStreetNumber("25");
        return address;
    }


}
