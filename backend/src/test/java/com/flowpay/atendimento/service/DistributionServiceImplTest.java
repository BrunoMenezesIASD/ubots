package com.flowpay.atendimento.service;

import com.flowpay.atendimento.dto.response.DashboardSummaryResponse;
import com.flowpay.atendimento.entity.QueueItem;
import com.flowpay.atendimento.entity.ServiceRequest;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.StreamEventType;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.exception.BusinessRuleException;
import com.flowpay.atendimento.exception.NotFoundException;
import com.flowpay.atendimento.repository.AttendantRepository;
import com.flowpay.atendimento.repository.QueueItemRepository;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.impl.DistributionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributionServiceImplTest {

    @Mock private ServiceRequestRepository requestRepo;
    @Mock private QueueItemRepository queueRepo;
    @Mock private AttendantRepository attendantRepo;
    @Mock private StreamService streamService;
    @Mock private DashboardService dashboardService;

    @InjectMocks private DistributionServiceImpl service;

    @Test
    void createAndDistribute_whenHasAttendant_assignsAndDoesNotEnqueue_andPublishesEvents() {
        // arrange
        DashboardSummaryResponse summary = mock(DashboardSummaryResponse.class);
        when(dashboardService.getSummary()).thenReturn(summary);

        // save inicial (NEW)
        when(requestRepo.save(any(ServiceRequest.class))).thenAnswer(inv -> {
            ServiceRequest r = inv.getArgument(0);
            if (r.getId() == null) r.setId(100L);
            return r;
        });

        // tryAssign -> attendant disponível
        when(attendantRepo.lockBestAvailableAttendantId(eq(Team.CARTOES.name())))
                .thenReturn(Optional.of(10L));
        when(attendantRepo.findByIdForUpdate(10L))
                .thenReturn(Optional.of(com.flowpay.atendimento.entity.Attendant.builder().id(10L).name("A").team(Team.CARTOES).active(true).build()));

        // act
        ServiceRequest out = service.createAndDistribute("Cliente", "cartao");

        // assert
        assertNotNull(out);
        assertEquals(Team.CARTOES, out.getTeam());
        assertEquals(ServiceRequestStatus.ASSIGNED, out.getStatus());
        assertEquals(10L, out.getAssignedAttendantId());

        // enqueue não deve acontecer
        verify(queueRepo, never()).save(any());
        verify(streamService).publish(eq(StreamEventType.REQUEST_CREATED), any());
        verify(streamService).publish(eq(StreamEventType.REQUEST_ASSIGNED), any());
        verify(streamService, never()).publish(eq(StreamEventType.REQUEST_QUEUED), any());
        verify(streamService).publish(StreamEventType.DASHBOARD_UPDATED, summary);
    }

    @Test
    void createAndDistribute_whenNoAttendant_enqueues_andPublishesQueued() {
        // arrange
        DashboardSummaryResponse summary = mock(DashboardSummaryResponse.class);
        when(dashboardService.getSummary()).thenReturn(summary);

        when(requestRepo.save(any(ServiceRequest.class))).thenAnswer(inv -> {
            ServiceRequest r = inv.getArgument(0);
            if (r.getId() == null) r.setId(101L);
            return r;
        });

        // tryAssign -> vazio
        when(attendantRepo.lockBestAvailableAttendantId(eq(Team.OUTROS.name())))
                .thenReturn(Optional.empty());

        // act
        ServiceRequest out = service.createAndDistribute("Cliente", "assunto qualquer");

        // assert
        assertNotNull(out);
        assertEquals(Team.OUTROS, out.getTeam());
        assertEquals(ServiceRequestStatus.QUEUED, out.getStatus());
        assertNull(out.getAssignedAttendantId());

        verify(queueRepo).save(argThat(q ->
                q.getServiceRequestId().equals(101L) && q.getTeam() == Team.OUTROS
        ));

        verify(streamService).publish(eq(StreamEventType.REQUEST_CREATED), any());
        verify(streamService).publish(eq(StreamEventType.REQUEST_QUEUED), any());
        verify(streamService, never()).publish(eq(StreamEventType.REQUEST_ASSIGNED), any());
        verify(streamService).publish(StreamEventType.DASHBOARD_UPDATED, summary);
    }

    @Test
    void createAndDistribute_whenAttendantIdReturnedButAttendantNotFound_throwsNotFound() {
        // arrange
        when(requestRepo.save(any(ServiceRequest.class))).thenAnswer(inv -> {
            ServiceRequest r = inv.getArgument(0);
            if (r.getId() == null) r.setId(102L);
            return r;
        });

        when(attendantRepo.lockBestAvailableAttendantId(eq(Team.EMPRESTIMOS.name())))
                .thenReturn(Optional.of(77L));
        when(attendantRepo.findByIdForUpdate(77L)).thenReturn(Optional.empty());

        // act + assert
        assertThrows(NotFoundException.class, () -> service.createAndDistribute("Cliente", "emprestimo"));

        // created foi publicado antes do tryAssign falhar
        verify(streamService).publish(eq(StreamEventType.REQUEST_CREATED), any());

        // não deve enfileirar nem publicar dashboard
        verify(queueRepo, never()).save(any());
        verify(streamService, never()).publish(eq(StreamEventType.DASHBOARD_UPDATED), any());
        verify(streamService, never()).publish(eq(StreamEventType.REQUEST_QUEUED), any());
        verify(streamService, never()).publish(eq(StreamEventType.REQUEST_ASSIGNED), any());
    }

    @Test
    void finishAndRedistribute_whenRequestNotFound_throwsNotFound() {
        when(requestRepo.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.finishAndRedistribute(1L));
        verifyNoInteractions(streamService, dashboardService, queueRepo, attendantRepo);
    }

    @Test
    void finishAndRedistribute_whenAlreadyDone_returnsImmediately_noEvents() {
        ServiceRequest req = ServiceRequest.builder()
                .id(2L).team(Team.CARTOES).status(ServiceRequestStatus.DONE).build();
        when(requestRepo.findByIdForUpdate(2L)).thenReturn(Optional.of(req));

        ServiceRequest out = service.finishAndRedistribute(2L);

        assertSame(req, out);
        verify(requestRepo, never()).save(any());
        verifyNoInteractions(streamService, dashboardService, queueRepo, attendantRepo);
    }

    @Test
    void finishAndRedistribute_whenNotAssigned_throwsBusinessRule() {
        ServiceRequest req = ServiceRequest.builder()
                .id(3L).team(Team.CARTOES).status(ServiceRequestStatus.NEW).build();
        when(requestRepo.findByIdForUpdate(3L)).thenReturn(Optional.of(req));

        assertThrows(BusinessRuleException.class, () -> service.finishAndRedistribute(3L));

        verify(requestRepo, never()).save(any());
        verifyNoInteractions(streamService, dashboardService, queueRepo, attendantRepo);
    }

    @Test
    void finishAndRedistribute_whenAssigned_finishes_andRedistributesFromQueue_returnWhenNoCapacity() {
        // arrange: request a finalizar
        ServiceRequest finished = ServiceRequest.builder()
                .id(4L).team(Team.CARTOES).status(ServiceRequestStatus.ASSIGNED).assignedAttendantId(10L).build();
        when(requestRepo.findByIdForUpdate(4L)).thenReturn(Optional.of(finished));
        when(requestRepo.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        DashboardSummaryResponse summary = mock(DashboardSummaryResponse.class);
        when(dashboardService.getSummary()).thenReturn(summary);

        // fila: primeira iteração "não QUEUED" -> delete+continue
        // segunda iteração "QUEUED" -> tryAssign false -> return
        QueueItem qi1 = QueueItem.builder().id(1L).serviceRequestId(201L).team(Team.CARTOES).build();
        QueueItem qi2 = QueueItem.builder().id(2L).serviceRequestId(202L).team(Team.CARTOES).build();

        when(queueRepo.findNextByTeamForUpdateSkipLocked(Team.CARTOES.name()))
                .thenReturn(Optional.of(qi1))
                .thenReturn(Optional.of(qi2));

        ServiceRequest r1 = ServiceRequest.builder().id(201L).team(Team.CARTOES).status(ServiceRequestStatus.DONE).build();
        ServiceRequest r2 = ServiceRequest.builder().id(202L).team(Team.CARTOES).status(ServiceRequestStatus.QUEUED).build();

        when(requestRepo.findByIdForUpdate(201L)).thenReturn(Optional.of(r1));
        when(requestRepo.findByIdForUpdate(202L)).thenReturn(Optional.of(r2));

        // tryAssign do r2: sem vaga => return
        when(attendantRepo.lockBestAvailableAttendantId(Team.CARTOES.name()))
                .thenReturn(Optional.empty());

        // act
        ServiceRequest out = service.finishAndRedistribute(4L);

        // assert
        assertEquals(ServiceRequestStatus.DONE, out.getStatus());

        // qi1 deletado (status != QUEUED)
        verify(queueRepo).delete(qi1);

        // qi2 NÃO deletado (retornou ao não conseguir atribuir)
        verify(queueRepo, never()).delete(qi2);

        verify(streamService).publish(eq(StreamEventType.REQUEST_FINISHED), any());
        verify(streamService).publish(eq(StreamEventType.DASHBOARD_UPDATED), eq(summary));
    }


    @Test
    void finishAndRedistribute_whenAssigned_redistributesQueue_assignedDeletesAndContinuesUntilEmpty() {
        // Este segundo teste cobre o branch: QUEUED + tryAssign true => delete e continua até empty.

        // arrange
        ServiceRequest finished = ServiceRequest.builder()
                .id(5L).team(Team.CARTOES).status(ServiceRequestStatus.ASSIGNED).assignedAttendantId(10L).build();
        when(requestRepo.findByIdForUpdate(5L)).thenReturn(Optional.of(finished));
        when(requestRepo.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        DashboardSummaryResponse summary = mock(DashboardSummaryResponse.class);
        when(dashboardService.getSummary()).thenReturn(summary);

        QueueItem qi = QueueItem.builder().id(10L).serviceRequestId(301L).team(Team.CARTOES).build();
        when(queueRepo.findNextByTeamForUpdateSkipLocked(Team.CARTOES.name()))
                .thenReturn(Optional.of(qi))
                .thenReturn(Optional.empty());

        ServiceRequest queued = ServiceRequest.builder().id(301L).team(Team.CARTOES).status(ServiceRequestStatus.QUEUED).build();
        when(requestRepo.findByIdForUpdate(301L)).thenReturn(Optional.of(queued));

        when(attendantRepo.lockBestAvailableAttendantId(Team.CARTOES.name()))
                .thenReturn(Optional.of(99L));
        when(attendantRepo.findByIdForUpdate(99L))
                .thenReturn(Optional.of(com.flowpay.atendimento.entity.Attendant.builder().id(99L).name("A").team(Team.CARTOES).active(true).build()));

        // act
        ServiceRequest out = service.finishAndRedistribute(5L);

        // assert
        assertEquals(ServiceRequestStatus.DONE, out.getStatus());

        // assigned -> remove da fila
        verify(queueRepo).delete(qi);

        // publish do assigned do item enfileirado aconteceu
        verify(streamService).publish(eq(StreamEventType.REQUEST_ASSIGNED), any());
        verify(streamService).publish(eq(StreamEventType.REQUEST_FINISHED), any());
        verify(streamService).publish(eq(StreamEventType.DASHBOARD_UPDATED), eq(summary));
    }

    @Test
    void tryDistributeFromQueue_whenQueuedNotFound_throwsNotFound() {
        // arrange: finalizar request para chamar tryDistributeFromQueue
        ServiceRequest finished = ServiceRequest.builder()
                .id(6L).team(Team.CARTOES).status(ServiceRequestStatus.ASSIGNED).assignedAttendantId(1L).build();
        when(requestRepo.findByIdForUpdate(6L)).thenReturn(Optional.of(finished));
        when(requestRepo.save(any(ServiceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        QueueItem qi = QueueItem.builder().id(20L).serviceRequestId(999L).team(Team.CARTOES).build();
        when(queueRepo.findNextByTeamForUpdateSkipLocked(Team.CARTOES.name()))
                .thenReturn(Optional.of(qi));

        when(requestRepo.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // act + assert
        assertThrows(NotFoundException.class, () -> service.finishAndRedistribute(6L));
    }
}
