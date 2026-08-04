package com.ticket_booking.booking.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.Booking;

public interface IBookingRepository extends JpaRepository<Booking, Long> {

	@Query("""
			SELECT DISTINCT b
			FROM Booking b
			JOIN FETCH b.event e
			JOIN FETCH e.venue
			LEFT JOIN FETCH b.seats
			WHERE b.user.id = :userId
			ORDER BY b.bookingDate DESC
			""")
	public List<Booking> findAllBookingsByUserId(@Param("userId") Long userId);
}
