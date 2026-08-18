package com.ticket_booking.event.repositories;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.Event;
import com.ticket_booking.domain.models.EventSeat;
import com.ticket_booking.event.repositories.views.EventSeatView;

import jakarta.persistence.LockModeType;

public interface IEventRepository extends JpaRepository<Event, Long> {

	@Query("""
			SELECT e 
			FROM Event e 
			JOIN FETCH e.venue
			ORDER BY e.startsAt DESC, e.id DESC
			""")
	public List<Event> findFirstPage(Limit limit);
	
	@Query("""
			SELECT e
			FROM Event e
			JOIN FETCH e.venue
			WHERE e.startsAt < :startsAt 
				OR (
					e.startsAt = :startsAt 
						AND 
					e.id < :eventId
				)
			ORDER BY e.startsAt DESC, e.id DESC
			""")
	public List<Event> findNextPage(
			@Param("startsAt") Instant startsAt, 
			@Param("eventId") Long eventId, 
			Limit limit);
	
	public Optional<Event> findByUuid(UUID eventId);
	
	@Query("""
			SELECT DISTINCT e 
			FROM Event e 
			JOIN FETCH e.venue
			JOIN FETCH e.eventSeats es
			JOIN FETCH es.seat
			WHERE e.uuid = :eventId
			""")
	public Optional<Event> findByUuidFetchVenueAndSeats(@Param("eventId") UUID eventId);
	
	@Query("""
			SELECT new com.ticket_booking.event.repositories.views.EventSeatView(
				es.uuid,
				s.number,
				s.row,
				es.status,
				es.price
			)
			FROM Event e 
			JOIN e.eventSeats es 
			JOIN es.seat s
			WHERE e.uuid = :eventId
			ORDER BY s.row ASC, s.number ASC
			""")
	public List<EventSeatView> findSeatsByEventId(@Param("eventId") UUID eventId);
	
	public boolean existsByUuid(UUID uuid);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
	    SELECT es
	    FROM EventSeat es
	    JOIN FETCH es.seat
	    JOIN FETCH es.event e
	    WHERE e.uuid = :eventId
	      AND es.uuid IN :seatIds
	    """)
	public List<EventSeat> findSeatsForBooking(
			@Param("eventId") UUID eventId,
	        @Param("seatIds") Collection<UUID> seatIds);
}
