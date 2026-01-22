package com.flowpay.atendimento.service.impl;

import com.flowpay.atendimento.dto.mapper.ServiceRequestMapper;
import com.flowpay.atendimento.dto.request.CreateServiceRequest;
import com.flowpay.atendimento.dto.response.ServiceRequestResponse;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.exception.NotFoundException;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.DistributionService;
import com.flowpay.atendimento.service.ServiceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRequestServiceImpl implements ServiceRequestService {

  private final DistributionService distributionService;
  private final ServiceRequestRepository repository;

  @Override
  public ServiceRequestResponse create(CreateServiceRequest req) {
    return ServiceRequestMapper.toResponse(distributionService.createAndDistribute(req.getCustomerName(), req.getSubject()));
  }

  @Override
  public ServiceRequestResponse getById(Long id) {
    return repository.findById(id)
      .map(ServiceRequestMapper::toResponse)
      .orElseThrow(() -> new NotFoundException("Atendimento não encontrado: " + id));
  }

  @Override
  public List<ServiceRequestResponse> list(Team team, ServiceRequestStatus status) {
    Specification<com.flowpay.atendimento.entity.ServiceRequest> spec = Specification.where(null);
    if (team != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("team"), team));
    if (status != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));

    return repository.findAll(spec).stream().map(ServiceRequestMapper::toResponse).toList();
  }

  @Override
  public ServiceRequestResponse finish(Long id) {
    return ServiceRequestMapper.toResponse(distributionService.finishAndRedistribute(id));
  }
}
