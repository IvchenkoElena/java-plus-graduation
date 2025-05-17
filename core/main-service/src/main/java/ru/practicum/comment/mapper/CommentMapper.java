package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.events.model.Event;
import ru.practicum.user.model.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", source = "author")
    @Mapping(target = "event", source = "event")
    Comment toComment(NewCommentDto newCommentDto, User author, Event event);

    @Mapping(target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventId", expression = "java(comment.getEvent().getId())")
    @Mapping(target = "authorId", expression = "java(comment.getAuthor().getId())")
    @Mapping(target = "status", expression = "java(comment.getStatus().name())")
    CommentDto toDto(Comment comment);

}
