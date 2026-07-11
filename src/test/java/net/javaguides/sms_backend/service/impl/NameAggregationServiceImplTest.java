package net.javaguides.sms_backend.service.impl;

import net.javaguides.sms_backend.dto.NameAggregationRequest;
import net.javaguides.sms_backend.exception.NameAggregationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class NameAggregationServiceImplTest {

    @Test
    void forwardToNextReturnsLocalNamesThenUniqueDownstreamNames() {
        TestNameAggregationService service = new TestNameAggregationService(
                new NameAggregationRequest(List.of("Alice", "Bob", "Suzy")),
                ""
        );

        NameAggregationRequest response = service.forwardToNext(List.of("Suzy"));

        assertThat(response.getName()).containsExactly("Suzy", "Alice", "Bob");
        assertThat(response.getFallbackReason()).isNull();
        assertThat(service.sentRequest().getName()).containsExactly("Suzy");
    }

    @Test
    void forwardToNextAppendsConfiguredServiceNameBeforeCallingDownstream() {
        TestNameAggregationService service = new TestNameAggregationService(
                new NameAggregationRequest(List.of("Alice")),
                "Backend"
        );

        NameAggregationRequest response = service.forwardToNext(List.of("Jude"));

        assertThat(service.sentRequest().getName()).containsExactly("Jude", "Backend");
        assertThat(response.getName()).containsExactly("Jude", "Backend", "Alice");
    }

    @Test
    void forwardToNextDoesNotDuplicateConfiguredServiceName() {
        TestNameAggregationService service = new TestNameAggregationService(
                new NameAggregationRequest(List.of("Alice")),
                "Backend"
        );

        NameAggregationRequest response = service.forwardToNext(List.of("Backend"));

        assertThat(service.sentRequest().getName()).containsExactly("Backend");
        assertThat(response.getName()).containsExactly("Backend", "Alice");
    }

    @Test
    void forwardToNextAcceptsEmptyInputList() {
        TestNameAggregationService service = new TestNameAggregationService(
                new NameAggregationRequest(List.of("Alice")),
                ""
        );

        NameAggregationRequest response = service.forwardToNext(List.of());

        assertThat(service.sentRequest().getName()).isEmpty();
        assertThat(response.getName()).containsExactly("Alice");
    }

    @Test
    void forwardToNextAcceptsNullNamesAsEmptyLocalList() {
        TestNameAggregationService service = new TestNameAggregationService(
                new NameAggregationRequest(List.of("Alice")),
                ""
        );

        NameAggregationRequest response = service.forwardToNext(null);

        assertThat(service.sentRequest().getName()).isEmpty();
        assertThat(response.getName()).containsExactly("Alice");
    }

    @Test
    void forwardToNextTreatsNullDownstreamResponseAsNoAdditionalNames() {
        TestNameAggregationService service = new TestNameAggregationService((NameAggregationRequest) null, "Backend");

        NameAggregationRequest response = service.forwardToNext(List.of("Alice"));

        assertThat(response.getName()).containsExactly("Alice", "Backend");
    }

    @Test
    void forwardToNextTreatsNullDownstreamNamesAsNoAdditionalNames() {
        TestNameAggregationService service = new TestNameAggregationService(new NameAggregationRequest(null), "");

        NameAggregationRequest response = service.forwardToNext(List.of("Alice"));

        assertThat(response.getName()).containsExactly("Alice");
    }

    @Test
    void forwardToNextPropagatesDownstreamFailureForResilience4jToHandle() {
        TestNameAggregationService service = new TestNameAggregationService(
                new NameAggregationException("Downstream returned 500"),
                ""
        );

        assertThatThrownBy(() -> service.forwardToNext(List.of("Alice")))
                .isInstanceOf(NameAggregationException.class)
                .hasMessage("Downstream returned 500");
    }

    @Test
    void downgradeReturnsLocalResponseWithFallbackReason() {
        TestNameAggregationService service = new TestNameAggregationService((NameAggregationRequest) null, "Backend");

        NameAggregationRequest response = service.downgrade(List.of("Suzy"), new RuntimeException("Read timed out"));

        assertThat(response.getName()).containsExactly("Suzy", "Backend");
        assertThat(response.getFallbackReason()).isEqualTo("fallback-downstream unavailable");
    }

    @Test
    void forwardToNextSendsRealDownstreamRequestAndMergesResponse() throws IOException {
        HttpServer server = httpServer(200, """
                {"name":["Remote","Alice"]}
                """);

        try {
            NameAggregationServiceImpl service = new NameAggregationServiceImpl(
                    url(server),
                    Duration.ofMillis(500),
                    "Backend"
            );

            NameAggregationRequest response = service.forwardToNext(List.of("Alice"));

            assertThat(response.getName()).containsExactly("Alice", "Backend", "Remote");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void forwardToNextThrowsNameAggregationExceptionWhenDownstreamReturnsError() throws IOException {
        HttpServer server = httpServer(503, """
                {"message":"unavailable"}
                """);

        try {
            NameAggregationServiceImpl service = new NameAggregationServiceImpl(
                    url(server),
                    Duration.ofMillis(500),
                    ""
            );

            assertThatThrownBy(() -> service.forwardToNext(List.of("Alice")))
                    .isInstanceOf(NameAggregationException.class)
                    .hasMessageContaining("Downstream returned 503");
        } finally {
            server.stop(0);
        }
    }

    private static HttpServer httpServer(int status, String responseBody) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/name/aggregation", exchange -> respond(exchange, status, responseBody));
        server.start();
        return server;
    }

    private static void respond(HttpExchange exchange, int status, String responseBody) throws IOException {
        byte[] body = responseBody.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream response = exchange.getResponseBody()) {
            response.write(body);
        }
    }

    private static String url(HttpServer server) {
        return "http://localhost:" + server.getAddress().getPort() + "/name/aggregation";
    }

    private static class TestNameAggregationService extends NameAggregationServiceImpl {
        private final NameAggregationRequest downstreamResponse;
        private final RuntimeException downstreamFailure;
        private NameAggregationRequest sentRequest;

        private TestNameAggregationService(NameAggregationRequest downstreamResponse, String serviceName) {
            super("http://localhost:8081/name/aggregation", Duration.ofMillis(100), serviceName);
            this.downstreamResponse = downstreamResponse;
            this.downstreamFailure = null;
        }

        private TestNameAggregationService(RuntimeException downstreamFailure, String serviceName) {
            super("http://localhost:8081/name/aggregation", Duration.ofMillis(100), serviceName);
            this.downstreamResponse = null;
            this.downstreamFailure = downstreamFailure;
        }

        private NameAggregationRequest sentRequest() {
            return sentRequest;
        }

        @Override
        protected NameAggregationRequest sendDownstream(NameAggregationRequest request) {
            this.sentRequest = request;

            if (downstreamFailure != null) {
                throw downstreamFailure;
            }

            return downstreamResponse;
        }
    }
}
