package com.ticket_booking.idempotency;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

import com.ticket_booking.idempotency.exceptions.IdempotencyFingerprintException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class IdempotencyFingerprintService {

    private final ObjectMapper objectMapper;

    public IdempotencyFingerprintService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String hash(Object request) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(request);
            return sha256(json);

        } catch (JacksonException ex) {
            throw new IdempotencyFingerprintException();
        }
    }

    private String sha256(byte[] value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(value);

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    ex
            );
        }
    }
}
