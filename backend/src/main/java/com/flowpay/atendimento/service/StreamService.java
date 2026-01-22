package com.flowpay.atendimento.service;

import com.flowpay.atendimento.enums.StreamEventType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface StreamService {
  SseEmitter subscribe();
  void publish(StreamEventType type, Object payload);
}
