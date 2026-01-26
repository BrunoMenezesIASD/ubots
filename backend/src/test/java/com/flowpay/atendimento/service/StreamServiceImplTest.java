package com.flowpay.atendimento.service;

import com.flowpay.atendimento.enums.StreamEventType;
import com.flowpay.atendimento.service.impl.StreamServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StreamServiceImplTest {

    @Test
    void subscribe_addsEmitterToInternalSet() throws Exception {
        StreamServiceImpl service = new StreamServiceImpl();

        SseEmitter emitter = service.subscribe();

        assertNotNull(emitter);

        Set<SseEmitter> emitters = getEmitters(service);
        assertTrue(emitters.contains(emitter), "subscribe() deve adicionar o emitter ao set interno");
    }

    @Test
    void publish_sendsEventToAllEmitters_successPath_doesNotRemove() throws Exception {
        StreamServiceImpl service = new StreamServiceImpl();

        SseEmitter e1 = mock(SseEmitter.class);
        SseEmitter e2 = mock(SseEmitter.class);

        Set<SseEmitter> emitters = getEmitters(service);
        emitters.add(e1);
        emitters.add(e2);

        service.publish(StreamEventType.REQUEST_CREATED, "payload");

        ArgumentCaptor<SseEmitter.SseEventBuilder> captor = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(e1).send(captor.capture());
        verify(e2).send(any(SseEmitter.SseEventBuilder.class));

        assertNotNull(captor.getValue());
        assertTrue(emitters.contains(e1));
        assertTrue(emitters.contains(e2));
    }

    @Test
    void publish_whenSendThrowsIOException_removesFailingEmitter_only() throws Exception {
        StreamServiceImpl service = new StreamServiceImpl();

        SseEmitter ok = mock(SseEmitter.class);
        SseEmitter failing = mock(SseEmitter.class);

        doThrow(new IOException("boom")).when(failing).send(any(SseEmitter.SseEventBuilder.class));

        Set<SseEmitter> emitters = getEmitters(service);
        emitters.add(ok);
        emitters.add(failing);

        service.publish(StreamEventType.DASHBOARD_UPDATED, 123);

        verify(ok).send(any(SseEmitter.SseEventBuilder.class));
        verify(failing).send(any(SseEmitter.SseEventBuilder.class));

        assertTrue(emitters.contains(ok), "emitter OK deve permanecer");
        assertFalse(emitters.contains(failing), "emitter que lançou IOException deve ser removido");
    }

    @Test
    void publish_withNoSubscribers_doesNothing() throws Exception {
        StreamServiceImpl service = new StreamServiceImpl();

        Set<SseEmitter> emitters = getEmitters(service);
        assertTrue(emitters.isEmpty());

        service.publish(StreamEventType.REQUEST_FINISHED, "x");

        assertTrue(emitters.isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static Set<SseEmitter> getEmitters(StreamServiceImpl service) throws Exception {
        Field f = StreamServiceImpl.class.getDeclaredField("emitters");
        f.setAccessible(true);
        return (Set<SseEmitter>) f.get(service);
    }
}
