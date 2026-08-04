package com.ticket_booking.common;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ticket_booking.common.exceptions.DomainException;
import com.ticket_booking.common.exceptions.EmailAlreadyInUseException;
import com.ticket_booking.common.exceptions.InvalidCurrentPasswordException;
import com.ticket_booking.common.exceptions.PasswordConfirmationException;
import com.ticket_booking.common.exceptions.SamePasswordException;
import com.ticket_booking.common.exceptions.UserNotFoundException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log =
	        LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	
	@ExceptionHandler(InvalidCurrentPasswordException.class)
	public ResponseEntity<ProblemDetail> handlePassword(
	        InvalidCurrentPasswordException ex,
            HttpServletRequest request) {

	    ProblemDetail problem = buildProblem(
	    		HttpStatus.UNAUTHORIZED,
	    		"Authentication failed",
	    		ex.getMessage(),
	    		request
	    );

	    return ResponseEntity
	            .status(HttpStatus.UNAUTHORIZED)
	            .body(problem);
	}
	
	@ExceptionHandler({
		PasswordConfirmationException.class,
		SamePasswordException.class
	})
	public ResponseEntity<ProblemDetail> handleBadRequest(
			DomainException ex,
            HttpServletRequest request) {

		ProblemDetail problem = buildProblem(
				HttpStatus.BAD_REQUEST,
				"Invalid request",
				ex.getMessage(),
				request
		);

		return ResponseEntity
				.badRequest()
				.body(problem);
	}
	
	@ExceptionHandler(EmailAlreadyInUseException.class)
	public ResponseEntity<ProblemDetail> handleEmailAlreadyInUse(
	        EmailAlreadyInUseException ex,
            HttpServletRequest request) {

	    ProblemDetail problem = buildProblem(
	    		HttpStatus.CONFLICT,
	    		"Email already in use",
	            ex.getMessage(),
	            request
	     );

	    return ResponseEntity
	            .status(HttpStatus.CONFLICT)
	            .body(problem);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleUserNotFound(
	        UserNotFoundException ex,
            HttpServletRequest request) {

	    ProblemDetail problem = buildProblem(
	    		HttpStatus.NOT_FOUND,
	    		"User not found",
	    		ex.getMessage(),
	    		request
	    );

	    return ResponseEntity
	    		.status(HttpStatus.NOT_FOUND)
	    		.body(problem);
	}
	
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(
    		BadCredentialsException ex,
            HttpServletRequest request) {
    	
    	ProblemDetail problem = buildProblem(
    			HttpStatus.UNAUTHORIZED, 
    			"Authentication Failed",
    			"Invalid email or password.",
    			request
    	);
    	
    	log.error("BadCredentialsException", ex);
        
    	return ResponseEntity
    			.status(HttpStatus.UNAUTHORIZED)
    			.body(problem);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ProblemDetail> handleJwtException(
    		JwtException ex,
            HttpServletRequest request) {
    	
        ProblemDetail problem = buildProblem(
        		HttpStatus.UNAUTHORIZED, 
        		"Invalid Token",
        		"Token is invalid or expired.",
        		request
        );

        log.error("JwtException", ex);
        
        return ResponseEntity
        		.status(HttpStatus.UNAUTHORIZED)
        		.body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(
    		MethodArgumentNotValidException ex,
            HttpServletRequest request) {
    	
        ProblemDetail problem = buildProblem(
        		HttpStatus.BAD_REQUEST, 
        		"Bad Request",
        		"Validation failed for one or more fields.",
        		request
        );
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        problem.setProperty("invalid_fields", fieldErrors);

        return ResponseEntity
        		.status(HttpStatus.BAD_REQUEST)
        		.body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(
    		Exception ex,
            HttpServletRequest request) {
    	
        ProblemDetail problem = buildProblem(
        		HttpStatus.INTERNAL_SERVER_ERROR, 
        		"Internal Server Error",
        		"An unexpected error occurred.",
        		request
        );
        
        log.error("InternalServerError: " + ex.getMessage());
        
        return ResponseEntity
        		.status(HttpStatus.INTERNAL_SERVER_ERROR)
        		.body(problem);
    }
    
    
    private ProblemDetail buildProblem(
    		HttpStatus status,
    		String title,
    		String detail,
    		HttpServletRequest request) {

    	ProblemDetail problem =
    			ProblemDetail.forStatusAndDetail(status, detail);

    	problem.setTitle(title);

    	problem.setProperty(
    			"timestamp",
    			OffsetDateTime.now());

    	problem.setProperty(
    			"path",
    			request.getRequestURI());

    	return problem;
    }
}