package com.flowpay.atendimento.service;

import com.flowpay.atendimento.dto.request.CreateAttendantRequest;
import com.flowpay.atendimento.dto.request.ToggleAttendantActiveRequest;
import com.flowpay.atendimento.dto.response.AttendantResponse;
import com.flowpay.atendimento.dto.response.DashboardSummaryResponse;
import com.flowpay.atendimento.entity.Attendant;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.StreamEventType;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.exception.NotFoundException;
import com.flowpay.atendimento.repository.AttendantRepository;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.impl.AttendantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendantServiceImplTest {

    @Mock private AttendantRepository attendantRepo;
    @Mock private ServiceRequestRepository requestRepo;
    @Mock private StreamService streamService;
    @Mock private DashboardService dashboardService;

    @InjectMocks private AttendantServiceImpl service;

    @Captor private ArgumentCaptor<Attendant> attendantCaptor;

    @Test
    void create_whenActiveNull_defaultsToTrue_andRemainingCapacityPositive_andPublishes() {
        // arrange
        CreateAttendantRequest req = new CreateAttendantRequest();
        req.setName("Ana");
        req.setTeam(Team.CARTOES);
        req.setActive(null); // branch: null => true

        DashboardSummaryResponse summary = mock(DashboardSummaryResponse.class);
        when(dashboardService.getSummary()).thenReturn(summary);

        when(attendantRepo.save(any(Attendant.class))).thenAnswer(inv -> {
            Attendant a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        when(requestRepo.countByAttendantAndStatus(10L, ServiceRequestStatus.ASSIGNED))
                .thenReturn(1L); // remaining = 2 (>0)

        // act
        AttendantResponse resp = service.create(req);

        // assert
        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertTrue(resp.isActive());
        assertEquals(1L, resp.getActiveAssignments());
        assertEquals(2L, resp.getRemainingCapacity());

        verify(attendantRepo).save(attendantCaptor.capture());
        Attendant saved = attendantCaptor.getValue();
        assertEquals("Ana", saved.getName());
        assertEquals(Team.CARTOES, saved.getTeam());
        assertTrue(saved.isActive());

        verify(streamService).publish(StreamEventType.ATTENDANT_UPDATED, resp);
        verify(streamService).publish(StreamEventType.DASHBOARD_UPDATED, summary);
        verifyNoMoreInteractions(streamService);
    }

    @Test
    void create_whenActiveFalse_setsFalse_andRemainingCapacityZero_andPublishes() {
        // arrange
        CreateAttendantRequest req = new CreateAttendantRequest();
        req.setName("Bia");
        req.setTeam(Team.EMPRESTIMOS);
        req.setActive(false); // branch: false => false

        DashboardSummaryResponse summary = mock(DashboardSummaryResponse.class);
        when(dashboardService.getSummary()).thenReturn(summary);

        when(attendantRepo.save(any(Attendant.class))).thenAnswer(inv -> {
            Attendant a = inv.getArgument(0);
            a.setId(11L);
            return a;
        });

        when(requestRepo.countByAttendantAndStatus(11L, ServiceRequestStatus.ASSIGNED))
                .thenReturn(5L); // remaining = max(0, 3-5)=0

        // act
        AttendantResponse resp = service.create(req);

        // assert
        assertNotNull(resp);
        assertEquals(11L, resp.getId());
        assertFalse(resp.isActive());
        assertEquals(5L, resp.getActiveAssignments());
        assertEquals(0L, resp.getRemainingCapacity());

        verify(attendantRepo).save(attendantCaptor.capture());
        assertFalse(attendantCaptor.getValue().isActive());

        verify(streamService).publish(StreamEventType.ATTENDANT_UPDATED, resp);
        verify(streamService).publish(StreamEventType.DASHBOARD_UPDATED, summary);
        verifyNoMoreInteractions(streamService);
    }

    @Test
    void list_whenTeamNull_usesFindAll_andMaps() {
        // arrange
        Attendant a1 = Attendant.builder().id(1L).name("A1").team(Team.CARTOES).active(true).build();
        Attendant a2 = Attendant.builder().id(2L).name("A2").team(Team.CARTOES).active(true).build();

        when(attendantRepo.findAll()).thenReturn(List.of(a1, a2));
        when(requestRepo.countByAttendantAndStatus(1L, ServiceRequestStatus.ASSIGNED)).thenReturn(0L); // remaining 3
        when(requestRepo.countByAttendantAndStatus(2L, ServiceRequestStatus.ASSIGNED)).thenReturn(4L); // remaining 0

        // act
        List<AttendantResponse> out = service.list(null);

        // assert
        assertNotNull(out);
        assertEquals(2, out.size());

        AttendantResponse r1 = out.get(0);
        AttendantResponse r2 = out.get(1);

        assertEquals(1L, r1.getId());
        assertEquals(0L, r1.getActiveAssignments());
        assertEquals(3L, r1.getRemainingCapacity());

        assertEquals(2L, r2.getId());
        assertEquals(4L, r2.getActiveAssignments());
        assertEquals(0L, r2.getRemainingCapacity());

        verify(attendantRepo).findAll();
        verify(attendantRepo, never()).findByTeamOrderByIdAsc(any());
        verifyNoInteractions(streamService, dashboardService);
    }

    @Test
    void list_whenTeamProvided_usesFindByTeamOrderByIdAsc() {
        // arrange
        Attendant a = Attendant.builder().id(3L).name("C1").team(Team.OUTROS).active(true).build();

        when(attendantRepo.findByTeamOrderByIdAsc(Team.OUTROS)).thenReturn(List.of(a));
        when(requestRepo.countByAttendantAndStatus(3L, ServiceRequestStatus.ASSIGNED)).thenReturn(2L); // remaining 1

        // act
        List<AttendantResponse> out = service.list(Team.OUTROS);

        // assert
        assertNotNull(out);
        assertEquals(1, out.size());
        assertEquals(3L, out.get(0).getId());
        assertEquals(2L, out.get(0).getActiveAssignments());
        assertEquals(1L, out.get(0).getRemainingCapacity());

        verify(attendantRepo, never()).findAll();
        verify(attendantRepo).findByTeamOrderByIdAsc(Team.OUTROS);
        verifyNoInteractions(streamService, dashboardService);
    }

    @Test
    void toggleActive_whenNotFound_throws_andDoesNotPublish() {
        // arrange
        when(attendantRepo.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ToggleAttendantActiveRequest req = new ToggleAttendantActiveRequest();
        req.setActive(true);

        // act + assert
        assertThrows(NotFoundException.class, () -> service.toggleActive(99L, req));

        verify(attendantRepo).findByIdForUpdate(99L);
        verify(attendantRepo, never()).save(any());
        verifyNoInteractions(requestRepo, streamService, dashboardService);
    }

    @Test
    void toggleActive_whenFound_updatesAndPublishes_andRemainingZeroAtLimit() {
        // arrange
        Attendant existing = Attendant.builder()
                .id(20L).name("T1").team(Team.CARTOES).active(false).build();

        when(attendantRepo.findByIdForUpdate(20L)).thenReturn(Optional.of(existing));
        when(attendantRepo.save(any(Attendant.class))).thenAnswer(inv -> inv.getArgument(0));

        when(requestRepo.countByAttendantAndStatus(20L, ServiceRequestStatus.ASSIGNED))
                .thenReturn(3L); // remaining = 0

        DashboardSummaryResponse summary = mock(DashboardSummaryResponse.class);
        when(dashboardService.getSummary()).thenReturn(summary);

        ToggleAttendantActiveRequest req = new ToggleAttendantActiveRequest();
        req.setActive(true);

        // act
        AttendantResponse resp = service.toggleActive(20L, req);

        // assert
        assertNotNull(resp);
        assertTrue(existing.isActive());
        assertTrue(resp.isActive());
        assertEquals(3L, resp.getActiveAssignments());
        assertEquals(0L, resp.getRemainingCapacity());

        verify(attendantRepo).findByIdForUpdate(20L);
        verify(attendantRepo).save(existing);

        verify(streamService).publish(StreamEventType.ATTENDANT_UPDATED, resp);
        verify(streamService).publish(StreamEventType.DASHBOARD_UPDATED, summary);
        verifyNoMoreInteractions(streamService);
    }
}
