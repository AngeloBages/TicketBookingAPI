package com.ticket_booking.domain.models;

import com.ticket_booking.domain.models.enums.SeatStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "seat")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_seq")
	@SequenceGenerator(name = "seat_seq", sequenceName = "seat_seq_generator")
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private int number;

    @Column(name = "seat_row", nullable = false)
    private String row;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    public Seat() {
    }


    public Long getId() { return id; }
    public int getNumber() { return number; }
    public String getRow() { return row; }
    public SeatStatus getStatus() { return status; }
    public Venue getVenue() { return venue; }
}
