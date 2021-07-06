package com.demo.spring.demo.controller;

import com.demo.spring.demo.beans.Cart;
import com.demo.spring.demo.beans.Item;
import com.demo.spring.demo.beans.UserAuth;
import com.demo.spring.demo.beans.UserInfo;
import com.demo.spring.demo.dto.ItemRequestDTO;
import com.demo.spring.demo.dto.JwtRequest;
import com.demo.spring.demo.dto.JwtResponse;
import com.demo.spring.demo.dto.UserAuthRequest;
import com.demo.spring.demo.dto.UserDTO;
import com.demo.spring.demo.service.UserDetailsManageService;
import com.demo.spring.demo.config.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


@RestController
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsManageService userDetailsService;


    @Operation(summary = "Get JWT token after authentication", description = "Method to authenticate the user " +
            "and provide the access token ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = Void.class))) })
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponse(token));
    }

    @Operation(summary = "Register the user", description = "Registers the user with his credentials. " +
            "Credentials are then used in /authenticate ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Registered",
                    content = @Content(schema = @Schema(implementation = Void.class))) })
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUserAuthDetails(
            @Parameter(description="Registering username and password of user. Cannot be empty",
                    required=true, schema=@Schema(implementation = UserAuthRequest.class))
            @RequestBody UserAuthRequest user) throws Exception {
        return ResponseEntity.ok(userDetailsService.save(user));
    }

    @Operation(summary = "Add the user details", description = "Add the profile properties of the user ",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details added successfully",
                    content = @Content(schema = @Schema(implementation = UserInfo.class)))})
    @RequestMapping(value = "/user/details", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(
            @Parameter(description="Request params to register user details.",
                    required=true, schema=@Schema(implementation = UserDTO.class))
            @RequestBody UserDTO userDetails,
            Principal principal) throws Exception {
        return ResponseEntity.ok(userDetailsService.saveUser(userDetails, principal));
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
