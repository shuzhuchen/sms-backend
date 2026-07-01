package net.javaguides.sms_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NameAggregationRequest {
    @NotEmpty(message = "name must contain at least one value")
    private List<@NotBlank(message = "name values must not be blank") String> name;
    private String fallbackReason;

    public NameAggregationRequest(List<String> name) {
        this.name = name;
    }

    public NameAggregationRequest(List<String> name, String fallbackReason) {
        this.name = name;
        this.fallbackReason = fallbackReason;
    }
}
