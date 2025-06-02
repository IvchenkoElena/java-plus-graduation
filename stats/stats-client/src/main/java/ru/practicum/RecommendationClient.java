package ru.practicum;

import com.google.common.collect.Lists;
//import io.grpc.Status;
//import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

//import java.util.Iterator;
import java.util.List;
//import java.util.Spliterator;
//import java.util.Spliterators;
//import java.util.stream.Stream;
//import java.util.stream.StreamSupport;

@Slf4j
@Service
public class RecommendationClient {
    @GrpcClient("analyzer")
    RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        return Lists.newArrayList(client.getInteractionsCount(request));
    }

    public List<RecommendedEventProto> getSimilarEvent(SimilarEventsRequestProto request) {
        return Lists.newArrayList(client.getSimilarEvents(request));
    }

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        return Lists.newArrayList(client.getRecommendationsForUser(request));
    }
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