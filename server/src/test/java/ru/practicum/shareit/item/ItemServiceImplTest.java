package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
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
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner() {
        return User.builder().id(1L).name("Owner").email("owner@mail.com").build();
    }

    private Item item(Long id, User owner) {
        return Item.builder()
                .id(id)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .build();
    }

    @Test
    void create_success() {
        User owner = owner();
        ItemDto dto = ItemDto.builder().name("Drill").description("Powerful drill").available(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> {
            Item i = inv.getArgument(0);
            i.setId(10L);
            return i;
        });

        ItemDto result = itemService.create(1L, dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getAvailable()).isTrue();

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(owner);
    }

    @Test
    void create_userNotFound_throwsNotFound() {
        ItemDto dto = ItemDto.builder().name("Drill").description("d").available(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(1L, dto))
                .isInstanceOf(NotFoundException.class);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_partial_success() {
        User owner = owner();
        Item existing = item(5L, owner);
        ItemDto dto = ItemDto.builder().name("New name").description("New desc").available(false).build();
        when(itemRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDto result = itemService.update(1L, 5L, dto);

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void update_onlyName_keepsOthers() {
        User owner = owner();
        Item existing = item(5L, owner);
        ItemDto dto = ItemDto.builder().name("Only name").build();
        when(itemRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(itemRepository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemDto result = itemService.update(1L, 5L, dto);

        assertThat(result.getName()).isEqualTo("Only name");
        assertThat(result.getDescription()).isEqualTo("Powerful drill");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void update_notOwner_throwsForbidden() {
        User owner = owner();
        Item existing = item(5L, owner);
        ItemDto dto = ItemDto.builder().name("x").build();
        when(itemRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> itemService.update(999L, 5L, dto))
                .isInstanceOf(ForbiddenException.class);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void update_itemNotFound_throwsNotFound() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(1L, 5L, ItemDto.builder().name("x").build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_owner_withBookingsAndComments() {
        User owner = owner();
        Item itm = item(5L, owner);
        User booker = User.builder().id(2L).name("Booker").email("b@mail.com").build();

        Comment comment = Comment.builder()
                .id(1L).text("Great").item(itm).author(booker).created(LocalDateTime.now()).build();
        when(itemRepository.findById(5L)).thenReturn(Optional.of(itm));
        when(commentRepository.findByItemId(5L)).thenReturn(List.of(comment));

        Booking last = Booking.builder().id(100L).booker(booker)
                .start(LocalDateTime.now().minusDays(2)).end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED).build();
        Booking next = Booking.builder().id(101L).booker(booker)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED).build();
        when(bookingRepository.findByItemIdAndStatusAndStartBefore(eq(5L), eq(BookingStatus.APPROVED), any(), any(Sort.class)))
                .thenReturn(List.of(last));
        when(bookingRepository.findByItemIdAndStatusAndStartAfter(eq(5L), eq(BookingStatus.APPROVED), any(), any(Sort.class)))
                .thenReturn(List.of(next));

        ItemResponseDto result = itemService.getById(1L, 5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().get(0).getAuthorName()).isEqualTo("Booker");
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getLastBooking().getId()).isEqualTo(100L);
        assertThat(result.getLastBooking().getBookerId()).isEqualTo(2L);
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getId()).isEqualTo(101L);
    }

    @Test
    void getById_owner_noBookings_nullBookings() {
        User owner = owner();
        Item itm = item(5L, owner);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(itm));
        when(commentRepository.findByItemId(5L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemIdAndStatusAndStartBefore(eq(5L), any(), any(), any(Sort.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.findByItemIdAndStatusAndStartAfter(eq(5L), any(), any(), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        ItemResponseDto result = itemService.getById(1L, 5L);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getById_notOwner_noBookings() {
        User owner = owner();
        Item itm = item(5L, owner);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(itm));
        when(commentRepository.findByItemId(5L)).thenReturn(Collections.emptyList());

        ItemResponseDto result = itemService.getById(999L, 5L);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        verify(bookingRepository, never()).findByItemIdAndStatusAndStartBefore(anyLong(), any(), any(), any(Sort.class));
    }

    @Test
    void getById_notFound_throwsNotFound() {
        when(itemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getById(1L, 5L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner_returnsListWithBookings() {
        User owner = owner();
        Item itm = item(5L, owner);
        User booker = User.builder().id(2L).name("Booker").email("b@mail.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(1L)).thenReturn(List.of(itm));
        when(commentRepository.findByItemId(5L)).thenReturn(Collections.emptyList());

        Booking last = Booking.builder().id(100L).booker(booker)
                .start(LocalDateTime.now().minusDays(2)).end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED).build();
        when(bookingRepository.findByItemIdAndStatusAndStartBefore(eq(5L), any(), any(), any(Sort.class)))
                .thenReturn(List.of(last));
        when(bookingRepository.findByItemIdAndStatusAndStartAfter(eq(5L), any(), any(), any(Sort.class)))
                .thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.getAllByOwner(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastBooking()).isNotNull();
        assertThat(result.get(0).getNextBooking()).isNull();
    }

    @Test
    void getAllByOwner_userNotFound_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getAllByOwner(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void search_nullText_returnsEmpty() {
        assertThat(itemService.search(null)).isEmpty();
        verify(itemRepository, never()).search(any());
    }

    @Test
    void search_blankText_returnsEmpty() {
        assertThat(itemService.search("   ")).isEmpty();
        verify(itemRepository, never()).search(any());
    }

    @Test
    void search_withText_returnsResults() {
        when(itemRepository.search("drill")).thenReturn(List.of(item(5L, owner())));

        List<ItemDto> result = itemService.search("drill");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void addComment_success() {
        User author = User.builder().id(2L).name("Author").email("a@mail.com").build();
        Item itm = item(5L, owner());
        CommentDto dto = CommentDto.builder().text("Nice").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(itm));
        when(bookingRepository.findCompletedBooking(eq(2L), eq(5L), eq(BookingStatus.APPROVED), any()))
                .thenReturn(Optional.of(Booking.builder().id(1L).build()));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(50L);
            return c;
        });

        CommentDto result = itemService.addComment(2L, 5L, dto);

        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getText()).isEqualTo("Nice");
        assertThat(result.getAuthorName()).isEqualTo("Author");
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void addComment_noCompletedBooking_throwsBadRequest() {
        User author = User.builder().id(2L).name("Author").email("a@mail.com").build();
        Item itm = item(5L, owner());
        CommentDto dto = CommentDto.builder().text("Nice").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(author));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(itm));
        when(bookingRepository.findCompletedBooking(eq(2L), eq(5L), eq(BookingStatus.APPROVED), any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(2L, 5L, dto))
                .isInstanceOf(BadRequestException.class);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_userNotFound_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(2L, 5L, CommentDto.builder().text("x").build()))
                .isInstanceOf(NotFoundException.class);
    }
}
