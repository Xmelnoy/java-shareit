package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = getUserOrThrow(ownerId);
        Item item = ItemMapper.toEntity(itemDto);
        item.setOwner(owner);
        Item saved = itemRepository.save(item);
        log.info("Создана вещь с id={} владельцем с id={}", saved.getId(), ownerId);
        return ItemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Пользователь не владелец вещи.");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item updated = itemRepository.save(item);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemResponseDto getById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBooking(itemId);
            nextBooking = getNextBooking(itemId);
        }

        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(commentDtos)
                .build();
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(Long ownerId) {
        getUserOrThrow(ownerId);
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                .map(item -> {
                    List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList());
                    BookingShortDto lastBooking = getLastBooking(item.getId());
                    BookingShortDto nextBooking = getNextBooking(item.getId());
                    return ItemResponseDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .lastBooking(lastBooking)
                            .nextBooking(nextBooking)
                            .comments(comments)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        boolean hasCompletedBooking = bookingRepository.findCompletedBooking(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now()).isPresent();

        if (!hasCompletedBooking) {
            throw new BadRequestException("Пользователь не может комментировать," +
                    " поскольку он еще не завершил бронирование данного товара");
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        Comment saved = commentRepository.save(comment);
        return CommentMapper.toDto(saved);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден."));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена."));
    }

    private BookingShortDto getLastBooking(Long itemId) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStatusAndStartBefore(
                itemId, BookingStatus.APPROVED, LocalDateTime.now(),
                Sort.by(Sort.Direction.DESC, "start"));
        if (bookings.isEmpty()) return null;
        Booking booking = bookings.get(0);
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    private BookingShortDto getNextBooking(Long itemId) {
        List<Booking> bookings = bookingRepository.findByItemIdAndStatusAndStartAfter(
                itemId, BookingStatus.APPROVED, LocalDateTime.now(),
                Sort.by(Sort.Direction.ASC, "start"));
        if (bookings.isEmpty()) return null;
        Booking booking = bookings.get(0);
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}