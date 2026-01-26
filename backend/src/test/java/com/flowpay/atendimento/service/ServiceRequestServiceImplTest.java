package com.flowpay.atendimento.service;

import com.flowpay.atendimento.dto.request.CreateServiceRequest;
import com.flowpay.atendimento.dto.response.ServiceRequestResponse;
import com.flowpay.atendimento.entity.ServiceRequest;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.exception.NotFoundException;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.impl.ServiceRequestServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceRequestServiceImplTest {

    @Mock private DistributionService distributionService;
    @Mock private ServiceRequestRepository repository;

    @InjectMocks private ServiceRequestServiceImpl service;

    @Test
    void create_delegatesToDistributionService_andMapsResponse() {
        // arrange
        CreateServiceRequest req = new CreateServiceRequest();
        req.setCustomerName("Bruno");
        req.setSubject("cartao");

        ServiceRequest created = ServiceRequest.builder()
                .id(10L)
                .customerName("Bruno")
                .subject("cartao")
                .team(Team.CARTOES)
                .status(ServiceRequestStatus.NEW)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(distributionService.createAndDistribute("Bruno", "cartao")).thenReturn(created);

        // act
        ServiceRequestResponse resp = service.create(req);

        // assert
        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals("Bruno", resp.getCustomerName());
        assertEquals("cartao", resp.getSubject());
        assertEquals(Team.CARTOES, resp.getTeam());
        assertEquals(ServiceRequestStatus.NEW, resp.getStatus());

        verify(distributionService).createAndDistribute("Bruno", "cartao");
        verifyNoInteractions(repository);
    }

    @Test
    void getById_whenFound_returnsMappedResponse() {
        // arrange
        ServiceRequest sr = ServiceRequest.builder()
                .id(20L)
                .customerName("Ana")
                .subject("assunto")
                .team(Team.OUTROS)
                .status(ServiceRequestStatus.QUEUED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(repository.findById(20L)).thenReturn(Optional.of(sr));

        // act
        ServiceRequestResponse resp = service.getById(20L);

        // assert
        assertNotNull(resp);
        assertEquals(20L, resp.getId());
        assertEquals("Ana", resp.getCustomerName());
        assertEquals(Team.OUTROS, resp.getTeam());
        assertEquals(ServiceRequestStatus.QUEUED, resp.getStatus());

        verify(repository).findById(20L);
        verifyNoInteractions(distributionService);
    }

    @Test
    void getById_whenNotFound_throwsNotFound() {
        // arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // act + assert
        assertThrows(NotFoundException.class, () -> service.getById(99L));

        verify(repository).findById(99L);
        verifyNoInteractions(distributionService);
    }

    @Test
    void list_whenTeamAndStatusNull_buildsBaseSpec_andCallsFindAll() {
        // arrange
        ServiceRequest sr1 = ServiceRequest.builder()
                .id(1L).customerName("C1").subject("S1")
                .team(Team.CARTOES).status(ServiceRequestStatus.NEW)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(sr1));

        // act
        List<ServiceRequestResponse> out = service.list(null, null);

        // assert
        assertNotNull(out);
        assertEquals(1, out.size());
        assertEquals(1L, out.get(0).getId());

        verify(repository).findAll(any(Specification.class));
        verifyNoInteractions(distributionService);
    }

    @Test
    void list_whenOnlyTeamProvided_addsTeamPredicateBranch() {
        // arrange
        ServiceRequest sr = ServiceRequest.builder()
                .id(2L).customerName("C2").subject("S2")
                .team(Team.EMPRESTIMOS).status(ServiceRequestStatus.ASSIGNED)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(sr));

        // act
        List<ServiceRequestResponse> out = service.list(Team.EMPRESTIMOS, null);

        // assert
        assertEquals(1, out.size());
        assertEquals(2L, out.get(0).getId());
        assertEquals(Team.EMPRESTIMOS, out.get(0).getTeam());

        verify(repository).findAll(any(Specification.class));
        verifyNoInteractions(distributionService);
    }

    @Test
    void list_whenOnlyStatusProvided_addsStatusPredicateBranch() {
        // arrange
        ServiceRequest sr = ServiceRequest.builder()
                .id(3L).customerName("C3").subject("S3")
                .team(Team.OUTROS).status(ServiceRequestStatus.DONE)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(sr));

        // act
        List<ServiceRequestResponse> out = service.list(null, ServiceRequestStatus.DONE);

        // assert
        assertEquals(1, out.size());
        assertEquals(3L, out.get(0).getId());
        assertEquals(ServiceRequestStatus.DONE, out.get(0).getStatus());

        verify(repository).findAll(any(Specification.class));
        verifyNoInteractions(distributionService);
    }

    @Test
    void list_whenTeamAndStatusProvided_addsBothPredicatesBranches() {
        // arrange
        ServiceRequest sr = ServiceRequest.builder()
                .id(4L).customerName("C4").subject("S4")
                .team(Team.CARTOES).status(ServiceRequestStatus.QUEUED)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(sr));

        // act
        List<ServiceRequestResponse> out = service.list(Team.CARTOES, ServiceRequestStatus.QUEUED);

        // assert
        assertEquals(1, out.size());
        assertEquals(4L, out.get(0).getId());
        assertEquals(Team.CARTOES, out.get(0).getTeam());
        assertEquals(ServiceRequestStatus.QUEUED, out.get(0).getStatus());

        verify(repository).findAll(any(Specification.class));
        verifyNoInteractions(distributionService);
    }

    @Test
    void finish_delegatesToDistributionService_andMapsResponse() {
        // arrange
        ServiceRequest finished = ServiceRequest.builder()
                .id(50L)
                .customerName("Z")
                .subject("X")
                .team(Team.OUTROS)
                .status(ServiceRequestStatus.DONE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(distributionService.finishAndRedistribute(50L)).thenReturn(finished);

        // act
        ServiceRequestResponse resp = service.finish(50L);

        // assert
        assertNotNull(resp);
        assertEquals(50L, resp.getId());
        assertEquals(ServiceRequestStatus.DONE, resp.getStatus());

        verify(distributionService).finishAndRedistribute(50L);
        verifyNoInteractions(repository);
    }
}
