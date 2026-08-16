package com.ticket_booking.venue.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ticket_booking.domain.models.Venue;

public interface IVenueRepository extends JpaRepository<Venue, Long> {

	List<Venue> findByOrderByIdAsc(Pageable pageable);
	
    List<Venue> findByIdGreaterThanOrderByIdAsc(
            Long id,
            Pageable pageable
    );

    Optional<Venue> findByUuid(UUID uuid);
    
    @Query("""
    		SELECT DISTINCT v 
    		FROM Venue v 
    		LEFT JOIN FETCH v.seats 
    		WHERE v.uuid = :venueId 
    			AND v.active = true
    		""")
    Optional<Venue> findByUuidFetchSeats(@Param("venueId") UUID venueId);
}
