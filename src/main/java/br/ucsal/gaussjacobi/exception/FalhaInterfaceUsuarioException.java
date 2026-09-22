package br.ucsal.gaussjacobi.exception;

public final class FalhaInterfaceUsuarioException extends RuntimeException {

    public FalhaInterfaceUsuarioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
