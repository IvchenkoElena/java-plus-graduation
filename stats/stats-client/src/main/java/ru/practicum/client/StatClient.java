package ru.practicum.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStats;
import ru.practicum.exception.ClientException;
import ru.practicum.exception.StatsServerUnavailable;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StatClient {
    private final DiscoveryClient discoveryClient;
    @Value("${statsServiceId}")
    private String statsServiceId;

    @Autowired
    public StatClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId, exception
            );
        }
    }

    public void saveHit(EndpointHitDto hitDto) {
        ServiceInstance instance = getInstance();

        String uri = UriComponentsBuilder.newInstance()
                .uri(URI.create("http://" + instance.getHost() + ":" + instance.getPort()))
                .path("/hit")
                .toUriString();

        RestClient restClient = RestClient.builder().baseUrl(uri).build();

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
        ServiceInstance instance = getInstance();

        String uriWithParams = UriComponentsBuilder.newInstance()
                .uri(URI.create("http://" + instance.getHost() + ":" + instance.getPort()))
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .toUriString();

        RestClient restClient = RestClient.builder().baseUrl(uriWithParams).build();

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
