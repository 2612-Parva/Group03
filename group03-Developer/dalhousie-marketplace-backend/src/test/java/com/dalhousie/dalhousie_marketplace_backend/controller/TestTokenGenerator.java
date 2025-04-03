package com.dalhousie.dalhousie_marketplace_backend.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class TestTokenGenerator {

    private static final long ONE_HOUR_MILLISECONDS = 1000 * 60 * 60;
    public static void main(String[] args) {
        String secretKey = "CBBAFF36D1B6EAD227C9E573495B12FBFAEF93F7C4FD825BCD3A7B4FBCC58680"; // use your actual secret
        String token = Jwts.builder()
                .setSubject("TestUser")
                .claim("userId", 1L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ONE_HOUR_MILLISECONDS)) // 1 hour
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();

        System.out.println("Bearer " + token);
    }
}
