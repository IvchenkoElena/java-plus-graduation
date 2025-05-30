package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.enums.CommentStatus;

import java.util.List;

@FeignClient(name = "comment-service", path = "/internal/comments")
public interface CommentClient {
    @GetMapping("/{eventId}/{status}")
    List<CommentDto> getByEventIdAndStatus(@PathVariable final Long eventId,
                                           @PathVariable final CommentStatus status);

    @GetMapping("/{status}")
    List<CommentDto> getAllByEventIdInAndStatus(@RequestParam final List<Long> idsList,
                                                @PathVariable CommentStatus status);
}
