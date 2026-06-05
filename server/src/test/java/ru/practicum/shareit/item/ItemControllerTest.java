package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.NewCommentRequest;
import ru.practicum.shareit.item.dto.ItemBooking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@DisplayName("Тесты контроллера вещей")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    private NewItemRequest newItemRequest;
    private UpdateItemRequest updateItemRequest;
    private ItemDto itemDto;
    private ItemBooking itemBooking;
    private NewCommentRequest commentRequest;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        newItemRequest = new NewItemRequest();
        newItemRequest.setName("Дрель");
        newItemRequest.setDescription("Мощная дрель");
        newItemRequest.setAvailable(true);

        updateItemRequest = new UpdateItemRequest();
        updateItemRequest.setName("Обновленная дрель");

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);

        itemBooking = new ItemBooking();
        itemBooking.setId(1L);
        itemBooking.setName("Дрель");
        itemBooking.setDescription("Мощная дрель");
        itemBooking.setAvailable(true);

        commentRequest = new NewCommentRequest();
        commentRequest.setText("Отличная вещь!");

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Отличная вещь!");
        commentDto.setAuthorName("Иван");
    }

    @Test
    @DisplayName("POST /items - должен создавать вещь")
    void addItem_shouldReturnCreated() throws Exception {
        when(itemService.addItem(eq(1L), any(NewItemRequest.class))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    @DisplayName("PATCH /items/{id} - должен обновлять вещь")
    void updateItem_shouldReturnOk() throws Exception {
        when(itemService.updateItem(eq(1L), eq(1L), any(UpdateItemRequest.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /items/{id} - должен удалять вещь")
    void deleteItem_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /items/{id} - должен возвращать вещь с бронированиями")
    void getItemById_shouldReturnItemBooking() throws Exception {
        when(itemService.getItem(1L, 1L)).thenReturn(itemBooking);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    @DisplayName("GET /items - должен возвращать список вещей владельца")
    void getItems_shouldReturnList() throws Exception {
        when(itemService.getItems(eq(1L), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(itemBooking)));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @DisplayName("GET /items/search - должен искать вещи по тексту")
    void searchItems_shouldReturnList() throws Exception {
        when(itemService.searchItems(eq(1L), eq("дрель"), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(itemDto)));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Дрель"));
    }

    @Test
    @DisplayName("POST /items/{id}/comment - должен добавлять комментарий")
    void addComment_shouldReturnCreated() throws Exception {
        when(itemService.addComment(eq(1L), eq(1L), any(NewCommentRequest.class))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }
}