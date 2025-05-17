package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.events.dto.EntityParam;
import ru.practicum.events.dto.EventDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.model.EventSort;
import ru.practicum.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventController {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String MAIN_SERVICE = "ewm-main-service";

    private final EventService eventService;
    private final StatClient statClient;


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
                                         @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable,
                                         HttpServletRequest request) {
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
        saveHit(request);
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
    public EventDto getEvent(@PathVariable Long id, HttpServletRequest request) {

        EventDto result = eventService.getEvent(id);
        saveHit(request);
        return result;
    }

    private void saveHit(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp(MAIN_SERVICE);
        endpointHitDto.setUri(request.getRequestURI());
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now());
        statClient.saveHit(endpointHitDto);
    }

}
