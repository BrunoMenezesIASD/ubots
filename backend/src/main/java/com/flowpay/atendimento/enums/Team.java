package com.flowpay.atendimento.enums;

public enum Team {
  CARTOES,
  EMPRESTIMOS,
  OUTROS;

  public static Team fromSubject(String subject) {
    if ("Problemas com cartão".equals(subject)) return CARTOES;
    if ("Contratação de empréstimo".equals(subject)) return EMPRESTIMOS;
    return OUTROS;
  }
}
