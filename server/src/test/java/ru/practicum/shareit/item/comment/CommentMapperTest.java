package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toDto_mapsFieldsAndAuthorName() {
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 12, 0);
        User author = User.builder().id(2L).name("Author").email("a@mail.com").build();
        Comment comment = Comment.builder()
                .id(5L).text("Great tool").author(author).created(created).build();

        CommentDto dto = CommentMapper.toDto(comment);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getText()).isEqualTo("Great tool");
        assertThat(dto.getAuthorName()).isEqualTo("Author");
        assertThat(dto.getCreated()).isEqualTo(created);
    }
}
