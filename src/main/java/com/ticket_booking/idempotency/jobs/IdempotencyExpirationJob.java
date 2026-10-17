package com.ticket_booking.idempotency.jobs;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ticket_booking.idempotency.repositories.IIdempotencyRepository;

import io.micrometer.observation.annotation.Observed;

@Component
public class IdempotencyExpirationJob {
	
	private static final Logger log =
            LoggerFactory.getLogger(IdempotencyExpirationJob.class);

    private final IIdempotencyRepository repository;

    public IdempotencyExpirationJob(
            IIdempotencyRepository repository) {

        this.repository = repository;
    }

    @Observed(
        name = "job.idempotency.expiration",
        contextualName = "idempotency-expiration-job"
    )
    @Scheduled(
        cron = "${app.idempotency.expiration.cron:0 0 3 * * *}"
    )
    public void deleteExpiredRecords() {

        int deleted = repository.deleteExpiredCompleted(
                Instant.now()
        );
        
        log.info(
                "Idempotency expiration completed deletedCount={}",
                deleted
        );
    }
}
