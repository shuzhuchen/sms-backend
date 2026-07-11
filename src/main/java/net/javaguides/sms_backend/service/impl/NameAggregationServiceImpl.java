package net.javaguides.sms_backend.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import net.javaguides.sms_backend.dto.NameAggregationRequest;
import net.javaguides.sms_backend.exception.NameAggregationException;
import net.javaguides.sms_backend.service.NameAggregationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class NameAggregationServiceImpl implements NameAggregationService {

    private final RestClient restClient;
    private final String downstreamUrl;
    private final String serviceName;

    public NameAggregationServiceImpl(@Value("${downstream.url}") String downstreamUrl,
                                      @Value("${aggregation.downstream.timeout:2s}") Duration downstreamTimeout,
                                      @Value("${aggregation.service-name:}") String serviceName) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(downstreamTimeout);
        requestFactory.setReadTimeout(downstreamTimeout);

        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.downstreamUrl = downstreamUrl;
        this.serviceName = serviceName;
    }

    @Override
    @Retry(name = "nameAggregationDownstream")
    @CircuitBreaker(name = "nameAggregationDownstream", fallbackMethod = "downgrade")
    public NameAggregationRequest forwardToNext(List<String> names) {
        NameAggregationRequest request = new NameAggregationRequest(buildLocalNames(names));
        NameAggregationRequest downstreamResponse = sendDownstream(request);

        return new NameAggregationRequest(appendDownstreamNames(request.getName(), downstreamResponse));
    }

    protected NameAggregationRequest sendDownstream(NameAggregationRequest request) {
        return restClient.post()
                .uri(downstreamUrl)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, httpResponse) -> {
                    throw new NameAggregationException("Downstream returned " + httpResponse.getStatusCode());
                })
                .body(NameAggregationRequest.class);
    }

    public NameAggregationRequest downgrade(List<String> names, Throwable failure) {
        return new NameAggregationRequest(buildLocalNames(names), "fallback-downstream unavailable");
    }

    private List<String> appendDownstreamNames(List<String> localNames, NameAggregationRequest downstreamResponse) {
        List<String> mergedNames = localNames == null ? new ArrayList<>() : new ArrayList<>(localNames);
        List<String> downstreamNames = downstreamResponse == null || downstreamResponse.getName() == null
                ? new ArrayList<>()
                : downstreamResponse.getName();

        for (String downstreamName : downstreamNames) {
            if (!mergedNames.contains(downstreamName)) {
                mergedNames.add(downstreamName);
            }
        }

        return mergedNames;
    }

    private List<String> buildLocalNames(List<String> names) {
        List<String> localNames = names == null ? new ArrayList<>() : new ArrayList<>(names);

        if (serviceName != null && !serviceName.isBlank() && !localNames.contains(serviceName)) {
            localNames.add(serviceName);
        }

        return localNames;
    }
}
