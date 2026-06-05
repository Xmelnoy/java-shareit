package ru.practicum.shareit.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ItemBookingTime;
import ru.practicum.shareit.booking.dto.NewBookingRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Тесты маппера бронирований")
class BookingMapperTest {

    @Autowired
    private BookingMapper mapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Test
    @DisplayName("toBookingEntity - должен маппить NewBookingRequest в Booking")
    void toBookingEntity_shouldMapCorrectly() {
        NewBookingRequest request = new NewBookingRequest();
        request.setStart(LocalDateTime.of(2026, 12, 1, 10, 0));
        request.setEnd(LocalDateTime.of(2026, 12, 5, 10, 0));
        request.setItemId(10L);

        Booking booking = mapper.toBookingEntity(request);

        assertThat(booking).isNotNull();
        assertThat(booking.getStartDate()).isEqualTo(LocalDateTime.of(2026, 12, 1, 10, 0));
        assertThat(booking.getEndDate()).isEqualTo(LocalDateTime.of(2026, 12, 5, 10, 0));
        assertThat(booking.getId()).isNull();
        assertThat(booking.getStatus()).isNull();
        assertThat(booking.getItem()).isNull();
        assertThat(booking.getBooker()).isNull();
    }

    @Test
    @DisplayName("toBookingEntity - должен возвращать null при null request")
    void toBookingEntity_shouldReturnNull_whenRequestIsNull() {
        Booking booking = mapper.toBookingEntity(null);

        assertThat(booking).isNull();
    }

    @Test
    @DisplayName("toBookingDto - должен маппить Booking в BookingDto")
    void toBookingDto_shouldMapCorrectly() {
        User booker = new User();
        booker.setUserId(2L);
        booker.setName("Бронер");
        booker.setEmail("booker@test.com");

        User owner = new User();
        owner.setUserId(1L);
        owner.setName("Владелец");
        owner.setEmail("owner@test.com");

        Item item = new Item();
        item.setItemId(10L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStartDate(LocalDateTime.of(2026, 12, 1, 10, 0));
        booking.setEndDate(LocalDateTime.of(2026, 12, 5, 10, 0));
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(booker);
        booking.setItem(item);

        BookingDto dto = mapper.toBookingDto(booking);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 12, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 12, 5, 10, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getBooker().getName()).isEqualTo("Бронер");
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("toBookingDto - должен возвращать null при null booking")
    void toBookingDto_shouldReturnNull_whenBookingIsNull() {
        BookingDto dto = mapper.toBookingDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toBookingDto - должен маппить booking с null booker и item")
    void toBookingDto_shouldMapBookingWithNullBookerAndItem() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStartDate(LocalDateTime.of(2026, 12, 1, 10, 0));
        booking.setEndDate(LocalDateTime.of(2026, 12, 5, 10, 0));
        booking.setStatus(BookingStatus.WAITING);
        booking.setBooker(null);
        booking.setItem(null);

        BookingDto dto = mapper.toBookingDto(booking);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getBooker()).isNull();
        assertThat(dto.getItem()).isNull();
    }

    @Test
    @DisplayName("toItemBookingsTimes - должен маппить Booking в ItemBookingTime")
    void toItemBookingsTimes_shouldMapCorrectly() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setStartDate(LocalDateTime.of(2026, 12, 1, 10, 0));
        booking.setEndDate(LocalDateTime.of(2026, 12, 5, 10, 0));

        ItemBookingTime times = mapper.toItemBookingsTimes(booking);

        assertThat(times).isNotNull();
        assertThat(times.getStart()).isEqualTo(LocalDateTime.of(2026, 12, 1, 10, 0));
        assertThat(times.getEnd()).isEqualTo(LocalDateTime.of(2026, 12, 5, 10, 0));
    }

    @Test
    @DisplayName("toItemBookingsTimes - должен возвращать null при null booking")
    void toItemBookingsTimes_shouldReturnNull_whenBookingIsNull() {
        ItemBookingTime times = mapper.toItemBookingsTimes(null);

        assertThat(times).isNull();
    }
}