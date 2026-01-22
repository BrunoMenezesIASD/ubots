package com.flowpay.atendimento.service.impl;

import com.flowpay.atendimento.dto.response.StreamEventResponse;
import com.flowpay.atendimento.enums.StreamEventType;
import com.flowpay.atendimento.service.StreamService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StreamServiceImpl implements StreamService {

  private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();

  @Override
  public SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(0L);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError(e -> emitters.remove(emitter));
    return emitter;
  }

  @Override
  public void publish(StreamEventType type, Object payload) {
    StreamEventResponse event = StreamEventResponse.builder()
      .type(type)
      .at(Instant.now())
      .payload(payload)
      .build();

    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().name(type.name()).data(event));
      } catch (IOException e) {
        emitters.remove(emitter);
      }
    }
  }
}
