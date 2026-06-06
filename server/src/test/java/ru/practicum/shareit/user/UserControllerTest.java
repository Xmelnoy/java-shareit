package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto dto(Long id, String name, String email) {
        return UserDto.builder().id(id).name(name).email(email).build();
    }

    @Test
    void create_returns201() throws Exception {
        UserDto request = dto(null, "John", "john@mail.com");
        UserDto response = dto(1L, "John", "john@mail.com");
        when(userService.create(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@mail.com"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        UserDto request = dto(null, "", "john@mail.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        UserDto request = dto(null, "John", "not-an-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns200() throws Exception {
        UserDto request = UserDto.builder().name("New").build();
        UserDto response = dto(1L, "New", "john@mail.com");
        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(response);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void getById_returns200() throws Exception {
        when(userService.getById(1L)).thenReturn(dto(1L, "John", "john@mail.com"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userService.getById(anyLong())).thenThrow(new NotFoundException("not found"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_returns200() throws Exception {
        when(userService.getAll()).thenReturn(List.of(
                dto(1L, "A", "a@mail.com"),
                dto(2L, "B", "b@mail.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }
}
