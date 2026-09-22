package br.ucsal.gaussjacobi.ui;

import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;

import java.util.Optional;

public interface InterfaceUsuario {

    Optional<ProblemaGaussJacobi> solicitarProblema();

    void exibirResultado(ResultadoGaussJacobi resultado);

    void exibirErro(String mensagem);

    boolean desejaResolverOutroSistema();
}
