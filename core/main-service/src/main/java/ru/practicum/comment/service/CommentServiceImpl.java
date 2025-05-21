package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.AdminUpdateCommentStatusDto;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.enums.AdminUpdateCommentStatusAction;
import ru.practicum.comment.enums.CommentStatus;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.common.exception.NotFoundException;
import ru.practicum.common.exception.OperationForbiddenException;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.EventState;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;
    private final RequestRepository requestRepository;

    @Transactional
    @Override
    public CommentDto createComment(long authorId, long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID %s not found", authorId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID %s not found", eventId)));
        if (authorId == event.getInitiator().getId()) {
            throw new OperationForbiddenException("Инициатор мероприятия не может оставлять комментарии к нему");
        }
        if (!event.getState().equals(EventState.PUBLISHED) || !event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new OperationForbiddenException("Мероприятие должно быть опубликовано, а дата его проведения в прошлом");
        }
        if (requestRepository.findByRequesterIdAndEventIdAndStatus(authorId, eventId, RequestStatus.CONFIRMED).isEmpty()) {
            throw new OperationForbiddenException("Комментарии может оставлять только подтвержденный участник мероприятия");
        }
        Comment comment = commentMapper.toComment(newCommentDto, author, event);
        commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    @Transactional
    @Override
    public CommentDto updateComment(long authorId, long commentId, NewCommentDto updateCommentDto) {
        Comment commentToUpdate = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with ID %s not found", commentId)));
        if (authorId != commentToUpdate.getAuthor().getId()) {
            throw new OperationForbiddenException("Изменить комментарий может только его автор");
        }
        commentToUpdate.setText(updateCommentDto.getText());
        commentToUpdate.setStatus(CommentStatus.PENDING);

        commentRepository.save(commentToUpdate);
        return commentMapper.toDto(commentToUpdate);
    }

    @Transactional
    @Override
    public void deleteComment(long authorId, long commentId) {
        Comment commentToDelete = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with ID %s not found", commentId)));
        if (authorId != commentToDelete.getAuthor().getId()) {
            throw new OperationForbiddenException("Удалить комментарий может только его автор");
        }
        commentRepository.delete(commentToDelete);
    }

    @Transactional
    @Override
    public CommentDto adminUpdateCommentStatus(Long commentId, AdminUpdateCommentStatusDto updateCommentStatusDto) {
        Comment commentToUpdateStatus = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with ID %s not found", commentId)));
        if (!commentToUpdateStatus.getStatus().equals(CommentStatus.PENDING)) {
            throw new OperationForbiddenException("Can't reject not pending comment");
        }
        if (updateCommentStatusDto.getAction().equals(AdminUpdateCommentStatusAction.PUBLISH_COMMENT)) {
            commentToUpdateStatus.setStatus(CommentStatus.PUBLISHED);
        }
        if (updateCommentStatusDto.getAction().equals(AdminUpdateCommentStatusAction.REJECT_COMMENT)) {
            commentToUpdateStatus.setStatus(CommentStatus.REJECTED);
        }
        commentRepository.save(commentToUpdateStatus);
        return commentMapper.toDto(commentToUpdateStatus);
    }

    @Override
    public List<CommentDto> adminPendigCommentList() {
        return commentRepository.findAllByStatus(CommentStatus.PENDING)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }
}
