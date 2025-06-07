package ru.practicum.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import ru.practicum.ewm.stats.services.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;
import ru.practicum.handler.RecommendationsHandler;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationsHandler handler;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Запрос на получение рекомендаций: {}", request);
            handler.getRecommendationsForUser(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Запрос на получение похожих мероприятий для указанного: {}", request);
            handler.getSimilarEvents(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при получении похожих мероприятий: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            log.info("Запрос на получение суммы весов для каждого события: {}", request);
            handler.getInteractionsCount(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при получении суммы весов для мероприятий: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}