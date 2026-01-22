package com.flowpay.atendimento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info()
      .title("FlowPay Atendimento API")
      .version("1.0.0")
      .description("API de distribuição e monitoramento de atendimentos. Capacidade 3 por atendente; fila FIFO por time; SSE para dashboard."));
  }
}
