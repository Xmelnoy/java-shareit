package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toDto_mapsAllFields() {
        User user = User.builder().id(1L).name("John").email("john@mail.com").build();

        UserDto dto = UserMapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John");
        assertThat(dto.getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void toEntity_mapsAllFields() {
        UserDto dto = UserDto.builder().id(2L).name("Jane").email("jane@mail.com").build();

        User user = UserMapper.toEntity(dto);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getName()).isEqualTo("Jane");
        assertThat(user.getEmail()).isEqualTo("jane@mail.com");
    }
}
