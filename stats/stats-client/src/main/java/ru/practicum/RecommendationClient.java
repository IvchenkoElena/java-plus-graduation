package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.services.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RecommendationClient {
    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public List<RecommendedEventProto> getRecommendations(Long userId, Integer maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        log.info("Запрос на получение рекомендаций: {}", request);
        List<RecommendedEventProto> result = new ArrayList<>();
        client.getRecommendationsForUser(request)
                .forEachRemaining(result::add);
        log.info("Резйльтат получения рекомендаций: {}", result);
        return result;
    }

    public List<RecommendedEventProto> getSimilarEvents(Long eventId, Long userId, Integer maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        log.info("Запрос на получение похожих мероприятий для указанного: {}", request);
        List<RecommendedEventProto> result = new ArrayList<>();
        client.getSimilarEvents(request)
                .forEachRemaining(result::add);
        log.info("Результат получения похожих мероприятий для указанного: {}", request);
        return result;
    }

    public Map<Long, Double> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        log.info("Запрос на получение суммы весов для каждого события: {}", request);
        Map<Long, Double> result = new HashMap<>();
        client.getInteractionsCount(request)
                .forEachRemaining(e -> result.put(e.getEventId(), e.getScore()));
        log.info("Результат получения суммы весов для каждого события: {}", request);
        return result;
    }

//    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
//        return Lists.newArrayList(client.getInteractionsCount(request));
//    }
//
//    public List<RecommendedEventProto> getSimilarEvent(SimilarEventsRequestProto request) {
//        return Lists.newArrayList(client.getSimilarEvents(request));
//    }
//
//    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
//        return Lists.newArrayList(client.getRecommendationsForUser(request));
//    }
}


    /*private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerStub;

    public Stream<RecommendedEventProto> getRecommendedEventsForUser(
            long userId, int size) {

        try {
            log.info("Клиент GPRC. Метод getRecommendedEventsForUser(). UserId: {}, size: {}", userId, size);
            UserPredictionsRequestProto predictionsRequestProto =
                    UserPredictionsRequestProto.newBuilder()
                            .setUserId(userId)
                            .setMaxResults(size)
                            .build();
            Iterator<RecommendedEventProto> responseIterator =
                    analyzerStub.getRecommendationsForUser(predictionsRequestProto);
            Stream<RecommendedEventProto> result = asStream(responseIterator);

            // log.info("Recommendations get: {}", result.toList());
            return result;
        } catch (Exception e) {
            log.error("Error sending UserPredictionsRequestProto: userId {}, size {}", userId, size, e);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getSimilarEvent(
            SimilarEventsRequestProto similarEventsRequestProto) {
        try {
            log.info("Клиент GPRC. Получение similarEvents: {}", similarEventsRequestProto);
            Iterator<RecommendedEventProto> responseIterator =
                    analyzerStub.getSimilarEvents(similarEventsRequestProto);
            Stream<RecommendedEventProto> result = asStream(responseIterator);
            log.info("SimilarEvents get: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error sending similarEventsRequestProto: {}", similarEventsRequestProto, e);
            return Stream.empty();
        }
    }

    public Stream<RecommendedEventProto> getInteractionsCount(Long eventId) {
        try {
            log.info("Клиент GPRC. Получение InteractionsCount: {}", eventId);

            InteractionsCountRequestProto interactionsCountRequestProto = InteractionsCountRequestProto.newBuilder()
                    .addEventId(eventId)
                    .build();
            log.info("Клиент GPRC. interactionsCountRequestProto: {}", interactionsCountRequestProto);
            Iterator<RecommendedEventProto> responseIterator = analyzerStub.getInteractionsCount(interactionsCountRequestProto);
            log.info("Клиент GPRC. responseIterator {}", responseIterator);
            Stream<RecommendedEventProto> result = asStream(responseIterator);
            // log.info("InteractionsCount get: {}", result.toList());
            return result;
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                log.error("Error sending {}", e.getStatus());
                return Stream.empty();
            }
        } catch (
                Exception e) {
            log.error("Непредвиденная ошибка {}", e.getMessage());
            return Stream.empty();
        }
        log.info("Клиент GPRC. Отправили пустой поток");
        return Stream.empty();
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }

}*/