package com.flowpay.atendimento.service.impl;

import com.flowpay.atendimento.dto.mapper.ServiceRequestMapper;
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
import com.flowpay.atendimento.service.DashboardService;
import com.flowpay.atendimento.service.DistributionService;
import com.flowpay.atendimento.service.StreamService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService {

  private final ServiceRequestRepository requestRepo;
  private final QueueItemRepository queueRepo;
  private final AttendantRepository attendantRepo;
  private final StreamService streamService;
  private final DashboardService dashboardService;

  @Override
  @Transactional
  public ServiceRequest createAndDistribute(String customerName, String subject) {
    Team team = Team.fromSubject(subject);

    ServiceRequest req = ServiceRequest.builder()
      .customerName(customerName)
      .subject(subject)
      .team(team)
      .status(ServiceRequestStatus.NEW)
      .assignedAttendantId(null)
      .createdAt(Instant.now())
      .updatedAt(Instant.now())
      .build();

    req = requestRepo.save(req);
    streamService.publish(StreamEventType.REQUEST_CREATED, ServiceRequestMapper.toResponse(req));

    if (!tryAssign(req)) {
      enqueue(req);
    }

    streamService.publish(StreamEventType.DASHBOARD_UPDATED, dashboardService.getSummary());
    return req;
  }

  @Override
  @Transactional
  public ServiceRequest finishAndRedistribute(Long requestId) {
    ServiceRequest req = requestRepo.findByIdForUpdate(requestId)
      .orElseThrow(() -> new NotFoundException("Atendimento não encontrado: " + requestId));

    if (req.getStatus() == ServiceRequestStatus.DONE) return req;
    if (req.getStatus() != ServiceRequestStatus.ASSIGNED) {
      throw new BusinessRuleException("Só é possível finalizar atendimentos em status ASSIGNED.");
    }

    req.setStatus(ServiceRequestStatus.DONE);
    req.setUpdatedAt(Instant.now());
    req = requestRepo.save(req);

    streamService.publish(StreamEventType.REQUEST_FINISHED, ServiceRequestMapper.toResponse(req));

    tryDistributeFromQueue(req.getTeam());

    streamService.publish(StreamEventType.DASHBOARD_UPDATED, dashboardService.getSummary());
    return req;
  }

  private boolean tryAssign(ServiceRequest req) {
    Optional<Long> attendantIdOptional = attendantRepo.lockBestAvailableAttendantId(req.getTeam().name());

    if (!attendantIdOptional.isPresent()) return false;
    Long attendantId = attendantIdOptional.get();
    attendantRepo.findByIdForUpdate(attendantId)
      .orElseThrow(() -> new NotFoundException("Atendente não encontrado: " + attendantId));

    req.setAssignedAttendantId(attendantId);
    req.setStatus(ServiceRequestStatus.ASSIGNED);
    req.setUpdatedAt(Instant.now());
    requestRepo.save(req);

    streamService.publish(StreamEventType.REQUEST_ASSIGNED, ServiceRequestMapper.toResponse(req));
    return true;
  }

  private void enqueue(ServiceRequest req) {
    queueRepo.save(QueueItem.builder()
      .serviceRequestId(req.getId())
      .team(req.getTeam())
      .enqueuedAt(Instant.now())
      .build());

    req.setStatus(ServiceRequestStatus.QUEUED);
    req.setUpdatedAt(Instant.now());
    requestRepo.save(req);

    streamService.publish(StreamEventType.REQUEST_QUEUED, ServiceRequestMapper.toResponse(req));
  }

  private void tryDistributeFromQueue(Team team) {
    while (true) {
      Optional<QueueItem> qiOpt = queueRepo.findFirstByTeamForUpdate(team);
      if (qiOpt.isEmpty()) return;

      QueueItem qi = qiOpt.get();
      ServiceRequest queued = requestRepo.findByIdForUpdate(qi.getServiceRequestId())
        .orElseThrow(() -> new NotFoundException("Atendimento enfileirado não encontrado: " + qi.getServiceRequestId()));

      if (queued.getStatus() != ServiceRequestStatus.QUEUED) {
        queueRepo.delete(qi);
        continue;
      }

      if (!tryAssign(queued)) {
        return;
      }

      queueRepo.delete(qi);
    }
  }
}
