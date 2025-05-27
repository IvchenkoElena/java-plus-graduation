package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.CommentClient;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.enums.CommentStatus;
import ru.practicum.service.CommentService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/internal/comments")
public class CommentInternalController implements CommentClient {
    private final CommentService commentService;

    @Override
    @GetMapping("/{eventId}/{status}")
    public List<CommentDto> getByEventIdAndStatus(@PathVariable final Long eventId,
                                                  @PathVariable final CommentStatus status) {
        return commentService.getByEventIdAndStatus(eventId, status);
    }

    @Override
    @GetMapping("/{status}")
    public List<CommentDto> getAllByEventIdInAndStatus(@RequestParam final List<Long> idsList,
                                                       @PathVariable CommentStatus status) {
        return commentService.getAllByEventIdInAndStatus(idsList, status);
    }
}
