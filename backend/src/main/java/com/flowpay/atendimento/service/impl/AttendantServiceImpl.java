package com.flowpay.atendimento.service.impl;

import com.flowpay.atendimento.dto.mapper.AttendantMapper;
import com.flowpay.atendimento.dto.request.CreateAttendantRequest;
import com.flowpay.atendimento.dto.request.ToggleAttendantActiveRequest;
import com.flowpay.atendimento.dto.response.AttendantResponse;
import com.flowpay.atendimento.entity.Attendant;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.StreamEventType;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.exception.NotFoundException;
import com.flowpay.atendimento.repository.AttendantRepository;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.AttendantService;
import com.flowpay.atendimento.service.DashboardService;
import com.flowpay.atendimento.service.StreamService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendantServiceImpl implements AttendantService {

  private static final int MAX_CAPACITY = 3;

  private final AttendantRepository attendantRepo;
  private final ServiceRequestRepository requestRepo;
  private final StreamService streamService;
  private final DashboardService dashboardService;

  @Override
  public AttendantResponse create(CreateAttendantRequest req) {
    Attendant a = Attendant.builder()
      .name(req.getName())
      .team(req.getTeam())
      .active(req.getActive() == null || req.getActive())
      .createdAt(Instant.now())
      .build();

    a = attendantRepo.save(a);
    long active = requestRepo.countByAttendantAndStatus(a.getId(), ServiceRequestStatus.ASSIGNED);
    AttendantResponse resp = AttendantMapper.toResponse(a, active, Math.max(0, MAX_CAPACITY - active));
    streamService.publish(StreamEventType.ATTENDANT_UPDATED, resp);
    streamService.publish(StreamEventType.DASHBOARD_UPDATED, dashboardService.getSummary());
    return resp;
  }

  @Override
  public List<AttendantResponse> list(Team team) {
    List<Attendant> list = (team == null) ? attendantRepo.findAll() : attendantRepo.findByTeamOrderByIdAsc(team);
    return list.stream().map(a -> {
      long active = requestRepo.countByAttendantAndStatus(a.getId(), ServiceRequestStatus.ASSIGNED);
      return AttendantMapper.toResponse(a, active, Math.max(0, MAX_CAPACITY - active));
    }).toList();
  }

  @Override
  @Transactional
  public AttendantResponse toggleActive(Long id, ToggleAttendantActiveRequest req) {
    Attendant a = attendantRepo.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Atendente não encontrado: " + id));
    a.setActive(req.isActive());
    a = attendantRepo.save(a);

    long active = requestRepo.countByAttendantAndStatus(a.getId(), ServiceRequestStatus.ASSIGNED);
    AttendantResponse resp = AttendantMapper.toResponse(a, active, Math.max(0, MAX_CAPACITY - active));
    streamService.publish(StreamEventType.ATTENDANT_UPDATED, resp);
    streamService.publish(StreamEventType.DASHBOARD_UPDATED, dashboardService.getSummary());
    return resp;
  }
}
