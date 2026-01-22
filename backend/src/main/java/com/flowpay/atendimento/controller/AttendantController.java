package com.flowpay.atendimento.controller;

import com.flowpay.atendimento.dto.request.CreateAttendantRequest;
import com.flowpay.atendimento.dto.request.ToggleAttendantActiveRequest;
import com.flowpay.atendimento.dto.response.AttendantResponse;
import com.flowpay.atendimento.enums.Team;
import com.flowpay.atendimento.service.AttendantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendants")
@RequiredArgsConstructor
@Tag(name = "Atendentes")
public class AttendantController {

  private final AttendantService service;

  @PostMapping
  @Operation(summary = "Cadastrar atendente")
  public AttendantResponse create(@Valid @RequestBody CreateAttendantRequest req) {
    return service.create(req);
  }

  @GetMapping
  @Operation(summary = "Listar atendentes (opcional por time)")
  public List<AttendantResponse> list(@RequestParam(required = false) Team team) {
    return service.list(team);
  }

  @PatchMapping("/{id}/toggle-active")
  @Operation(summary = "Ativar/desativar atendente")
  public AttendantResponse toggle(@PathVariable Long id, @RequestBody ToggleAttendantActiveRequest req) {
    return service.toggleActive(id, req);
  }
}
