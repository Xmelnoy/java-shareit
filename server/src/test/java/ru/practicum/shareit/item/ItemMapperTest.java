package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toDto_withRequest() {
        ItemRequest request = ItemRequest.builder().id(7L).build();
        Item item = Item.builder()
                .id(1L).name("Drill").description("Powerful").available(true)
                .owner(User.builder().id(1L).build())
                .request(request)
                .build();

        ItemDto dto = ItemMapper.toDto(item);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Powerful");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(7L);
    }

    @Test
    void toDto_withoutRequest() {
        Item item = Item.builder()
                .id(2L).name("Saw").description("Sharp").available(false).build();

        ItemDto dto = ItemMapper.toDto(item);

        assertThat(dto.getRequestId()).isNull();
        assertThat(dto.getAvailable()).isFalse();
    }

    @Test
    void toEntity_mapsFields() {
        ItemDto dto = ItemDto.builder()
                .id(3L).name("Hammer").description("Heavy").available(true).requestId(9L).build();

        Item item = ItemMapper.toEntity(dto);

        assertThat(item.getId()).isEqualTo(3L);
        assertThat(item.getName()).isEqualTo("Hammer");
        assertThat(item.getDescription()).isEqualTo("Heavy");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getOwner()).isNull();
        assertThat(item.getRequest()).isNull();
    }
}
