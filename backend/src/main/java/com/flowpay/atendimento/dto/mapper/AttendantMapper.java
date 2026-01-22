package com.flowpay.atendimento.dto.mapper;

import com.flowpay.atendimento.dto.response.AttendantResponse;
import com.flowpay.atendimento.entity.Attendant;

public final class AttendantMapper {
  private AttendantMapper() {}

  public static AttendantResponse toResponse(Attendant a, long activeAssignments, long remainingCapacity) {
    return AttendantResponse.builder()
      .id(a.getId())
      .name(a.getName())
      .team(a.getTeam())
      .active(a.isActive())
      .activeAssignments(activeAssignments)
      .remainingCapacity(remainingCapacity)
      .build();
  }
}
