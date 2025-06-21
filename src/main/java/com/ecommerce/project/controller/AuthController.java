package com.ecommerce.project.controller;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.requests.SignUpRequest;
import com.ecommerce.project.response.MessageResponse;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.jwt.LoginRequest;
import com.ecommerce.project.security.jwt.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtils jwtUtils;
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication;

        try {

            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );


        } catch (AuthenticationException exception) {

            Map<String, Object> map = new HashMap<>();
            map.put("message", "Invalid username or password");
            map.put("status", false);

            return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);

        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();

        // generate token from user name
//        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // get roles from token
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

//        UserInfoResponse loginResponse = new UserInfoResponse(userDetails.getId(), jwtToken, userDetails.getUsername(), roles);
        UserInfoResponse loginResponse = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(loginResponse);

    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser (@Valid @RequestBody SignUpRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username " + signUpRequest.getUsername() + " is already in use"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email " + signUpRequest.getEmail() + " is already in use"));
        }

        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword())
        );

        // role (string) from request, needs to be converted to the constant
        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            // if user role is empty, assign default role
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found"));

            roles.add(userRole);
        } else {
            // convert user role string to the set role in AppRole

        strRoles.forEach(role -> {
            switch (role) {
                case "admin":
                    Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                    roles.add(adminRole);
                    break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                        roles.add(sellerRole);
                        break;
                default:
                    Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role not found"));
                    roles.add(userRole);
            }
            });

        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @GetMapping("/username")
    public ResponseEntity<?> currentUsername(Authentication authentication) {

        UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserInfoResponse loginResponse = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return ResponseEntity.ok().body(loginResponse);

    }


    @PostMapping("/signout")
    public ResponseEntity<?> signOutUser() {
        // this sets the cookie to null
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("Successfully logged out")
        );
    }
}
