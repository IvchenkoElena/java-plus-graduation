package ru.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.client.EventClient;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.comment.AdminUpdateCommentStatusDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.enums.AdminUpdateCommentStatusAction;
import ru.practicum.dto.comment.enums.CommentStatus;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.request.enums.RequestStatus;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.repository.CommentRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.OperationForbiddenException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestClient requestClient;

    @Transactional
    @Override
    public CommentDto createComment(long authorId, long eventId, NewCommentDto newCommentDto) {
        checkBeforeCreate(authorId,eventId);

        Comment comment = commentMapper.toComment(newCommentDto, authorId, eventId);
        commentRepository.save(comment);
        return commentMapper.toDto(comment);
    }

    public void checkBeforeCreate(long authorId, long eventId) {
        try {
            if(!userClient.exists(authorId)) {
                throw new NotFoundException(String.format("User with ID %s not found", authorId));
            }
            EventDto eventDto = eventClient.getById(eventId);
            if (authorId == eventDto.getInitiator()) {
                throw new OperationForbiddenException("Инициатор мероприятия не может оставлять комментарии к нему");
            }
            if (!eventDto.getState().equals(EventState.PUBLISHED) ||
                    !LocalDateTime.parse(eventDto.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isBefore(LocalDateTime.now())) {
                throw new OperationForbiddenException("Мероприятие должно быть опубликовано, а дата его проведения в прошлом");
            }
            if (requestClient.getByRequesterIdAndEventIdAndStatus(authorId, eventId, RequestStatus.CONFIRMED) == null) {
                throw new OperationForbiddenException("Комментарии может оставлять только подтвержденный участник мероприятия");
            }
        } catch (FeignException e) {
            if (e.status() == 409) {
                log.warn("Ошибка клиента:" + e.getMessage());// почему-то не логигируется этот случай по факту
                throw new OperationForbiddenException(e.getMessage());
            } else if (e.status() == 404) {
                log.warn("Ошибка клиента:" + e.getMessage());// и этот тоже
                throw new NotFoundException(e.getMessage());
            } else {
                log.warn("Ошибка клиента:" + e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    @Transactional
    @Override
    public CommentDto updateComment(long authorId, long commentId, NewCommentDto updateCommentDto) {
        Comment commentToUpdate = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with ID %s not found", commentId)));
        if (authorId != commentToUpdate.getAuthorId()) {
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
        if (authorId != commentToDelete.getAuthorId()) {
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

    @Override
    public List<CommentDto> getByEventIdAndStatus(Long eventId, CommentStatus status) {
        return commentRepository.findByEventIdAndStatus(eventId, status)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public List<CommentDto> getAllByEventIdInAndStatus(List<Long> idsList, CommentStatus status) {
        return commentRepository.findAllByEventIdInAndStatus(idsList, status)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }
}
