package com.flowpay.atendimento.dto.request;

import com.flowpay.atendimento.enums.Team;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAttendantRequest {
  @NotBlank @Size(max = 120)
  private String name;

  @NotNull
  private Team team;

  private Boolean active = true;
}
