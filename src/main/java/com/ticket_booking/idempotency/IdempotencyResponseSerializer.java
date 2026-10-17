package com.ticket_booking.idempotency;

import org.springframework.stereotype.Service;

import com.ticket_booking.idempotency.exceptions.IdempotencyResponseDeserializationException;
import com.ticket_booking.idempotency.exceptions.IdempotencyResponseSerializationException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class IdempotencyResponseSerializer {

    private final ObjectMapper objectMapper;

    public IdempotencyResponseSerializer(
    		ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(Object response) {

        try {
            return objectMapper.writeValueAsString(response);
        } catch (JacksonException ex) {
            throw new IdempotencyResponseSerializationException();
        }
    }

    public <T> T deserialize(
            String responseBody,
            Class<T> responseType) {

        try {
            return objectMapper.readValue(
                    responseBody,
                    responseType
            );
        } catch (JacksonException ex) {
            throw new IdempotencyResponseDeserializationException();
        }
    }
}