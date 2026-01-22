package com.flowpay.atendimento.controller;

import com.flowpay.atendimento.dto.response.DashboardSummaryResponse;
import com.flowpay.atendimento.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/summary")
  @Operation(summary = "Resumo do dashboard")
  public DashboardSummaryResponse summary() {
    return dashboardService.getSummary();
  }
}
