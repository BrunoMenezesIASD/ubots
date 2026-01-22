package com.flowpay.atendimento.controller;

import com.flowpay.atendimento.dto.request.CreateServiceRequestRequest;
import com.flowpay.atendimento.dto.response.ServiceRequestResponse;
import com.flowpay.atendimento.enums.ServiceRequestStatus;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "Atendimentos")
public class ServiceRequestController {

  private final ServiceRequestService service;

  @PostMapping
  @Operation(summary = "Criar atendimento e distribuir/enfileirar automaticamente")
  public ServiceRequestResponse create(@Valid @RequestBody CreateServiceRequestRequest req) {
    return service.create(req);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar atendimento por id")
  public ServiceRequestResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @GetMapping
  @Operation(summary = "Listar atendimentos com filtros opcionais")
  public List<ServiceRequestResponse> list(@RequestParam(required = false) Team team,
                                           @RequestParam(required = false) ServiceRequestStatus status) {
    return service.list(team, status);
  }

  @PostMapping("/{id}/finish")
  @Operation(summary = "Finalizar atendimento e redistribuir a fila do time")
  public ServiceRequestResponse finish(@PathVariable Long id) {
    return service.finish(id);
  }
}
