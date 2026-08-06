package com.ticket_booking.common;

import java.util.List;

public record CursorPage<T>(
	    List<T> items,
	    String nextCursor,
	    boolean hasNext
	) {}
