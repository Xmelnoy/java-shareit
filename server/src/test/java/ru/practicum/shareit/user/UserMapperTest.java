package ru.practicum.shareit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.NewUserRequest;
import ru.practicum.shareit.user.dto.UpdateUserRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Тесты маппера пользователей")
class UserMapperTest {

    @Autowired
    private UserMapper mapper;

    @Test
    @DisplayName("toUserEntity - должен маппить NewUserRequest в User")
    void toUserEntity_shouldMapCorrectly() {
        NewUserRequest request = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        User user = mapper.toUserEntity(request);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Иван Иванов");
        assertThat(user.getEmail()).isEqualTo("ivan@example.com");
        assertThat(user.getUserId()).isNull();
    }

    @Test
    @DisplayName("toUserEntity - должен возвращать null при null request")
    void toUserEntity_shouldReturnNull_whenRequestIsNull() {
        User user = mapper.toUserEntity(null);

        assertThat(user).isNull();
    }

    @Test
    @DisplayName("toUserDto - должен маппить User в UserDto")
    void toUserDto_shouldMapCorrectly() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Иван Иванов");
        user.setEmail("ivan@example.com");

        UserDto dto = mapper.toUserDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Иван Иванов");
        assertThat(dto.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    @DisplayName("toUserDto - должен возвращать null при null user")
    void toUserDto_shouldReturnNull_whenUserIsNull() {
        UserDto dto = mapper.toUserDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("updateUserFromRequest - должен обновлять только непустые поля")
    void updateUserFromRequest_shouldUpdateOnlyNonNullFields() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Старое имя");
        user.setEmail("old@example.com");

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Новое имя")
                .build();

        mapper.updateUserFromRequest(user, request);

        assertThat(user.getName()).isEqualTo("Новое имя");
        assertThat(user.getEmail()).isEqualTo("old@example.com");
        assertThat(user.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateUserFromRequest - должен обновлять все поля если они не null")
    void updateUserFromRequest_shouldUpdateAllFields() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Старое имя");
        user.setEmail("old@example.com");

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Новое имя")
                .email("new@example.com")
                .build();

        mapper.updateUserFromRequest(user, request);

        assertThat(user.getName()).isEqualTo("Новое имя");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("updateUserFromRequest - не должен менять ничего если request null")
    void updateUserFromRequest_shouldNotChangeAnything_whenRequestIsNull() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Старое имя");
        user.setEmail("old@example.com");

        mapper.updateUserFromRequest(user, null);

        assertThat(user.getName()).isEqualTo("Старое имя");
        assertThat(user.getEmail()).isEqualTo("old@example.com");
        assertThat(user.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateUserFromRequest - не должен менять поля если все поля в request null")
    void updateUserFromRequest_shouldNotChangeFields_whenAllFieldsAreNull() {
        User user = new User();
        user.setUserId(1L);
        user.setName("Старое имя");
        user.setEmail("old@example.com");

        UpdateUserRequest request = UpdateUserRequest.builder()
                .build();

        mapper.updateUserFromRequest(user, request);

        assertThat(user.getName()).isEqualTo("Старое имя");
        assertThat(user.getEmail()).isEqualTo("old@example.com");
    }
}