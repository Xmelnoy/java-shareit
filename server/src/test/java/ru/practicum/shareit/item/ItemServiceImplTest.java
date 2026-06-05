package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.NewCommentRequest;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemBooking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса вещей")
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private NewItemRequest newItemRequest;
    private UpdateItemRequest updateItemRequest;
    private ItemDto itemDto;
    private ItemBooking itemBooking;
    private NewCommentRequest commentRequest;
    private Comment comment;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUserId(1L);
        owner.setName("Владелец");
        owner.setEmail("owner@test.com");

        booker = new User();
        booker.setUserId(2L);
        booker.setName("Бронер");
        booker.setEmail("booker@test.com");

        item = new Item();
        item.setItemId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner);

        newItemRequest = new NewItemRequest();
        newItemRequest.setName("Дрель");
        newItemRequest.setDescription("Мощная дрель");
        newItemRequest.setAvailable(true);

        updateItemRequest = new UpdateItemRequest();
        updateItemRequest.setName("Обновленная дрель");

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");

        itemBooking = new ItemBooking();
        itemBooking.setId(1L);
        itemBooking.setName("Дрель");

        commentRequest = new NewCommentRequest();
        commentRequest.setText("Отличная вещь!");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Отличная вещь!");
        comment.setCreated(LocalDateTime.now());

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличная вещь!");
        commentDto.setAuthorName("Бронер");
    }

    @Test
    @DisplayName("addItem - должен создавать вещь")
    void addItem_shouldCreateItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.newItemRequest(any(NewItemRequest.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.addItem(1L, newItemRequest);

        assertThat(result.getId()).isEqualTo(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("addItem - должен создавать вещь с запросом")
    void addItem_shouldCreateItemWithRequest() {
        newItemRequest.setRequestId(10L);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.newItemRequest(any(NewItemRequest.class))).thenReturn(item);
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.addItem(1L, newItemRequest);

        assertThat(result.getId()).isEqualTo(1L);
        verify(itemRequestRepository).findById(10L);
    }

    @Test
    @DisplayName("addItem - должен выбрасывать NotFoundException, если пользователь не найден")
    void addItem_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addItem(99L, newItemRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("addItem - должен выбрасывать NotFoundException, если запрос не найден")
    void addItem_shouldThrowNotFoundException_whenRequestNotFound() {
        newItemRequest.setRequestId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.newItemRequest(any(NewItemRequest.class))).thenReturn(item);
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addItem(1L, newItemRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("updateItem - должен обновлять вещь")
    void updateItem_shouldUpdateItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateItemRequest);

        assertThat(result.getId()).isEqualTo(1L);
        verify(itemMapper).updateItemFromRequest(any(Item.class), any(UpdateItemRequest.class));
    }

    @Test
    @DisplayName("updateItem - должен выбрасывать AccessException, если пользователь не владелец")
    void updateItem_shouldThrowAccessException_whenUserNotOwner() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(2L, 1L, updateItemRequest))
                .isInstanceOf(AccessException.class)
                .hasMessageContaining("Не является владельцем");
    }

    @Test
    @DisplayName("deleteItem - должен удалять вещь")
    void deleteItem_shouldDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L, 1L);

        verify(itemRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteItem - должен выбрасывать AccessException, если пользователь не владелец")
    void deleteItem_shouldThrowAccessException_whenUserNotOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.deleteItem(1L, 2L))
                .isInstanceOf(AccessException.class)
                .hasMessageContaining("Не является владельцем");
    }

    @Test
    @DisplayName("getItem - должен возвращать вещь с бронированиями для владельца")
    void getItem_shouldReturnItemWithBookingsForOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemBooking(any(Item.class))).thenReturn(itemBooking);
        when(commentRepository.findByItem_ItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        ItemBooking result = itemService.getItem(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getItem - должен возвращать вещь без бронирований для не-владельца")
    void getItem_shouldReturnItemWithoutBookingsForNonOwner() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemBooking(any(Item.class))).thenReturn(itemBooking);
        when(commentRepository.findByItem_ItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        ItemBooking result = itemService.getItem(2L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingRepository, never()).findCurrentByBookerId(
                anyLong(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getItems - должен возвращать список вещей владельца")
    void getItems_shouldReturnOwnerItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item)));
        when(itemMapper.toItemBooking(any(Item.class))).thenReturn(itemBooking);
        when(commentRepository.findByItem_ItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        Page<ItemBooking> result = itemService.getItems(1L, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("searchItems - должен искать вещи по тексту")
    void searchItems_shouldSearchByText() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.searchAvailableItems(eq("дрель"), eq(true), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item)));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        Page<ItemDto> result = itemService.searchItems(1L, "дрель", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("searchItems - должен возвращать пустую страницу при пустом тексте")
    void searchItems_shouldReturnEmptyPage_whenTextIsBlank() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        Page<ItemDto> result = itemService.searchItems(1L, "", 0, 10);

        assertThat(result.getContent()).isEmpty();
    }


    @Test
    @DisplayName("addComment - должен добавлять комментарий, если было бронирование")
    void addComment_shouldAddComment_whenBookingExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(
                eq(2L), eq(1L), any(LocalDateTime.class))).thenReturn(true);
        when(commentMapper.toCommentEntity(any(NewCommentRequest.class))).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        CommentDto result = itemService.addComment(2L, 1L, commentRequest);

        assertThat(result.getId()).isEqualTo(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("addComment - должен выбрасывать AccessException, если не было бронирования")
    void addComment_shouldThrowAccessException_whenNoBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.hasUserBookedItem(
                eq(2L), eq(1L), any(LocalDateTime.class))).thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(2L, 1L, commentRequest))
                .isInstanceOf(AccessException.class)
                .hasMessageContaining("не бронировал");
    }

    @Test
    @DisplayName("getItemEntity - должен возвращать предмет по ID")
    void getItemEntity_shouldReturnItem_whenFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = itemService.getItemEntity(1L);

        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        verify(itemRepository).findById(1L);
    }

    @Test
    @DisplayName("getItemEntity - должен выбрасывать NotFoundException, если предмет не найден")
    void getItemEntity_shouldThrowNotFoundException_whenNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemEntity(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("validateItem - должен выбрасывать NotFoundException при добавлении несуществующего предмета")
    void validateItem_shouldThrowNotFoundException_whenAddItemNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItem(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("validateItem - должен выбрасывать NotFoundException при обновлении несуществующего предмета")
    void validateItem_shouldThrowNotFoundException_whenUpdateItemNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(1L, 99L, updateItemRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("validateItem - должен выбрасывать NotFoundException при удалении несуществующего предмета")
    void validateItem_shouldThrowNotFoundException_whenDeleteItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден")
                .hasMessageContaining("99");
    }
}