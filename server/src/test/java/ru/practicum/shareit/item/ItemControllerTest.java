package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

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
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto(Long id) {
        return ItemDto.builder().id(id).name("Drill").description("Powerful").available(true).build();
    }

    @Test
    void create_returns201() throws Exception {
        ItemDto request = ItemDto.builder().name("Drill").description("Powerful").available(true).build();
        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(itemDto(10L));

        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void create_invalidBody_returns400() throws Exception {
        ItemDto request = ItemDto.builder().name("").description("").available(null).build();

        mockMvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns200() throws Exception {
        ItemDto response = itemDto(10L);
        when(itemService.update(eq(1L), eq(10L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(patch("/items/10")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void update_notOwner_returns403() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemDto.class)))
                .thenThrow(new ForbiddenException("not owner"));

        mockMvc.perform(patch("/items/10")
                        .header(HEADER, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns200() throws Exception {
        ItemResponseDto response = ItemResponseDto.builder()
                .id(10L).name("Drill").description("Powerful").available(true).build();
        when(itemService.getById(1L, 10L)).thenReturn(response);

        mockMvc.perform(get("/items/10").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(itemService.getById(anyLong(), anyLong())).thenThrow(new NotFoundException("nf"));

        mockMvc.perform(get("/items/99").header(HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByOwner_returns200() throws Exception {
        ItemResponseDto r = ItemResponseDto.builder()
                .id(10L).name("Drill").description("Powerful").available(true).build();
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(r));

        mockMvc.perform(get("/items").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void search_returns200() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of(itemDto(10L)));

        mockMvc.perform(get("/items/search").param("text", "drill").header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void addComment_returns201() throws Exception {
        CommentDto request = CommentDto.builder().text("Nice").build();
        CommentDto response = CommentDto.builder()
                .id(1L).text("Nice").authorName("Author").created(LocalDateTime.now()).build();
        when(itemService.addComment(eq(1L), eq(10L), any(CommentDto.class))).thenReturn(response);

        mockMvc.perform(post("/items/10/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Nice"))
                .andExpect(jsonPath("$.authorName").value("Author"));
    }

    @Test
    void addComment_blankText_returns400() throws Exception {
        CommentDto request = CommentDto.builder().text("").build();

        mockMvc.perform(post("/items/10/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_noCompletedBooking_returns400() throws Exception {
        CommentDto request = CommentDto.builder().text("Nice").build();
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenThrow(new BadRequestException("no booking"));

        mockMvc.perform(post("/items/10/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(itemService).addComment(eq(1L), eq(10L), any(CommentDto.class));
    }
}
