package com.treem.treem.helpers.security;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Matthew Walker on 2/12/16.
 * Copyright (c) 2016 Treem LLC. All rights reserved.
 */
public class Encryption {
    public final static Encryption SHARED_INSTANCE = new Encryption();

    private Encryption() {
        // Prevent direct instantiation
    }

    private String hmacSHA256(String key) {

        try {
            byte[] inputData = key.getBytes("UTF-8");
            byte[] keyData = key.getBytes("UTF-8");

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(keyData, "HmacSHA256");
            sha256_HMAC.init(secretKey);

            return Base64.encodeToString(sha256_HMAC.doFinal(inputData), inputData.length);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getObfuscatedKeyWithClassTypes (byte[] keyBytes, String className1, String className2, String className3) {
        className1 = hmacSHA256(className1);
        className2 = hmacSHA256(className2);
        className3 = hmacSHA256(className3);
        final byte[] classBytes = (className1 + className2 + className3).getBytes();

        byte[] returnBytes = new byte[keyBytes.length];
        byte returnByte;

        for(int i=0; i < keyBytes.length; ++i) {
            returnByte = (byte) ((int)keyBytes[i] ^ (int)classBytes[i]);

            // must be valid within "-" / "." / "_" / DIGIT / ALPHA
            if(!isValidOAuthCharacter(returnByte)) {
                returnByte = ((i % 2 == 0) || (i > classBytes.length - 1) || !isValidOAuthCharacter(classBytes[i])) ? keyBytes[i] : classBytes[i];
            }

            returnBytes[i] = returnByte;
        }

        try {
            return new String(returnBytes, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean isValidOAuthCharacter(int charByte) {
        return (charByte > 47 && charByte < 58) || (charByte > 64 && charByte < 91) || (charByte > 96 && charByte < 123) || (charByte == 45) || (charByte == 46) || (charByte == 95);
    }
}
