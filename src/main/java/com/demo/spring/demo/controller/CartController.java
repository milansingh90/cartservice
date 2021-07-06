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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemService cartItemService;


    @Operation(summary = "Find carts by cart code.", description = "Get cart by cart code for the user. " +
            "User is fetched from the provided Authorization header.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched cart",
                    content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = Void.class)))  })
    @GetMapping("/api/carts/{code}")
    public Optional<Cart> findCartById(
            @Parameter(description="Cart code to fetch the cart. Cannot be empty.", required=true) @PathVariable String code,
            Principal principal) throws CartNotFoundException, UserNotAllowedException {

        Optional<Cart> cart = cartService.getCartByCode(code);
        if(!cart.isPresent()){
            throw new CartNotFoundException("Cart for the given code not found");
        }
        checkIfUserAllowedToViewOrModifyCart(principal, cart.get());
        return cart;
    }

    @Operation(summary = "Find all carts for the user.",
            description = "Fetches all carts associated with the user ",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched cart",
                    content = @Content(array =@ArraySchema(schema = @Schema(implementation = Cart.class)))),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = Void.class)))  })
    @GetMapping("/api/carts")
    public List<Cart> findAllCarts(Principal principal) {
        List<Cart> carts =  cartService.getCarts();
        return carts.stream()
                .filter(cart -> principal.getName() == cart.getUserInfo().getUserAuth().getUsername())
                .collect(Collectors.toList());
    }

    @Operation(summary = "Create cart", description = "Creates a cart for the user. " +
            "Returns the cart code which is used in other endpoint calls" +
            "User is fetched from the provided Authorization header.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cart is successfully created",
                    content = @Content(schema = @Schema(implementation = Cart.class))) })
    @PostMapping("/api/carts/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Optional<Cart> createNewCart(Principal principal) {
        Cart cart = cartService.createAndSaveCart(principal);
        return Optional.of(cart);
    }

    @Operation(summary = "Add Items to the cart", description = "Add items to the created cart of the user. ",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item is added to the cart",
                    content = @Content(schema = @Schema(implementation = Cart.class))),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "User does not have access to view/modify cart",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = Void.class))) })
    @PostMapping("/api/carts/{code}/entries")
    public Optional<Cart> addItemToCart(
            @Parameter(description="Cart code to add items the cart. Cannot be empty.", required=true)
            @PathVariable String code,
            @Parameter(description="Item to add. Cannot null or empty.",
                    required=true, schema=@Schema(implementation = ItemRequestDTO.class))
            @RequestBody ItemRequestDTO itemRequestDTO,
            Principal principal)
        throws CartNotFoundException, CartItemException, UserNotAllowedException {
        getAndPrepareCart(code, itemRequestDTO, principal);
        return cartService.addItemToCart(itemRequestDTO);

    }

    @Operation(summary = "Update Item in the cart", description = "Update items which are already added in the cart. ",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item is updated in the cart",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "403", description = "User does not have access to view/modify cart",
                    content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Item not found",
                    content = @Content(schema = @Schema(implementation = Void.class)))})
    @PutMapping("/api/carts/{code}/entries")
    public Optional<Item> updateItem(
            @Parameter(description="Cart code to update the cart. Cannot be empty.", required=true)
            @PathVariable String code,
            @Parameter(description="Item to update. Cannot null or empty.",
                    required=true, schema=@Schema(implementation = ItemRequestDTO.class))
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

