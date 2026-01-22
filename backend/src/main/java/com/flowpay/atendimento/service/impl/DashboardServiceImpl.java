package com.flowpay.atendimento.service.impl;

import com.flowpay.atendimento.dto.mapper.AttendantMapper;
import com.flowpay.atendimento.dto.response.AttendantResponse;
import com.flowpay.atendimento.dto.response.DashboardSummaryResponse;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.repository.AttendantRepository;
import com.flowpay.atendimento.repository.QueueItemRepository;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

  private static final int MAX_CAPACITY = 3;

  private final ServiceRequestRepository requestRepo;
  private final QueueItemRepository queueRepo;
  private final AttendantRepository attendantRepo;

  @Override
  public DashboardSummaryResponse getSummary() {
    Map<ServiceRequestStatus, Long> totalByStatus = new EnumMap<>(ServiceRequestStatus.class);
    for (ServiceRequestStatus s : ServiceRequestStatus.values()) totalByStatus.put(s, 0L);
    for (Object[] row : requestRepo.countGroupedByStatus()) {
      totalByStatus.put((ServiceRequestStatus) row[0], (Long) row[1]);
    }

    Map<Team, Long> queueSizeByTeam = new EnumMap<>(Team.class);
    for (Team t : Team.values()) queueSizeByTeam.put(t, queueRepo.countByTeam(t));

    Map<Team, Map<ServiceRequestStatus, Long>> byTeam = new EnumMap<>(Team.class);
    for (Team t : Team.values()) {
      Map<ServiceRequestStatus, Long> inner = new EnumMap<>(ServiceRequestStatus.class);
      for (ServiceRequestStatus s : ServiceRequestStatus.values()) inner.put(s, 0L);
      byTeam.put(t, inner);
    }
    for (Object[] row : requestRepo.countGroupedByTeamAndStatus()) {
      Team team = (Team) row[0];
      ServiceRequestStatus status = (ServiceRequestStatus) row[1];
      Long total = (Long) row[2];
      byTeam.get(team).put(status, total);
    }

    List<AttendantResponse> attendants = attendantRepo.findAll().stream().map(a -> {
      long active = requestRepo.countByAttendantAndStatus(a.getId(), ServiceRequestStatus.ASSIGNED);
      long remaining = Math.max(0, MAX_CAPACITY - active);
      return AttendantMapper.toResponse(a, active, remaining);
    }).toList();

    return DashboardSummaryResponse.builder()
      .totalByStatus(totalByStatus)
      .queueSizeByTeam(queueSizeByTeam)
      .attendants(attendants)
      .totalByTeamAndStatus(byTeam)
      .build();
  }
}
