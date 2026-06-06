package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    @Test
    void toEntity_setsFieldsAndCreated() {
        User requestor = User.builder().id(1L).name("John").email("j@mail.com").build();

        ItemRequest entity = ItemRequestMapper.toEntity("need a drill", requestor);

        assertThat(entity.getDescription()).isEqualTo("need a drill");
        assertThat(entity.getRequestor()).isEqualTo(requestor);
        assertThat(entity.getCreated()).isNotNull();
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toDto_mapsFieldsAndItems() {
        LocalDateTime created = LocalDateTime.now();
        ItemRequest request = ItemRequest.builder()
                .id(5L)
                .description("desc")
                .created(created)
                .build();
        ItemDto item = ItemDto.builder().id(1L).name("Drill")
                .description("d").available(true).requestId(5L).build();

        ItemRequestResponseDto dto = ItemRequestMapper.toDto(request, List.of(item));

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getDescription()).isEqualTo("desc");
        assertThat(dto.getCreated()).isEqualTo(created);
        assertThat(dto.getItems()).containsExactly(item);
    }

    @Test
    void toDto_emptyItems() {
        ItemRequest request = ItemRequest.builder()
                .id(6L).description("d").created(LocalDateTime.now()).build();

        ItemRequestResponseDto dto = ItemRequestMapper.toDto(request, List.of());

        assertThat(dto.getItems()).isEmpty();
    }
}
