package com.flowpay.atendimento.service;

import com.flowpay.atendimento.dto.request.CreateServiceRequestRequest;
import com.flowpay.atendimento.dto.response.ServiceRequestResponse;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;

import java.util.List;

public interface ServiceRequestService {
  ServiceRequestResponse create(CreateServiceRequestRequest req);
  ServiceRequestResponse getById(Long id);
  List<ServiceRequestResponse> list(Team team, ServiceRequestStatus status);
  ServiceRequestResponse finish(Long id);
}
