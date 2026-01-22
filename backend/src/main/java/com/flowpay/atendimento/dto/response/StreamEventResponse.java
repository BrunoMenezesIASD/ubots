package com.flowpay.atendimento.dto.response;

import com.flowpay.atendimento.enums.StreamEventType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class StreamEventResponse {
  StreamEventType type;
  Instant at;
  Object payload;
}
