package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking.BookingStatus;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingResponseDto response(Long id, BookingStatus status) {
        return BookingResponseDto.builder()
                .id(id)
                .start(LocalDateTime.of(2026, 1, 1, 10, 0))
                .end(LocalDateTime.of(2026, 1, 2, 10, 0))
                .status(status)
                .booker(UserDto.builder().id(2L).name("Booker").email("b@mail.com").build())
                .item(ItemDto.builder().id(5L).name("Drill").description("Powerful").available(true).build())
                .build();
    }

    private BookingRequestDto validRequest() {
        return BookingRequestDto.builder()
                .itemId(5L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void create_returns201() throws Exception {
        when(bookingService.create(eq(2L), any(BookingRequestDto.class)))
                .thenReturn(response(100L, BookingStatus.WAITING));

        mockMvc.perform(post("/bookings")
                        .header(HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(2))
                .andExpect(jsonPath("$.item.id").value(5));
    }

    @Test
    void create_nullItemId_returns400() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_pastStart_returns400() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(5L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nonFutureEnd_returns400() throws Exception {
        BookingRequestDto request = BookingRequestDto.builder()
                .itemId(5L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_true_returns200() throws Exception {
        when(bookingService.approve(1L, 100L, true))
                .thenReturn(response(100L, BookingStatus.APPROVED));

        mockMvc.perform(patch("/bookings/100")
                        .header(HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_false_returns200() throws Exception {
        when(bookingService.approve(1L, 100L, false))
                .thenReturn(response(100L, BookingStatus.REJECTED));

        mockMvc.perform(patch("/bookings/100")
                        .header(HEADER, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void approve_notOwner_returns403() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), eq(true)))
                .thenThrow(new ForbiddenException("not owner"));

        mockMvc.perform(patch("/bookings/100")
                        .header(HEADER, 9L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approve_alreadyProcessed_returns400() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), eq(true)))
                .thenThrow(new BadRequestException("processed"));

        mockMvc.perform(patch("/bookings/100")
                        .header(HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        when(bookingService.getById(2L, 100L)).thenReturn(response(100L, BookingStatus.WAITING));

        mockMvc.perform(get("/bookings/100").header(HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenThrow(new NotFoundException("nf"));

        mockMvc.perform(get("/bookings/99").header(HEADER, 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_stranger_returns403() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenThrow(new ForbiddenException("stranger"));

        mockMvc.perform(get("/bookings/100").header(HEADER, 999L))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserBookings_defaultAll_returns200() throws Exception {
        when(bookingService.getUserBookings(2L, BookingState.ALL))
                .thenReturn(List.of(response(100L, BookingStatus.APPROVED)));

        mockMvc.perform(get("/bookings").header(HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100));

        verify(bookingService).getUserBookings(2L, BookingState.ALL);
    }

    @Test
    void getUserBookings_explicitState_returns200() throws Exception {
        when(bookingService.getUserBookings(2L, BookingState.WAITING))
                .thenReturn(List.of(response(100L, BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings").header(HEADER, 2L).param("state", "WAITING"))
                .andExpect(status().isOk());

        verify(bookingService).getUserBookings(2L, BookingState.WAITING);
    }

    @Test
    void getOwnerBookings_defaultAll_returns200() throws Exception {
        when(bookingService.getOwnerBookings(1L, "ALL"))
                .thenReturn(List.of(response(100L, BookingStatus.APPROVED)));

        mockMvc.perform(get("/bookings/owner").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService).getOwnerBookings(1L, "ALL");
    }

    @Test
    void getOwnerBookings_explicitState_returns200() throws Exception {
        when(bookingService.getOwnerBookings(1L, "FUTURE"))
                .thenReturn(List.of(response(100L, BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings/owner").header(HEADER, 1L).param("state", "FUTURE"))
                .andExpect(status().isOk());

        verify(bookingService).getOwnerBookings(1L, "FUTURE");
    }

    @Test
    void getOwnerBookings_unknownState_returns400() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), eq("BANANA")))
                .thenThrow(new BadRequestException("Unknown state: BANANA"));

        mockMvc.perform(get("/bookings/owner").header(HEADER, 1L).param("state", "BANANA"))
                .andExpect(status().isBadRequest());
    }
}
