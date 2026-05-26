package ru.practicum.shareit.booking;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingRequestDto requestDto) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(requestDto.getItemId());
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может забронировать собственный товар");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Товар недоступен для бронирования");
        }
        if (requestDto.getEnd().isBefore(requestDto.getStart()) || requestDto.getEnd().equals(requestDto.getStart())) {
            throw new BadRequestException("Дата окончания должна быть после даты начала");
        }
        Booking booking = Booking.builder()
                .start(requestDto.getStart())
                .end(requestDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        Booking saved = bookingRepository.save(booking);
        log.info("Создана бронь id={} для вещи id={} пользователем id={}", saved.getId(), item.getId(), userId);
        return BookingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец может принять/отклонить бронь.");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Бронирование уже обработано.");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);
        return BookingMapper.toDto(updated);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только букер или владелец может просматривать бронирование");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state) {
        getUserOrThrow(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, sort);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        };

        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state) {
        getUserOrThrow(ownerId);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerId(ownerId, sort);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(ownerId, now, now, sort);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, now, sort);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, now, sort);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, sort);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, sort);
                break;
            default:
                throw new BadRequestException("Unknown state: " + state);
        }
        return bookings.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена."));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено."));
    }
}