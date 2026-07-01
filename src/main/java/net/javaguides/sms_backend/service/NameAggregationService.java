package net.javaguides.sms_backend.service;

import net.javaguides.sms_backend.dto.NameAggregationRequest;

import java.util.List;

public interface NameAggregationService {

    NameAggregationRequest forwardToNext(List<String> names);
}
