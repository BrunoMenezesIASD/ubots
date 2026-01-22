package com.flowpay.atendimento.integration;

import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.repository.ServiceRequestRepository;
import com.flowpay.atendimento.service.DistributionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ConcurrencyDistributionTest {

  private final DistributionService distributionService;
  private final ServiceRequestRepository requestRepo;

  ConcurrencyDistributionTest(DistributionService distributionService, ServiceRequestRepository requestRepo) {
    this.distributionService = distributionService;
    this.requestRepo = requestRepo;
  }

  @Test
  void concurrentCreatesShouldNotExceedCapacity() throws Exception {
    int requests = 30;
    ExecutorService pool = Executors.newFixedThreadPool(10);

    CountDownLatch ready = new CountDownLatch(requests);
    CountDownLatch start = new CountDownLatch(1);

    List<Future<Long>> futures = new ArrayList<>();
    for (int i = 0; i < requests; i++) {
      int idx = i;
      futures.add(pool.submit(() -> {
        ready.countDown();
        start.await(10, TimeUnit.SECONDS);
        return distributionService.createAndDistribute("C" + idx, "Problemas com cartão").getId();
      }));
    }

    assertTrue(ready.await(10, TimeUnit.SECONDS));
    start.countDown();

    List<Long> ids = new ArrayList<>();
    for (Future<Long> f : futures) ids.add(f.get(20, TimeUnit.SECONDS));

    pool.shutdown();
    assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

    var all = requestRepo.findAllById(ids);
    assertEquals(ids.size(), all.size());

    var counts = new java.util.HashMap<Long, Integer>();
    for (var r : all) {
      if (r.getStatus() == ServiceRequestStatus.ASSIGNED) {
        assertNotNull(r.getAssignedAttendantId());
        counts.merge(r.getAssignedAttendantId(), 1, Integer::sum);
      }
    }
    counts.forEach((attId, c) -> assertTrue(c <= 3, "Atendente " + attId + " excedeu capacidade: " + c));
  }
}
