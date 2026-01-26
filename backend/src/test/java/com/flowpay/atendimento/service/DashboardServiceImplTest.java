package com.flowpay.atendimento.service;

import com.flowpay.atendimento.dto.response.AttendantResponse;
import com.flowpay.atendimento.dto.response.DashboardSummaryResponse;
import com.flowpay.atendimento.entity.Attendant;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.repository.AttendantRepository;
import com.flowpay.atendimento.repository.QueueItemRepository;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private ServiceRequestRepository requestRepo;
    @Mock private QueueItemRepository queueRepo;
    @Mock private AttendantRepository attendantRepo;

    @InjectMocks private DashboardServiceImpl service;

    @Test
    void getSummary_whenReposReturnEmptyGroups_initializesZeros_andAttendantsEmpty() {
        // arrange
        when(requestRepo.countGroupedByStatus()).thenReturn(List.of());
        when(requestRepo.countGroupedByTeamAndStatus()).thenReturn(List.of());
        when(attendantRepo.findAll()).thenReturn(List.of());

        // queue size (uma chamada por team)
        for (Team t : Team.values()) {
            when(queueRepo.countByTeam(t)).thenReturn(0L);
        }

        // act
        DashboardSummaryResponse resp = service.getSummary();

        // assert
        assertNotNull(resp);

        // totalByStatus: todos status presentes e 0
        assertNotNull(resp.getTotalByStatus());
        assertEquals(ServiceRequestStatus.values().length, resp.getTotalByStatus().size());
        for (ServiceRequestStatus s : ServiceRequestStatus.values()) {
            assertEquals(0L, resp.getTotalByStatus().get(s));
        }

        // queueSizeByTeam: todos teams presentes e 0
        assertNotNull(resp.getQueueSizeByTeam());
        assertEquals(Team.values().length, resp.getQueueSizeByTeam().size());
        for (Team t : Team.values()) {
            assertEquals(0L, resp.getQueueSizeByTeam().get(t));
            verify(queueRepo).countByTeam(t);
        }

        // totalByTeamAndStatus: todos teams e todos status inicializados com 0
        assertNotNull(resp.getTotalByTeamAndStatus());
        assertEquals(Team.values().length, resp.getTotalByTeamAndStatus().size());
        for (Team t : Team.values()) {
            Map<ServiceRequestStatus, Long> inner = resp.getTotalByTeamAndStatus().get(t);
            assertNotNull(inner);
            assertEquals(ServiceRequestStatus.values().length, inner.size());
            for (ServiceRequestStatus s : ServiceRequestStatus.values()) {
                assertEquals(0L, inner.get(s));
            }
        }

        // attendants vazio
        assertNotNull(resp.getAttendants());
        assertTrue(resp.getAttendants().isEmpty());

        verify(requestRepo).countGroupedByStatus();
        verify(requestRepo).countGroupedByTeamAndStatus();
        verify(attendantRepo).findAll();
        verifyNoMoreInteractions(queueRepo, requestRepo, attendantRepo);
    }

    @Test
    void getSummary_whenReposReturnGroupedData_overridesZeros_andComputesRemainingCapacityBranches() {
        // arrange
        // countGroupedByStatus: sobrescreve alguns status (branch: loop sobrescrevendo)
        when(requestRepo.countGroupedByStatus()).thenReturn(List.of(
                new Object[]{ServiceRequestStatus.NEW, 2L},
                new Object[]{ServiceRequestStatus.ASSIGNED, 5L}
        ));

        // queue sizes (não-zero para garantir que foi setado)
        for (Team t : Team.values()) {
            when(queueRepo.countByTeam(t)).thenReturn(0L);
        }
        when(queueRepo.countByTeam(Team.CARTOES)).thenReturn(7L);

        // countGroupedByTeamAndStatus: sobrescreve entradas específicas do mapa aninhado
        when(requestRepo.countGroupedByTeamAndStatus()).thenReturn(List.of(
                new Object[]{Team.CARTOES, ServiceRequestStatus.NEW, 3L},
                new Object[]{Team.OUTROS, ServiceRequestStatus.DONE, 9L}
        ));

        // attendants: dois atendentes pra cobrir Math.max (remaining >0 e remaining ==0)
        Attendant a1 = Attendant.builder().id(10L).name("A1").team(Team.CARTOES).active(true).build();
        Attendant a2 = Attendant.builder().id(11L).name("A2").team(Team.OUTROS).active(true).build();
        when(attendantRepo.findAll()).thenReturn(List.of(a1, a2));

        when(requestRepo.countByAttendantAndStatus(10L, ServiceRequestStatus.ASSIGNED)).thenReturn(1L); // remaining 2
        when(requestRepo.countByAttendantAndStatus(11L, ServiceRequestStatus.ASSIGNED)).thenReturn(4L); // remaining 0

        // act
        DashboardSummaryResponse resp = service.getSummary();

        // assert
        assertNotNull(resp);

        // totalByStatus: NEW=2, ASSIGNED=5 e os demais continuam 0
        assertEquals(2L, resp.getTotalByStatus().get(ServiceRequestStatus.NEW));
        assertEquals(5L, resp.getTotalByStatus().get(ServiceRequestStatus.ASSIGNED));
        assertEquals(0L, resp.getTotalByStatus().get(ServiceRequestStatus.QUEUED));
        assertEquals(0L, resp.getTotalByStatus().get(ServiceRequestStatus.DONE));

        // queueSizeByTeam: CARTOES=7 e demais 0
        assertEquals(7L, resp.getQueueSizeByTeam().get(Team.CARTOES));
        assertEquals(0L, resp.getQueueSizeByTeam().get(Team.EMPRESTIMOS));
        assertEquals(0L, resp.getQueueSizeByTeam().get(Team.OUTROS));

        // totalByTeamAndStatus: sobrescritos
        assertEquals(3L, resp.getTotalByTeamAndStatus().get(Team.CARTOES).get(ServiceRequestStatus.NEW));
        assertEquals(9L, resp.getTotalByTeamAndStatus().get(Team.OUTROS).get(ServiceRequestStatus.DONE));
        // e um que deve permanecer 0
        assertEquals(0L, resp.getTotalByTeamAndStatus().get(Team.CARTOES).get(ServiceRequestStatus.DONE));

        // attendants: verifica activeAssignments e remainingCapacity (branch Math.max)
        List<AttendantResponse> attendants = resp.getAttendants();
        assertEquals(2, attendants.size());

        AttendantResponse r1 = attendants.get(0);
        AttendantResponse r2 = attendants.get(1);

        // como o stream mantém ordem da lista do repo
        assertEquals(10L, r1.getId());
        assertEquals(1L, r1.getActiveAssignments());
        assertEquals(2L, r1.getRemainingCapacity()); // >0 branch

        assertEquals(11L, r2.getId());
        assertEquals(4L, r2.getActiveAssignments());
        assertEquals(0L, r2.getRemainingCapacity()); // ==0 branch

        // verifies principais
        verify(requestRepo).countGroupedByStatus();
        verify(requestRepo).countGroupedByTeamAndStatus();
        verify(attendantRepo).findAll();
        verify(requestRepo).countByAttendantAndStatus(10L, ServiceRequestStatus.ASSIGNED);
        verify(requestRepo).countByAttendantAndStatus(11L, ServiceRequestStatus.ASSIGNED);

        for (Team t : Team.values()) {
            verify(queueRepo).countByTeam(t);
        }

        verifyNoMoreInteractions(queueRepo, requestRepo, attendantRepo);
    }
}

