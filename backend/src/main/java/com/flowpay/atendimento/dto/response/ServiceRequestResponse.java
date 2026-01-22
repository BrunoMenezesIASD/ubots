package com.flowpay.atendimento.dto.response;

import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@Data
public class ServiceRequestResponse {
  Long id;
  String customerName;
  String subject;
  Team team;
  ServiceRequestStatus status;
  Long assignedAttendantId;
  Instant createdAt;
  Instant updatedAt;
}
