package com.ticket_booking.idempotency;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.domain.models.IdempotencyRecord;
import com.ticket_booking.domain.models.enums.IdempotencyOperation;
import com.ticket_booking.idempotency.exceptions.IdempotencyKeyReuseException;
import com.ticket_booking.idempotency.exceptions.IdempotencyOperationInProgressException;
import com.ticket_booking.idempotency.exceptions.IdempotencyRecordNotFoundException;
import com.ticket_booking.idempotency.repositories.IIdempotencyRepository;

@Service
public class IdempotencyService {
	
	private final IIdempotencyRepository repository;
	private final IdempotencyProperties properties;
	
	public IdempotencyService(
			IIdempotencyRepository repository,
			IdempotencyProperties properties) {
		this.repository = repository;
		this.properties = properties;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord claim(
    		IdempotencyOperation operation,
    		String identityId,
    	    String key,
    	    String requestHash) {
		
		identityId = Strings.trimToNull(identityId);
		
		Optional<IdempotencyRecord> existing = 
				repository.findByOperationAndIdentityIdAndIdempotencyKey(
						operation,
						identityId,
						key
				);

        if (existing.isPresent()) {
        	return validateExisting(
        			existing.get(), 
        			requestHash
        	);
        }
        
        Instant expiresAt =
                Instant.now().plus(
                        Duration.ofMinutes(
                                properties.expirationMin()
                        )
                );
        	
        Optional<Long> insertedId =
                repository.insertIfAbsent(
                       operation,
                       identityId,
                       key,
                       requestHash,
                       expiresAt
                );

        if (insertedId.isPresent()) {
            return repository.findById(insertedId.get())
                    .orElseThrow(() ->
                            new IdempotencyRecordNotFoundException()
                    );
        }

        /*
         * In case another transaction won the race.
         */
        IdempotencyRecord concurrent =
        		repository.findByOperationAndIdentityIdAndIdempotencyKey(
						operation, 
						identityId,
						key
				)
                .orElseThrow(() ->
                		new IdempotencyRecordNotFoundException()
                );

        return validateExisting(
        		concurrent, 
        		requestHash
        );
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(
            Long recordId,
            String responseBody) {
		
		IdempotencyRecord record =
                repository.findById(recordId)
                .orElseThrow(() ->
                		new IdempotencyRecordNotFoundException()
                );
		
		record.complete(responseBody);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void release(Long recordId) {

        repository.deleteById(recordId);
    }
	
	private IdempotencyRecord validateExisting(
			IdempotencyRecord record,
			String requestHash) {
		
		if (!record.hasSameRequest(requestHash)) {
    	    throw new IdempotencyKeyReuseException();
    	}
    	
        if (record.isInProgress()) {
            throw new IdempotencyOperationInProgressException();
        }

        return record;
	}
}
