package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.IlligalArgumentException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnavailibleExceprion;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса бронирований")
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private NewBookingRequest request;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUserId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@test.com");

        booker = new User();
        booker.setUserId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@test.com");

        item = new Item();
        item.setItemId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner);

        request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(3));

        booking = new Booking();
        booking.setId(1L);
        booking.setStartDate(request.getStart());
        booking.setEndDate(request.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(request.getStart());
        bookingDto.setEnd(request.getEnd());
        bookingDto.setStatus(BookingStatus.WAITING);
    }

    @Test
    @DisplayName("addBooking - должен создавать бронирование")
    void addBooking_shouldCreateBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.toBookingEntity(any(NewBookingRequest.class))).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.addBooking(2L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("addBooking - должен выбрасывать NotFoundException, если пользователь не найден")
    void addBooking_shouldThrowNotFoundException_whenUserNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.addBooking(99L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("addBooking - должен выбрасывать NotFoundException, если предмет не найден")
    void addBooking_shouldThrowNotFoundException_whenItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        request.setItemId(99L);

        assertThatThrownBy(() -> bookingService.addBooking(2L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("addBooking - должен выбрасывать UnavailibleExceprion, если предмет недоступен")
    void addBooking_shouldThrowUnavailibleExceprion_whenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.addBooking(2L, request))
                .isInstanceOf(UnavailibleExceprion.class)
                .hasMessageContaining("не доступен");
    }

    @Test
    @DisplayName("addBooking - должен выбрасывать IlligalArgumentException, если даты невалидны")
    void addBooking_shouldThrowIlligalArgumentException_whenDatesInvalid() {
        request.setStart(LocalDateTime.now().plusDays(3));
        request.setEnd(LocalDateTime.now().plusDays(1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.addBooking(2L, request))
                .isInstanceOf(IlligalArgumentException.class)
                .hasMessageContaining("раньше начала");
    }

    @Test
    @DisplayName("updateBooking - должен подтверждать бронирование")
    void updateBooking_shouldApprove() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.updateBooking(1L, 1L, true);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("updateBooking - должен отклонять бронирование")
    void updateBooking_shouldReject() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.updateBooking(1L, 1L, false);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("updateBooking - должен выбрасывать AccessException, если пользователь не владелец")
    void updateBooking_shouldThrowAccessException_whenUserNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBooking(99L, 1L, true))
                .isInstanceOf(AccessException.class)
                .hasMessageContaining("Не является владельцем");
    }

    @Test
    @DisplayName("getBooking - должен возвращать бронирование для booker")
    void getBooking_shouldReturnForBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBooking(2L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getBooking - должен возвращать бронирование для owner")
    void getBooking_shouldReturnForOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBooking(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getBooking - должен выбрасывать AccessException, если пользователь не booker и не owner")
    void getBooking_shouldThrowAccessException_whenUserNotBookerOrOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBooking(99L, 1L))
                .isInstanceOf(AccessException.class)
                .hasMessageContaining("Не является владельцем");
    }

    @Test
    @DisplayName("getBookings - должен возвращать все бронирования")
    void getBookings_shouldReturnAll() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerId(2L)).thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookings(2L, "ALL");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getBookings - должен возвращать текущие бронирования")
    void getBookings_shouldReturnCurrent() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentByBookerId(
                eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookings(2L, "CURRENT");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getBookings - должен возвращать прошедшие бронирования")
    void getBookings_shouldReturnPast() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findPastByBookerId(
                eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookings(2L, "PAST");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getBookings - должен возвращать будущие бронирования")
    void getBookings_shouldReturnFuture() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findFutureByBookerId(
                eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookings(2L, "FUTURE");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getBookings - должен возвращать бронирования со статусом WAITING")
    void getBookings_shouldReturnWaiting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(2L, BookingStatus.WAITING))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookings(2L, "WAITING");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getBookings - должен возвращать пустой список для неизвестного state")
    void getBookings_shouldReturnEmptyForUnknownState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getBookings(2L, "UNKNOWN");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getBookings - должен возвращать отклонённые бронирования при state=REJECTED")
    void getBookings_shouldReturnRejected_whenStateIsRejected() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(2L, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookings(2L, "REJECTED");

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndStatus(2L, BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("addBooking - должен выбрасывать IlligalArgumentException, если время начала и конца совпадают")
    void addBooking_shouldThrowIlligalArgumentException_whenStartEqualsEnd() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        request.setStart(sameTime);
        request.setEnd(sameTime);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.addBooking(2L, request))
                .isInstanceOf(IlligalArgumentException.class)
                .hasMessageContaining("совпадают");
    }


    @Test
    @DisplayName("getBooking - должен выбрасывать NotFoundException, если бронирование не найдено")
    void getBooking_shouldThrowNotFoundException_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдено")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateBooking - должен выбрасывать NotFoundException, если бронирование не найдено")
    void updateBooking_shouldThrowNotFoundException_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking(1L, 99L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдено")
                .hasMessageContaining("99");
    }
}