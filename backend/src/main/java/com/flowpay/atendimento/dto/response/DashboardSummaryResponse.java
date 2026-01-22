package com.flowpay.atendimento.dto.response;

import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
@Data
public class DashboardSummaryResponse {
  Map<ServiceRequestStatus, Long> totalByStatus;
  Map<Team, Long> queueSizeByTeam;
  List<AttendantResponse> attendants;
  Map<Team, Map<ServiceRequestStatus, Long>> totalByTeamAndStatus;
}
