package ru.practicum.comment.dto;

import lombok.Data;
import ru.practicum.comment.enums.AdminUpdateCommentStatusAction;

@Data
public class AdminUpdateCommentStatusDto {
    private AdminUpdateCommentStatusAction action;
}
