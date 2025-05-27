package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "eventId", source = "eventId")
    Comment toComment(NewCommentDto newCommentDto, long authorId, long eventId);

    @Mapping(target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "eventId", expression = "java(comment.getEventId())")
    @Mapping(target = "authorId", expression = "java(comment.getAuthorId())")
    @Mapping(target = "status", expression = "java(comment.getStatus().name())")
    CommentDto toDto(Comment comment);

}
