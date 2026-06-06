package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    private ItemRequestResponseDto response(Long id) {
        return ItemRequestResponseDto.builder()
                .id(id)
                .description("desc" + id)
                .created(LocalDateTime.now())
                .items(List.of())
                .build();
    }

    @Test
    void create_returns201() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("need a drill");
        when(requestService.create(eq(1L), any(ItemRequestCreateDto.class))).thenReturn(response(10L));

        mockMvc.perform(post("/requests")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("desc10"));
    }

    @Test
    void create_blankDescription_returns400() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRequests_returns200() throws Exception {
        when(requestService.getUserRequests(1L)).thenReturn(List.of(response(1L), response(2L)));

        mockMvc.perform(get("/requests").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllRequests_returns200WithParams() throws Exception {
        when(requestService.getAllRequests(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(response(5L)));

        mockMvc.perform(get("/requests/all")
                        .header(HEADER, 1L)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        verify(requestService).getAllRequests(1L, 0, 5);
    }

    @Test
    void getAllRequests_usesDefaults() throws Exception {
        when(requestService.getAllRequests(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/requests/all").header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(requestService).getAllRequests(1L, 0, 10);
    }

    @Test
    void getRequestById_returns200() throws Exception {
        when(requestService.getRequestById(1L, 3L)).thenReturn(response(3L));

        mockMvc.perform(get("/requests/3").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void getRequestById_notFound_returns404() throws Exception {
        when(requestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("not found"));

        mockMvc.perform(get("/requests/99").header(HEADER, 1L))
                .andExpect(status().isNotFound());
    }
}
