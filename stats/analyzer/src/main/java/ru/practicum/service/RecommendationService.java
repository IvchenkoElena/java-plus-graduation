package ru.practicum.service;

import ru.practicum.grpc.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.recommendation.RecommendedEventProto;
import ru.practicum.grpc.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.recommendation.UserPredictionsRequestProto;

import java.util.List;

public interface RecommendationService {
    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto eventsRequestProto);

    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto requestProto);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}