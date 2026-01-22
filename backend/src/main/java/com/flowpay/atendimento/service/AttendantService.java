package com.flowpay.atendimento.service;

import com.flowpay.atendimento.dto.request.CreateAttendantRequest;
import com.flowpay.atendimento.dto.request.ToggleAttendantActiveRequest;
import com.flowpay.atendimento.dto.response.AttendantResponse;
import com.flowpay.atendimento.enums.Team;

import java.util.List;

public interface AttendantService {
  AttendantResponse create(CreateAttendantRequest req);
  List<AttendantResponse> list(Team team);
  AttendantResponse toggleActive(Long id, ToggleAttendantActiveRequest req);
}
