package com.flowpay.atendimento.distribution;

import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.service.DistributionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DistributionServiceImplTest {

  private final DistributionService distributionService;

  DistributionServiceImplTest(DistributionService distributionService) {
    this.distributionService = distributionService;
  }

  @Test
  void shouldCreateAndDistribute() {
    var r = distributionService.createAndDistribute("Cliente 1", "Problemas com cartão");
    assertNotNull(r.getId());
    assertTrue(r.getStatus() == ServiceRequestStatus.ASSIGNED || r.getStatus() == ServiceRequestStatus.QUEUED);
  }
}
