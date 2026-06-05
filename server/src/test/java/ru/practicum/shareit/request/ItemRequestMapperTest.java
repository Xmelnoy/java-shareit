package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestReq;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Тесты маппера запросов вещей")
class ItemRequestMapperTest {

    @Autowired
    private ItemRequestMapper mapper;

    @Autowired
    private ItemMapper itemMapper;

    @Test
    @DisplayName("toItemEntity - должен маппить NewItemRequestReq в ItemRequest")
    void toItemEntity_shouldMapCorrectly() {
        NewItemRequestReq request = new NewItemRequestReq();
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2026, 12, 1, 10, 0));

        ItemRequest itemRequest = mapper.toItemEntity(request);

        assertThat(itemRequest).isNotNull();
        assertThat(itemRequest.getDescription()).isEqualTo("Нужна дрель");
        assertThat(itemRequest.getCreated()).isEqualTo(LocalDateTime.of(2026, 12, 1, 10, 0));
        assertThat(itemRequest.getId()).isNull();
        assertThat(itemRequest.getRequestor()).isNull();
        assertThat(itemRequest.getItems()).isNull();
    }

    @Test
    @DisplayName("toItemEntity - должен возвращать null при null request")
    void toItemEntity_shouldReturnNull_whenRequestIsNull() {
        ItemRequest itemRequest = mapper.toItemEntity(null);

        assertThat(itemRequest).isNull();
    }

    @Test
    @DisplayName("toRequestDto - должен маппить ItemRequest в ItemRequestDto")
    void toRequestDto_shouldMapCorrectly() {
        Item item1 = new Item();
        item1.setItemId(10L);
        item1.setName("Дрель");
        item1.setDescription("Мощная дрель");
        item1.setAvailable(true);

        Item item2 = new Item();
        item2.setItemId(20L);
        item2.setName("Молоток");
        item2.setDescription("Надёжный молоток");
        item2.setAvailable(true);

        ItemRequest request = new ItemRequest();
        request.setId(100L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2026, 12, 1, 10, 0));
        request.setItems(List.of(item1, item2));

        ItemRequestDto dto = mapper.toRequestDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 12, 1, 10, 0));
        assertThat(dto.getItems()).hasSize(2);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(10L);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(dto.getItems().get(1).getId()).isEqualTo(20L);
        assertThat(dto.getItems().get(1).getName()).isEqualTo("Молоток");
    }

    @Test
    @DisplayName("toRequestDto - должен возвращать null при null request")
    void toRequestDto_shouldReturnNull_whenRequestIsNull() {
        ItemRequestDto dto = mapper.toRequestDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toRequestDto - должен маппить request с null items")
    void toRequestDto_shouldMapRequestWithNullItems() {
        ItemRequest request = new ItemRequest();
        request.setId(100L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2026, 12, 1, 10, 0));
        request.setItems(null);

        ItemRequestDto dto = mapper.toRequestDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getItems()).isNull();
    }

    @Test
    @DisplayName("toRequestDto - должен маппить request с пустым списком items")
    void toRequestDto_shouldMapRequestWithEmptyItems() {
        ItemRequest request = new ItemRequest();
        request.setId(100L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2026, 12, 1, 10, 0));
        request.setItems(List.of());

        ItemRequestDto dto = mapper.toRequestDto(request);

        assertThat(dto).isNotNull();
        assertThat(dto.getItems()).isEmpty();
    }
}