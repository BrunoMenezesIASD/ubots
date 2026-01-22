package com.flowpay.atendimento.utils;

import com.flowpay.atendimento.exception.ApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {

    private static final Logger loggerGlobalHandler = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public static void buildLogErrorGlobalHandler(Exception ex, HttpServletRequest request, String errorId) {

        loggerGlobalHandler.error("""
            ERRO [{}]
            Tipo........: {}
            Mensagem....: {}
            Método......: {}
            Path........: {}
         
            """,
                errorId,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                ex
        );
    }

    public static void buildLogErrorGlobalHandler(Exception ex, HttpServletRequest request) {

        loggerGlobalHandler.error("""
            ERRO
            Tipo........: {}
            Mensagem....: {}
            Método......: {}
            Path........: {}
         
            """,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                ex
        );
    }

    public static void buildLogWarnGlobalHandler(Exception ex, HttpServletRequest request) {

        loggerGlobalHandler.warn("""
            WARN
            Tipo........: {}
            Mensagem....: {}
            Método......: {}
            Path........: {}
         
            """,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getMethod(),
                request.getRequestURI(),
                ex
        );
    }

    public static <T> void buildLogErrorGeneric(Exception ex, Class<T> component) {

        final Logger logger = LoggerFactory.getLogger(component);

        logger.error("""
            ERRO
            Tipo........: {}
            Mensagem....: {}
         
            """,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );
    }

    public static <T> void buildLogWarnGeneric(Exception ex, Class<T> component) {

        final Logger logger = LoggerFactory.getLogger(component);

        logger.warn("""
            WARN
            Tipo........: {}
            Mensagem....: {}
         
            """,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );
    }
}
