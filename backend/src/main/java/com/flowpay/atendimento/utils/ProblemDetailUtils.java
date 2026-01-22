package com.flowpay.atendimento.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public class ProblemDetailUtils {

    public static ProblemDetail buildProblem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("method", request.getMethod());
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }

    public static ProblemDetail buildProblem(HttpStatus status, String title, String detail, HttpServletRequest request, String erroId) {
        ProblemDetail problem = ProblemDetailUtils.buildProblem(status, title, detail,request);
        problem.setProperty("erroId", erroId);
        return problem;
    }

    public static ProblemDetail buildProblem(HttpStatus status, String title, String detail, HttpServletRequest request, Map<String, String> erros) {
        ProblemDetail problem = ProblemDetailUtils.buildProblem(status, title, detail,request);
        problem.setProperty("erros", erros);
        return problem;
    }
}
