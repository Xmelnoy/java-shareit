package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.NewCommentRequest;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@SpringBootTest
@DisplayName("Тесты маппера комментариев")
class CommentMapperTest {

    @Autowired
    private CommentMapper mapper;

    @Autowired
    private ItemMapper itemMapper;

    @Test
    @DisplayName("toCommentEntity - должен маппить NewCommentRequest в Comment")
    void toCommentEntity_shouldMapCorrectly() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Отличная вещь!");

        Comment comment = mapper.toCommentEntity(request);

        assertThat(comment).isNotNull();
        assertThat(comment.getText()).isEqualTo("Отличная вещь!");
        assertThat(comment.getId()).isNull();
        assertThat(comment.getCreated()).isNull();
        assertThat(comment.getItem()).isNull();
        assertThat(comment.getCommentator()).isNull();
    }

    @Test
    @DisplayName("toCommentEntity - должен возвращать null при null request")
    void toCommentEntity_shouldReturnNull_whenRequestIsNull() {
        Comment comment = mapper.toCommentEntity(null);

        assertThat(comment).isNull();
    }

    @Test
    @DisplayName("toCommentDto - должен маппить Comment в CommentDto")
    void toCommentDto_shouldMapCorrectly() {
        User commentator = new User();
        commentator.setUserId(2L);
        commentator.setName("Бронер");
        commentator.setEmail("booker@test.com");

        Item item = new Item();
        item.setItemId(10L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Отличная вещь!");
        comment.setCreated(LocalDateTime.of(2026, 12, 5, 12, 0));
        comment.setCommentator(commentator);
        comment.setItem(item);

        CommentDto dto = mapper.toCommentDto(comment);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getText()).isEqualTo("Отличная вещь!");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 12, 5, 12, 0));
        assertThat(dto.getAuthorName()).isEqualTo("Бронер");
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(10L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("toCommentDto - должен возвращать null при null comment")
    void toCommentDto_shouldReturnNull_whenCommentIsNull() {
        CommentDto dto = mapper.toCommentDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toCommentDto - должен маппить comment с null commentator")
    void toCommentDto_shouldMapCommentWithNullCommentator() {
        Item item = new Item();
        item.setItemId(10L);
        item.setName("Дрель");

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Отличная вещь!");
        comment.setCreated(LocalDateTime.of(2026, 12, 5, 12, 0));
        comment.setCommentator(null);
        comment.setItem(item);

        CommentDto dto = mapper.toCommentDto(comment);

        assertThat(dto).isNotNull();
        assertThat(dto.getAuthorName()).isNull();
        assertThat(dto.getItem()).isNotNull();
    }

    @Test
    @DisplayName("toCommentDto - должен маппить comment с null item")
    void toCommentDto_shouldMapCommentWithNullItem() {
        User commentator = new User();
        commentator.setUserId(2L);
        commentator.setName("Бронер");

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Отличная вещь!");
        comment.setCreated(LocalDateTime.of(2026, 12, 5, 12, 0));
        comment.setCommentator(commentator);
        comment.setItem(null);

        CommentDto dto = mapper.toCommentDto(comment);

        assertThat(dto).isNotNull();
        assertThat(dto.getItem()).isNull();
    }
}