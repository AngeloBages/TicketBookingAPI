package com.ticket_booking.booking.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.Booking;

public interface IBookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.user.id = :userId
        ORDER BY b.bookingDate DESC, b.id DESC
    """)
    List<Booking> findFirstPage(
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.user.id = :userId
        AND (
            b.bookingDate < :bookingDate
            OR (
                b.bookingDate = :bookingDate
                AND b.id < :id
            )
        )
        ORDER BY b.bookingDate DESC, b.id DESC
    """)
    List<Booking> findNextPage(
            @Param("userId") Long userId,
            @Param("bookingDate") LocalDateTime bookingDate,
            @Param("id") Long id,
            Pageable pageable);

    @Query("""
        SELECT DISTINCT b
        FROM Booking b
        JOIN FETCH b.event e
        JOIN FETCH e.venue
        LEFT JOIN FETCH b.seats
        WHERE b.id IN :ids
        ORDER BY b.bookingDate DESC, b.id DESC
    """)
    List<Booking> fetchBookings(
            @Param("ids") List<Long> ids);
}
