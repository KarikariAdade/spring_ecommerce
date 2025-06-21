package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsServiceImplementation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImplementation userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        logger.debug("AuthTokenFilter called for url: {}", request.getRequestURI());

        try {
            // extract token
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // get username
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                // load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                // request data is being parsed in the authentication object
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("roles from jwt: {}", userDetails.getAuthorities());

            }


        } catch (Exception exception) {

            logger.error(exception.getMessage(), exception);
        }

        // adds this filter to the Spring Security Filter Chain
        // so you basically update the security context, and continues the security filter chain

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {

//        String jwt = jwtUtils.getJwtFromHeader(request);
        String jwt = jwtUtils.getJwtFromCookies(request);

        logger.debug("Jwt header: {}", jwt);

        return jwt;
    }


}
