package ru.practicum;

import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.stats.actions.UserActionProto;

@Slf4j
@Component
public class CollectorClient {
    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub client;
    public void sendUserAction(UserActionProto action) {
        Empty empty = client.collectUserAction(action);
    }
}