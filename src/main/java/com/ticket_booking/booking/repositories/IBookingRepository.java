package com.ticket_booking.booking.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.Booking;
import com.ticket_booking.domain.models.BookingSeat;
import com.ticket_booking.domain.models.enums.BookingStatus;

import jakarta.persistence.LockModeType;

public interface IBookingRepository extends JpaRepository<Booking, Long> {
	
	Optional<Booking> findByUuidAndUserId(UUID uuid, Long userId);
    
	@Query("""
	        SELECT b.id
	        FROM Booking b
	        WHERE b.event.id = :eventId
	          AND b.status = :status
	        ORDER BY b.id
	""")
	List<Long> findIdByEventIdAndStatus(
	        @Param("eventId") Long eventId,
	        @Param("status") BookingStatus status,
	        Limit limit);

    @Query("""
        SELECT DISTINCT b
        FROM Booking b
        JOIN FETCH b.event e
        JOIN FETCH e.venue
        JOIN FETCH b.bookingSeats bs
        JOIN FETCH bs.eventSeat es
        JOIN FETCH es.seat
        WHERE b.uuid = :bookingId 
          AND b.user.id = :userId
    """)
    Optional<Booking> findByUuidAndUserIdFull(
            @Param("bookingId") UUID bookingId, 
            @Param("userId") Long userId);
    
    @Query("""
    	SELECT DISTINCT b
        FROM Booking b
    	JOIN FETCH b.event e
    	JOIN FETCH e.venue v
    	LEFT JOIN FETCH b.bookingSeats bs
    	LEFT JOIN FETCH bs.eventSeat es
    	LEFT JOIN FETCH es.seat s
    	WHERE b.id = :id
    """)
    Optional<Booking> findByIdWithDescriptionAssociations(
    		@Param("id") Long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT b
		FROM Booking b
		WHERE b.id = :id
	""")
	Optional<Booking> findByIdForUpdate(@Param("id") Long id);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT b
		FROM Booking b
		WHERE b.uuid = :uuid
		  AND b.user.id = :userId
	""")
	Optional<Booking> findByUuidAndUserIdForUpdate(
			@Param("uuid") UUID uuid, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    	SELECT b
	    FROM Booking b
	    WHERE b.status = 'PENDING'
	      AND b.expiresAt <= :now
	    ORDER BY b.expiresAt
    	""")
    List<Booking> findPendingBookingsExpiredForUpdate(
    		@Param("now") Instant now, 
    		Limit limit);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT p.booking
		FROM Payment p
		WHERE p.id = :paymentId
	""")
	Optional<Booking> findByPaymentIdForUpdate(@Param("paymentId") Long paymentId);

    @Query("""
        SELECT b
        FROM Booking b
        JOIN FETCH b.event e
        JOIN FETCH e.venue
        WHERE b.user.id = :userId
        ORDER BY b.bookedAt DESC, b.id DESC
    """)
    List<Booking> findFirstPage(
            @Param("userId") Long userId,
            Limit limit);

    @Query("""
        SELECT b
        FROM Booking b
        JOIN FETCH b.event e
        JOIN FETCH e.venue
        WHERE b.user.id = :userId
        AND (
            b.bookedAt < :bookingTimestamp
            OR (
                b.bookedAt = :bookingTimestamp
                AND b.id < :id
            )
        )
        ORDER BY b.bookedAt DESC, b.id DESC
    """)
    List<Booking> findNextPage(
            @Param("userId") Long userId,
            @Param("bookingTimestamp") Instant bookingTimestamp,
            @Param("id") Long id,
            Limit limit);

    @Query("""
        SELECT bs
        FROM BookingSeat bs
        JOIN FETCH bs.eventSeat es
        JOIN FETCH es.seat
        WHERE bs.booking.id IN :ids
    """)
    List<BookingSeat> fetchBookingsSeats(
            @Param("ids") List<Long> ids);
}
