package net.javaguides.sms_backend.controller;

import jakarta.validation.Valid;
import net.javaguides.sms_backend.dto.NameAggregationRequest;
import net.javaguides.sms_backend.service.NameAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/name")
public class NameAggregationController {

    private final NameAggregationService nameAggregationService;

    public NameAggregationController(NameAggregationService nameAggregationService) {
        this.nameAggregationService = nameAggregationService;
    }

    @PostMapping("/aggregation")
    public ResponseEntity<NameAggregationRequest> aggregate(@Valid @RequestBody NameAggregationRequest request) {
        return ResponseEntity.ok(nameAggregationService.forwardToNext(request.getName()));
    }
}
