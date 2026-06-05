package ru.practicum.shareit.booking.dto;

import java.util.Optional;


public enum BookingState {
    WAITING, APPROVED, REJECTED, ALL, CURRENT, PAST;

    public static Optional<BookingState> of(String state) {
        for (BookingState bookingState : values()) {
            if (bookingState.name().equalsIgnoreCase(state)) {
                return Optional.of(bookingState);
            }
        }
        return Optional.empty();
    }
}
