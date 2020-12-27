package com.example.async.jaxrs_client.rx;

import com.example.async.jaxrs_client.Forecast;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@ApplicationScoped
public class WeatherForecast {

    private Client client;
    private List<WebTarget> targets;

    @Resource
    ManagedExecutorService mes;

    @PostConstruct
    private void initClient() {
        client = ClientBuilder.newClient();
        targets = asList(
                client.target("..."),
                client.target("...")
        );
    }

    public Forecast getAverageForecast() {
        return invokeTargetsAsync()
                .stream()
                .reduce((l, r) -> l.thenCombine(r, this::calculateAverage))
                .map(s -> s.toCompletableFuture().join())
                .orElseThrow(() -> new IllegalStateException("No weather service available"));
    }

    private List<CompletionStage<Forecast>> invokeTargetsAsync() {
        return targets.stream()
                .map(t -> t
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .rx()
                        .get(Forecast.class))
                .collect(Collectors.toList());
    }

    private Forecast calculateAverage(Forecast first, Forecast second) {
        // ...
        return first;
    }

    @PreDestroy
    public void closeClient() {
        client.close();
    }

}
