package com.ticket_booking.common;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.ticket_booking.common.exceptions.DomainException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log =
	        LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(
    		BadCredentialsException ex,
            HttpServletRequest request) {
    	
    	return problem(
    			HttpStatus.UNAUTHORIZED, 
    			"Authentication Failed",
    			"Invalid email or password.",
    			request
    	);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ProblemDetail> handleJwtException(
    		JwtException ex,
            HttpServletRequest request) {
    	
        return problem(
        		HttpStatus.UNAUTHORIZED, 
        		"Invalid Token",
        		"Token is invalid or expired.",
        		request
        );
    }
    
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAuthorizationDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request) {

        return problem(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have permission to access this resource.",
                request);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed.");

        problem.setTitle("Bad Request");
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("path", request.getRequestURI());
        
        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> {
                            String propertyPath = violation.getPropertyPath().toString();
                            int lastDot = propertyPath.lastIndexOf('.');
                            return lastDot != -1 ? propertyPath.substring(lastDot + 1) : propertyPath;
                        },
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));

        problem.setProperty("invalid_fields", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException ife &&
            ife.getTargetType().isEnum()) {

            String field = ife.getPath().isEmpty()
                    ? "value"
                    : ife.getPath().get(0).getPropertyName();

            String allowedValues = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            return problem(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Request",
                    "Invalid value for '%s'. Allowed values are: %s."
                            .formatted(field, allowedValues),
                    request);
        }

        return problem(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON",
                "The request body is malformed.",
                request);
    }

    @ExceptionHandler({
    	MethodArgumentNotValidException.class,
    	HandlerMethodValidationException.class})
    public ResponseEntity<ProblemDetail> handleInvalidMethodArgumentException(
    		MethodArgumentNotValidException ex,
            HttpServletRequest request) {
    	
    	ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed.");

        problem.setTitle("Bad Request");
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("path", request.getRequestURI());
        
        Map<String, String> errors =
                ex.getBindingResult()
                  .getFieldErrors()
                  .stream()
                  .collect(Collectors.toMap(
                          FieldError::getField,
                          DefaultMessageSourceResolvable::getDefaultMessage,
                          (a, b) -> a));

        problem.setProperty("invalid_fields", errors);

        return ResponseEntity
        		.status(HttpStatus.BAD_REQUEST)
        		.body(problem);
    }
    
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(
    		DomainException ex,
            HttpServletRequest request) {

        return problem(
                ex.getStatus(),
                ex.getTitle(),
                ex.getMessage(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception", ex);

        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request);
    }
    
    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setProperty("timestamp", OffsetDateTime.now());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(status).body(problem);
    }
}