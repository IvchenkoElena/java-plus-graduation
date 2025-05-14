package ru.practicum.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.ClientException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatClient {

    private final String http = "http";

    private String serverUri;
    private RestClient restClient;
    // эти поля нужно сделать final?

    @Autowired
            //(@Value("${STAT_SERVER_URI:http://stats-server:9090}")
            // почему-то пришлось туту менять путь
            // по-старому не работало
    public StatClient(@Value("${STAT_SERVER_URI:http://localhost:9090}") String serverUri) {
        this.serverUri = serverUri;
        this.restClient = RestClient.create();
    }


    public void saveHit(EndpointHitDto hitDto) {
        String uri = UriComponentsBuilder.newInstance()
                .uri(URI.create(serverUri))
                .path("/hit")
                .toUriString();

        restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(hitDto)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ClientException(
                            response.getStatusCode().value(),
                            response.getBody().toString()
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ClientException(
                            response.getStatusCode().value(),
                            response.getBody().toString()
                    );
                })
                .toBodilessEntity();
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String uriWithParams = UriComponentsBuilder.newInstance()
                .uri(URI.create(serverUri))
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .toUriString();

        return restClient.get()
                .uri(uriWithParams).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ClientException(
                            response.getStatusCode().value(),
                            response.getBody().toString()
                    );
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ClientException(
                            response.getStatusCode().value(),
                            response.getBody().toString()
                    );
                })
                .body(new ParameterizedTypeReference<>() {
                });
    }

}
