package com.ticket_booking.booking.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.Booking;
import com.ticket_booking.domain.models.BookingSeat;

public interface IBookingRepository extends JpaRepository<Booking, Long> {
	
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
            Pageable pageable);

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
            Pageable pageable);

    @Query("""
        SELECT DISTINCT bs
        FROM BookingSeat bs
        JOIN FETCH bs.eventSeat es
        JOIN FETCH es.seat
        WHERE bs.booking.id IN :ids
    """)
    List<BookingSeat> fetchBookingsSeats(
            @Param("ids") List<Long> ids);
    
    @Query("""
            SELECT b
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
		    JOIN FETCH b.bookingSeats bs
		    JOIN FETCH bs.eventSeat es
		    WHERE b.uuid = :bookingId
		      AND b.user.id = :userId
    		""")
    	Optional<Booking> findByUuidAndUserIdWithSeats(UUID bookingId, Long userId);
}
