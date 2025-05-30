package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.UserActionControllerGrpc;
import ru.practicum.grpc.stats.actions.UserActionProto;
import ru.practicum.handlers.ActionsHandlers;

@RequiredArgsConstructor

@Slf4j
@GrpcService
public class UserActionCollector extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final ActionsHandlers actionHandler;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            actionHandler.handle(request);
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            // в случае исключения отправляем ошибку клиенту
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));
        }
    }
}