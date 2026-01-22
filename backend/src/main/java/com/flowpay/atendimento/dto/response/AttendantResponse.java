package com.flowpay.atendimento.dto.response;

import com.flowpay.atendimento.enums.Team;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AttendantResponse {
  Long id;
  String name;
  Team team;
  boolean active;
  long activeAssignments;
  long remainingCapacity;
}
