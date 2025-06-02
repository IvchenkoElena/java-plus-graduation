package ru.practicum.event.controller;

import com.google.protobuf.Timestamp;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.CollectorClient;
import ru.practicum.dto.event.EntityParam;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventRecommendationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.enums.EventSort;
import ru.practicum.event.service.EventService;
import ru.practicum.grpc.stats.actions.ActionTypeProto;
import ru.practicum.grpc.stats.actions.UserActionProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventController {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String MAIN_SERVICE = "ewm-main-service";

    private final EventService eventService;
    private final CollectorClient collectorClient;

    /**
     * Получение событий с возможностью фильтрации.
     * В выдаче - только опубликованные события.
     * Текстовый поиск (по аннотации и подробному описанию) - без учета регистра букв.
     * Если в запросе не указан диапазон дат [rangeStart-rangeEnd], то выгружаются события,
     * которые происходят позже текущей даты и времени.
     * Информация о каждом событии включает в себя количество просмотров и количество уже одобренных заявок на участие.
     * Информация о том, что по эндпоинту был осуществлен и обработан запрос, сохраняется в сервисе статистики.
     * В случае, если по заданным фильтрам не найдено ни одного события, возвращается пустой список.
     *
     * @param text          текст для поиска в содержимом аннотации и подробном описании события
     * @param sort          Вариант сортировки: по дате события или по количеству просмотров
     * @param from          количество событий, которые нужно пропустить для формирования текущего набора
     * @param size          количество событий в наборе
     * @param categories    список идентификаторов категорий в которых будет вестись поиск
     * @param rangeStart    дата и время не раньше которых должно произойти событие
     * @param rangeEnd      дата и время не позже которых должно произойти событие
     * @param paid          поиск только платных/бесплатных событий
     * @param onlyAvailable только события у которых не исчерпан лимит запросов на участие
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
                                         @RequestParam(required = false) EventSort sort,
                                         @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(required = false, defaultValue = "10") int size,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable) {
        EntityParam params = new EntityParam();
        params.setText(text);
        params.setSort(sort);
        params.setFrom(from);
        params.setSize(size);
        params.setCategories(categories);
        params.setRangeStart(rangeStart);
        params.setRangeEnd(rangeEnd);
        params.setPaid(paid);
        params.setOnlyAvailable(onlyAvailable);

        List<EventShortDto> result = eventService.getEvents(params);
        return result;
    }

    /**
     * Получение подробной информации об опубликованном событии по его идентификатору.
     * Cобытие должно быть опубликовано.
     * Информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов.
     * Информация о том, что по эндпоинту был осуществлен и обработан запрос, сохраняется в сервисе статистики.
     *
     * @param id id события
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEvent(@PathVariable Long id, @RequestHeader("X-EWM-USER-ID") long userId) {

        EventDto result = eventService.getPublishedEvent(id);
        collectorClient.sendUserAction(createUserAction(id, userId, ActionTypeProto.ACTION_VIEW, Instant.now()));
        return result;
    }

    private UserActionProto createUserAction(Long eventId, Long userId, ActionTypeProto type, Instant timestamp) {
        return UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(type)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build())
                .build();
    }

    @GetMapping("/recommendations")
    public List<EventRecommendationDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId) {
        return eventService.getRecommendations(userId);
    }

    @PutMapping("/{eventId}/like")
    public void addLike(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") long userId) {
        eventService.addLike(eventId, userId);
    }
}
