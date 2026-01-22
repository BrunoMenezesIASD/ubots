package com.flowpay.atendimento.dto.mapper;

import com.flowpay.atendimento.dto.response.ServiceRequestResponse;
import com.flowpay.atendimento.entity.ServiceRequest;

public final class ServiceRequestMapper {
  private ServiceRequestMapper() {}

  public static ServiceRequestResponse toResponse(ServiceRequest r) {
    return ServiceRequestResponse.builder()
      .id(r.getId())
      .customerName(r.getCustomerName())
      .subject(r.getSubject())
      .team(r.getTeam())
      .status(r.getStatus())
      .assignedAttendantId(r.getAssignedAttendantId())
      .createdAt(r.getCreatedAt())
      .updatedAt(r.getUpdatedAt())
      .build();
  }
}
