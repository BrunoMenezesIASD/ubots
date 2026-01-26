package com.flowpay.atendimento.enums;

import java.util.Locale;

public enum Team {
  CARTOES,
  EMPRESTIMOS,
  OUTROS;

  public static Team fromSubject(String subject) {
    if ("cartão".contains(subject.toLowerCase(Locale.ROOT))
        || "cartões".contains(subject.toLowerCase(Locale.ROOT))
        || "cartao".contains(subject.toLowerCase(Locale.ROOT))
        || "cartoes".contains(subject.toLowerCase(Locale.ROOT))
        || "card".contains(subject.toLowerCase(Locale.ROOT))) return CARTOES;

    if ("empréstimo".contains(subject.toLowerCase(Locale.ROOT))
        || "emprestimo".contains(subject.toLowerCase(Locale.ROOT))
        || "emprestimos".contains(subject.toLowerCase(Locale.ROOT))
        || "emprestado".contains(subject.toLowerCase(Locale.ROOT))
        || "loan".contains(subject.toLowerCase(Locale.ROOT))) return EMPRESTIMOS;

    return OUTROS;
  }
}
