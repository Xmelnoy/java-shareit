package ru.practicum.shareit.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemInRequest;
import ru.practicum.shareit.item.dto.NewItemRequest;
import ru.practicum.shareit.item.dto.UpdateItemRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Тесты маппера предметов")
class ItemMapperTest {

    @Autowired
    private ItemMapper mapper;

    @Test
    @DisplayName("toItemEntity - должен маппить ItemDto в Item")
    void toItemEntity_shouldMapCorrectly() {
        ItemDto dto = new ItemDto();
        dto.setId(10L);
        dto.setName("Дрель");
        dto.setDescription("Мощная дрель");
        dto.setAvailable(true);

        Item item = mapper.toItemEntity(dto);

        assertThat(item).isNotNull();
        assertThat(item.getItemId()).isEqualTo(10L);
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Мощная дрель");
        assertThat(item.getAvailable()).isTrue();
    }

    @Test
    @DisplayName("toItemEntity - должен обрабатывать null значения")
    void toItemEntity_shouldHandleNullValues() {
        ItemDto dto = new ItemDto();
        dto.setId(10L);

        Item item = mapper.toItemEntity(dto);

        assertThat(item).isNotNull();
        assertThat(item.getItemId()).isEqualTo(10L);
        assertThat(item.getName()).isNull();
        assertThat(item.getDescription()).isNull();
        assertThat(item.getAvailable()).isNull();
    }

    @Test
    @DisplayName("toItemDto - должен маппить Item в ItemDto")
    void toItemDto_shouldMapCorrectly() {
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

        ItemDto dto = mapper.toItemDto(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getAvailable()).isTrue();
    }


    @Test
    @DisplayName("toItemList - должен маппить List<Item> в List<ItemDto>")
    void toItemList_shouldMapCorrectly() {
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

        List<ItemDto> result = mapper.toItemDtoList(List.of(item1, item2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("Дрель");
        assertThat(result.get(1).getId()).isEqualTo(20L);
        assertThat(result.get(1).getName()).isEqualTo("Молоток");
    }

    @Test
    @DisplayName("toItemDtoList - должен маппить List<Item> в List<ItemDto>")
    void toItemDtoList_shouldMapCorrectly() {
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

        List<ItemDto> result = mapper.toItemDtoList(List.of(item1, item2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(1).getId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("toItemBooking - должен маппить Item в ItemBooking")
    void toItemBooking_shouldMapCorrectly() {
        User owner = new User();
        owner.setUserId(1L);
        owner.setName("Владелец");

        Item item = new Item();
        item.setItemId(10L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner);

        var booking = mapper.toItemBooking(item);

        assertThat(booking).isNotNull();
        assertThat(booking.getId()).isEqualTo(10L);
        assertThat(booking.getName()).isEqualTo("Дрель");
        assertThat(booking.getDescription()).isEqualTo("Мощная дрель");
        assertThat(booking.getAvailable()).isTrue();
    }

    @Test
    @DisplayName("updateItemFromRequest - должен обновлять только непустые поля")
    void updateItemFromRequest_shouldUpdateOnlyNonNullFields() {
        Item item = new Item();
        item.setItemId(10L);
        item.setName("Старое имя");
        item.setDescription("Старое описание");
        item.setAvailable(true);

        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новое имя");

        mapper.updateItemFromRequest(item, request);

        assertThat(item.getName()).isEqualTo("Новое имя");
        assertThat(item.getDescription()).isEqualTo("Старое описание");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getItemId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("updateItemFromRequest - должен обновлять все поля если они не null")
    void updateItemFromRequest_shouldUpdateAllFields() {
        Item item = new Item();
        item.setItemId(10L);
        item.setName("Старое имя");
        item.setDescription("Старое описание");
        item.setAvailable(true);

        UpdateItemRequest request = new UpdateItemRequest();
        request.setName("Новое имя");
        request.setDescription("Новое описание");
        request.setAvailable(false);

        mapper.updateItemFromRequest(item, request);

        assertThat(item.getName()).isEqualTo("Новое имя");
        assertThat(item.getDescription()).isEqualTo("Новое описание");
        assertThat(item.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("newItemRequest - должен маппить NewItemRequest в Item")
    void newItemRequest_shouldMapCorrectly() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Мощная дрель");
        request.setAvailable(true);
        request.setRequestId(50L);

        Item item = mapper.newItemRequest(request);

        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getDescription()).isEqualTo("Мощная дрель");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getItemId()).isNull();
        assertThat(item.getOwner()).isNull();
    }

    @Test
    @DisplayName("toItemInRequest - должен маппить Item в Item (для запроса)")
    void toItemInRequest_shouldMapCorrectly() {
        User owner = new User();
        owner.setUserId(1L);
        owner.setName("Владелец");

        Item item = new Item();
        item.setItemId(10L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(owner);

        ItemInRequest result = mapper.toItemInRequest(item);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("toItemEntity - должен возвращать null при null DTO")
    void toItemEntity_shouldReturnNull_whenDtoIsNull() {
        Item result = mapper.toItemEntity(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toItemDto - должен возвращать null при null Item")
    void toItemDto_shouldReturnNull_whenItemIsNull() {
        ItemDto result = mapper.toItemDto(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toItemBooking - должен возвращать null при null Item")
    void toItemBooking_shouldReturnNull_whenItemIsNull() {
        var result = mapper.toItemBooking(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toItemInRequest - должен возвращать null при null Item")
    void toItemInRequest_shouldReturnNull_whenItemIsNull() {
        ItemInRequest result = mapper.toItemInRequest(null);

        assertThat(result).isNull();
    }


    @Test
    @DisplayName("newItemRequest - должен возвращать null при null request")
    void newItemRequest_shouldReturnNull_whenRequestIsNull() {
        Item result = mapper.newItemRequest(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("updateItemFromRequest - не должен менять item при null request")
    void updateItemFromRequest_shouldNotChangeItem_whenRequestIsNull() {
        Item item = new Item();
        item.setItemId(10L);
        item.setName("Старое имя");
        item.setDescription("Старое описание");
        item.setAvailable(true);

        mapper.updateItemFromRequest(item, null);

        assertThat(item.getItemId()).isEqualTo(10L);
        assertThat(item.getName()).isEqualTo("Старое имя");
        assertThat(item.getDescription()).isEqualTo("Старое описание");
        assertThat(item.getAvailable()).isTrue();
    }


    @Test
    @DisplayName("toItemList - должен маппить пустой список")
    void toItemList_shouldMapEmptyList() {
        List<ItemDto> result = mapper.toItemDtoList(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toItemDtoList - должен маппить пустой список")
    void toItemDtoList_shouldMapEmptyList() {
        List<ItemDto> result = mapper.toItemDtoList(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateItemFromRequest - должен игнорировать null поля в request")
    void updateItemFromRequest_shouldIgnoreNullFields() {
        Item item = new Item();
        item.setItemId(10L);
        item.setName("Старое имя");
        item.setDescription("Старое описание");
        item.setAvailable(true);

        UpdateItemRequest request = new UpdateItemRequest();

        mapper.updateItemFromRequest(item, request);

        assertThat(item.getName()).isEqualTo("Старое имя");
        assertThat(item.getDescription()).isEqualTo("Старое описание");
        assertThat(item.getAvailable()).isTrue();
    }

}