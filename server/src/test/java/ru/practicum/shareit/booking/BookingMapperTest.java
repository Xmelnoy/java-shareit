package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toDto_mapsAllFieldsAndNested() {
        User owner = User.builder().id(1L).name("Owner").email("owner@mail.com").build();
        User booker = User.builder().id(2L).name("Booker").email("booker@mail.com").build();
        Item item = Item.builder()
                .id(5L).name("Drill").description("Powerful").available(true).owner(owner).build();
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 2, 10, 0);
        Booking booking = Booking.builder()
                .id(100L).start(start).end(end)
                .item(item).booker(booker).status(BookingStatus.APPROVED).build();

        BookingResponseDto dto = BookingMapper.toDto(booking);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getBooker().getName()).isEqualTo("Booker");
        assertThat(dto.getItem().getId()).isEqualTo(5L);
        assertThat(dto.getItem().getName()).isEqualTo("Drill");
        assertThat(dto.getItem().getAvailable()).isTrue();
    }
}
