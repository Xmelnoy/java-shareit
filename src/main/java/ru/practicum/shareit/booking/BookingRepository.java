package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime now1, LocalDateTime now2, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime now1, LocalDateTime now2, Sort sort);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status, Sort sort);

    Optional<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, BookingStatus status, LocalDateTime endBefore);

    List<Booking> findByItemIdAndStatusAndStartBefore(Long itemId, BookingStatus status, LocalDateTime startBefore, Sort sort);

    List<Booking> findByItemIdAndStatusAndStartAfter(Long itemId, BookingStatus status, LocalDateTime startAfter, Sort sort);

    @Query("select b from Booking b where b.booker.id = :bookerId " +
            "and b.item.id = :itemId and b.status = :status and b.end < :now")
    Optional<Booking> findCompletedBooking(@Param("bookerId") Long bookerId,
                                           @Param("itemId") Long itemId,
                                           @Param("status") BookingStatus status,
                                           @Param("now") LocalDateTime now);
}