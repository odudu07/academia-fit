package com.exemplo.academia.exception;

/** Violação de regra de negócio (sem matrícula, cancelamento tardio...) -> HTTP 422. */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
