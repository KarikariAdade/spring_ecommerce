package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImplementation;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationInMilliseconds;

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtCookieName}")
    private String jwtCookie;

    // getting jwt from header

//    public String getJwtFromHeader(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//
//        logger.debug("Authorization Header: {}", bearerToken);
//        //remove bearer prefix
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//
//        return null;
//
//    }


    // uses cookie instead of the normal bearer token
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);

        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    public ResponseCookie generateJwtCookie(UserDetailsImplementation userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());

        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
                .path("/api")
                .maxAge(24 * 60 * 60)
                .httpOnly(false)
                .build();

        return cookie;
    }

    // use this to overwrite the cookie. this deletes the existing cooking and logs the user out
    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
                .path("/api")
                .build();

        return cookie;
    }

    // generating token from username

    public String generateTokenFromUsername (String username) {
//    public String generateTokenFromUsername (UserDetails userDetails) {

//        String username = userDetails.getUsername();

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationInMilliseconds)))
                .signWith(key())
                .compact();

    }

    // getting username from jwt token

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token) // more like parseSignedData
                .getPayload()
                .getSubject(); // subject is user
    }

    // generate signing key

    public Key key () {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }


    // validate jwt token
    public boolean validateJwtToken(String authToken) {

        try {

            System.out.println("Validating token: " + authToken);

            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);

            return true;

        } catch (MalformedJwtException exception) {

            logger.error("Invalid JWT token: {}", exception.getMessage());

        } catch (ExpiredJwtException exception) {

            logger.error("Expired JWT token: {}", exception.getMessage());

        } catch (UnsupportedJwtException exception) {

            logger.error("Unsupported JWT token: {}", exception.getMessage());

        } catch (IllegalArgumentException exception) {

            logger.error("JWT claims string is empty: {}", exception.getMessage());

        }

        return false;
    }

}
