package com.intermediate.Blog.Application.Security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenHelper {

    private static final String secretKey = "A8fu92KdL0pQwXr3Zs9TjBv4Nc7Gh5Uy1Rq2Mx6Wz8Ye0Ts4Vn9Hb3Df6Pq8Kl2Mn8fu92KdL0pQwXr3Zs9TjBv4Nc7Gh5Uy1Rq2Mx6Wz8Ye0Ts4Vn9Hb3Df6Pq8Kl2Mn";

    private final Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
    private String username;


    public String generateToken(String username , String role ){

        Date Expiration = Date.from(
                LocalDateTime.now()
                        .plusMonths(1)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        Map<String , Object> claims = new HashMap<>();
        claims.put("role", role);

        return
                Jwts.builder()
                        .setClaims(claims)
                        .setSubject(username)
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(Expiration)
                        .signWith(key,SignatureAlgorithm.HS256)
                        .compact();
    }

    public String extractUsername(String token){
       return
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token){
        Object roleObj = getAllClaims(token).get("role");
        return  roleObj!=null ? roleObj.toString() : null;
    }

    public Claims getAllClaims(String token){
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();


            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }


}
