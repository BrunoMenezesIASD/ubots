package com.flowpay.atendimento.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateServiceRequestRequest {
  @NotBlank @Size(max = 120)
  private String customerName;

  @NotBlank @Size(max = 200)
  private String subject;
}
