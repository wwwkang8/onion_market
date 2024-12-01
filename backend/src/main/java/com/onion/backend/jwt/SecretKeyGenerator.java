package com.onion.backend.jwt;

import java.security.Key;
import java.util.Base64;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class SecretKeyGenerator {

    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Generated Secret key : " + secretKey);
    }

}
