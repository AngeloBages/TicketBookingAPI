package com.ticket_booking.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ticket_booking.domain.models.enums.IdempotencyOperation;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

	/**
     * SpEL expression identifying the specific idempotent operation.
     *
     * <p>Example:
     * {@code #request.operation()}
     */
    IdempotencyOperation operation();

    /**
     * SpEL expression identifying the idempotency key.
     *
     * <p>Example:
     * {@code #request.idempotencyKey()}
     */
    String key();

    /**
     * SpEL expression identifying the identity associated with the request.
     *
     * <p>When specified, the identity is persisted with the idempotency
	 * record and must match on subsequent requests using the same key.
	 *
	 * <p>Example:
	 * {@code #request.userId()}
     */
    String identityId() default "";

    /**
     * SpEL expression identifying the semantic request fingerprint.
     *
     * <p>Example:
     * {@code #request}
     */
    String fingerprint();
}
