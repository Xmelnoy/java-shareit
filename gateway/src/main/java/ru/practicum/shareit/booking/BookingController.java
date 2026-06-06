package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.exception.ConflictException;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @Valid @RequestBody NewBookingRequest request) {
        log.info("POST /bookings: userId={}, itemId={}", userId, request.getItemId());
        validateBookingDates(request);
        return bookingClient.addBooking(userId, request);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> updateBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @PathVariable("bookingId") Long bookingId,
                                                    @RequestParam("approved") Boolean approved) {
        log.info("PATCH /bookings/{}: userId={}, approved={}", bookingId, userId, approved);
        if (approved == null) {
            return ResponseEntity.badRequest().body("Approved is required");
        }
        return bookingClient.updateBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable("bookingId") Long bookingId) {
        log.info("GET /bookings/{}: userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping()
    public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @RequestParam(name = "state", defaultValue = "ALL") String state,
                                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                        @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /bookings: userId={}, state={}, from={}, size={}", userId, state, from, size);
        BookingState bookingState = BookingState.of(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state.toUpperCase()));

        return bookingClient.getBookings(userId, bookingState, from, size);
    }

    private void validateBookingDates(NewBookingRequest request) {
        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();

        if (start.equals(end)) {
            throw new ConflictException("Conflict: start=" + start + " == end=" + end + " ");
        }

        if (start.isAfter(end)) {
            throw new ConflictException("Conflict: start=" + start + " after end=" + end + " ");
        }
    }
}
