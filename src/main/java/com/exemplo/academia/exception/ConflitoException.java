package com.exemplo.academia.exception;

/** Conflito com o estado atual (aula lotada, duplicidade...) -> HTTP 409. */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
