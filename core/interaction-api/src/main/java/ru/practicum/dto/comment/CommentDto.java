package ru.practicum.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommentDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;
    private String text;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long eventId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long authorId;

    private String created;

    private String status;
}
