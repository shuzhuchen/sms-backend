package net.javaguides.sms_backend.exception;

import net.javaguides.sms_backend.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");

    @Test
    void handleResourceNotFoundReturnsNotFoundResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("missing"),
                request
        );

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("missing");
        assertThat(response.getBody().path()).isEqualTo("/test");
    }

    @Test
    void handleNameAggregationReturnsBadGatewayResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleNameAggregation(
                new NameAggregationException("downstream"),
                request
        );

        assertThat(response.getStatusCode().value()).isEqualTo(502);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Name aggregation downstream failed");
    }

    @Test
    void handleBadRequestReturnsInvalidBodyResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(new RuntimeException("bad"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Invalid request body");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void handleUnexpectedReturnsInternalServerErrorResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Unexpected server error");
    }

    @Test
    void nameAggregationExceptionSupportsCause() {
        RuntimeException cause = new RuntimeException("root");

        NameAggregationException exception = new NameAggregationException("failed", cause);

        assertThat(exception).hasMessage("failed").hasCause(cause);
    }
}
