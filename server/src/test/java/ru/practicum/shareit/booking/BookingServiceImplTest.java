package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner() {
        return User.builder().id(1L).name("Owner").email("owner@mail.com").build();
    }

    private User booker() {
        return User.builder().id(2L).name("Booker").email("booker@mail.com").build();
    }

    private Item item(Long id, User owner, boolean available) {
        return Item.builder()
                .id(id).name("Drill").description("Powerful drill")
                .available(available).owner(owner).build();
    }

    private BookingRequestDto requestDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        return BookingRequestDto.builder().itemId(itemId).start(start).end(end).build();
    }

    private Booking booking(Long id, User booker, Item item, BookingStatus status) {
        return Booking.builder()
                .id(id).booker(booker).item(item).status(status)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void create_success_statusWaiting() {
        User owner = owner();
        User booker = booker();
        Item item = item(5L, owner, true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingResponseDto result = bookingService.create(2L, requestDto(5L, start, end));

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(2L);
        assertThat(result.getItem().getId()).isEqualTo(5L);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void create_ownerBooksOwnItem_throwsNotFound() {
        User owner = owner();
        Item item = item(5L, owner, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(1L,
                requestDto(5L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_itemNotAvailable_throwsBadRequest() {
        User owner = owner();
        User booker = booker();
        Item item = item(5L, owner, false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(2L,
                requestDto(5L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_endBeforeStart_throwsBadRequest() {
        User owner = owner();
        User booker = booker();
        Item item = item(5L, owner, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.create(2L, requestDto(5L, start, end)))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_endEqualsStart_throwsBadRequest() {
        User owner = owner();
        User booker = booker();
        Item item = item(5L, owner, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        LocalDateTime moment = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.create(2L, requestDto(5L, moment, moment)))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(2L,
                requestDto(5L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
        verify(itemRepository, never()).findById(any());
    }

    @Test
    void create_itemNotFound_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker()));
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(2L,
                requestDto(5L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approve_approvedTrue_statusApproved() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.WAITING);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDto result = bookingService.approve(1L, 100L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_approvedFalse_statusRejected() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.WAITING);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponseDto result = bookingService.approve(1L, 100L, false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_notOwner_throwsForbidden() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.WAITING);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approve(999L, 100L, true))
                .isInstanceOf(ForbiddenException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approve_alreadyProcessed_throwsBadRequest() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.APPROVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approve(1L, 100L, true))
                .isInstanceOf(BadRequestException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approve_bookingNotFound_throwsNotFound() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approve(1L, 100L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_byBooker_success() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.WAITING);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getById(2L, 100L);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getBooker().getId()).isEqualTo(2L);
    }

    @Test
    void getById_byOwner_success() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.WAITING);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getById(1L, 100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getById_stranger_throwsForbidden() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.WAITING);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getById(999L, 100L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getById(2L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserBookings_all() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.APPROVED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker()));
        when(bookingRepository.findByBookerId(eq(2L), any(Sort.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.ALL);

        assertThat(result).hasSize(1);
        ArgumentCaptor<Sort> captor = ArgumentCaptor.forClass(Sort.class);
        verify(bookingRepository).findByBookerId(eq(2L), captor.capture());
        assertThat(captor.getValue().getOrderFor("start").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getUserBookings_current() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker()));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(eq(2L), any(), any(), any(Sort.class)))
                .thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getUserBookings(2L, BookingState.CURRENT);

        assertThat(result).isEmpty();
        verify(bookingRepository).findByBookerIdAndStartBeforeAndEndAfter(eq(2L), any(), any(), any(Sort.class));
    }

    @Test
    void getUserBookings_past() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker()));
        when(bookingRepository.findByBookerIdAndEndBefore(eq(2L), any(), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getUserBookings(2L, BookingState.PAST);

        verify(bookingRepository).findByBookerIdAndEndBefore(eq(2L), any(), any(Sort.class));
    }

    @Test
    void getUserBookings_future() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker()));
        when(bookingRepository.findByBookerIdAndStartAfter(eq(2L), any(), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getUserBookings(2L, BookingState.FUTURE);

        verify(bookingRepository).findByBookerIdAndStartAfter(eq(2L), any(), any(Sort.class));
    }

    @Test
    void getUserBookings_waiting() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker()));
        when(bookingRepository.findByBookerIdAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getUserBookings(2L, BookingState.WAITING);

        verify(bookingRepository).findByBookerIdAndStatus(eq(2L), eq(BookingStatus.WAITING), any(Sort.class));
    }

    @Test
    void getUserBookings_rejected() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker()));
        when(bookingRepository.findByBookerIdAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getUserBookings(2L, BookingState.REJECTED);

        verify(bookingRepository).findByBookerIdAndStatus(eq(2L), eq(BookingStatus.REJECTED), any(Sort.class));
    }

    @Test
    void getUserBookings_userNotFound_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getUserBookings(2L, BookingState.ALL))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnerBookings_all() {
        Booking booking = booking(100L, booker(), item(5L, owner(), true), BookingStatus.APPROVED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(bookingRepository.findByItemOwnerId(eq(1L), any(Sort.class))).thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, "ALL");

        assertThat(result).hasSize(1);
    }

    @Test
    void getOwnerBookings_all_lowercase() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(bookingRepository.findByItemOwnerId(eq(1L), any(Sort.class))).thenReturn(List.of());

        bookingService.getOwnerBookings(1L, "all");

        verify(bookingRepository).findByItemOwnerId(eq(1L), any(Sort.class));
    }

    @Test
    void getOwnerBookings_current() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(eq(1L), any(), any(), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getOwnerBookings(1L, "CURRENT");

        verify(bookingRepository).findByItemOwnerIdAndStartBeforeAndEndAfter(eq(1L), any(), any(), any(Sort.class));
    }

    @Test
    void getOwnerBookings_past() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(bookingRepository.findByItemOwnerIdAndEndBefore(eq(1L), any(), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getOwnerBookings(1L, "PAST");

        verify(bookingRepository).findByItemOwnerIdAndEndBefore(eq(1L), any(), any(Sort.class));
    }

    @Test
    void getOwnerBookings_future() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(bookingRepository.findByItemOwnerIdAndStartAfter(eq(1L), any(), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getOwnerBookings(1L, "FUTURE");

        verify(bookingRepository).findByItemOwnerIdAndStartAfter(eq(1L), any(), any(Sort.class));
    }

    @Test
    void getOwnerBookings_waiting() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getOwnerBookings(1L, "WAITING");

        verify(bookingRepository).findByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.WAITING), any(Sort.class));
    }

    @Test
    void getOwnerBookings_rejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(bookingRepository.findByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of());

        bookingService.getOwnerBookings(1L, "REJECTED");

        verify(bookingRepository).findByItemOwnerIdAndStatus(eq(1L), eq(BookingStatus.REJECTED), any(Sort.class));
    }

    @Test
    void getOwnerBookings_unknownState_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));

        assertThatThrownBy(() -> bookingService.getOwnerBookings(1L, "BANANA"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getOwnerBookings_userNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOwnerBookings(1L, "ALL"))
                .isInstanceOf(NotFoundException.class);
        verify(bookingRepository, never()).findByItemOwnerId(anyLong(), any(Sort.class));
    }
}
