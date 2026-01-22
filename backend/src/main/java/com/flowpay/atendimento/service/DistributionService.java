package com.flowpay.atendimento.service;

import com.flowpay.atendimento.entity.ServiceRequest;

public interface DistributionService {
  ServiceRequest createAndDistribute(String customerName, String subject);
  ServiceRequest finishAndRedistribute(Long requestId);
}
