package com.flowpay.atendimento.exception;

import com.flowpay.atendimento.utils.LoggerUtils;
import com.flowpay.atendimento.utils.ProblemDetailUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    LoggerUtils.buildLogErrorGlobalHandler(ex, request);
    pd.setTitle("Not Found");
    pd.setDetail(ex.getMessage());
    pd.setType(URI.create("https://flowpay.local/problems/not-found"));
    pd.setProperty("path", request.getRequestURI());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ProblemDetail> handleBusiness(BusinessRuleException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
    LoggerUtils.buildLogErrorGlobalHandler(ex, request);
    pd.setTitle("Business rule violation");
    pd.setDetail(ex.getMessage());
    pd.setType(URI.create("https://flowpay.local/problems/business-rule"));
    pd.setProperty("path", request.getRequestURI());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    LoggerUtils.buildLogErrorGlobalHandler(ex, request);
    pd.setTitle("Validation error");
    pd.setType(URI.create("https://flowpay.local/problems/validation"));
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));
    pd.setProperty("errors", errors);
    pd.setProperty("path", request.getRequestURI());
    return ResponseEntity.badRequest().body(pd);
  }

  // Utilizando ProblemDetailUtils

  @ExceptionHandler({
          ConnectException.class,
          SocketException.class
  })
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ProblemDetail handleInfrastructureExceptions(Exception ex, HttpServletRequest request) {
    LoggerUtils.buildLogErrorGlobalHandler(ex, request);
    return ProblemDetailUtils.buildProblem(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Falha na Comunicação com Dependência",
            ex.getMessage(),
            request
    );
  }

  @ExceptionHandler(TimeoutException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ProblemDetail handleTimeoutException(Exception ex, HttpServletRequest request) {
    LoggerUtils.buildLogErrorGlobalHandler(ex, request);
    return ProblemDetailUtils.buildProblem(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Tempo Limite de Operação Excedido",
            ex.getMessage(),
            request
    );
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ProblemDetail handleAllUnhandledExceptions(Exception ex, HttpServletRequest request) {
    String errorId = UUID.randomUUID().toString();
    LoggerUtils.buildLogErrorGlobalHandler(ex, request, errorId);
    return ProblemDetailUtils.buildProblem(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Falha Inesperada no Sistema",
            ex.getMessage(),
            request,
            errorId
    );
  }
}
