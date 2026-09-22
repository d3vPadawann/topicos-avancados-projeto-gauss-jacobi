package br.ucsal.gaussjacobi.application;

import br.ucsal.gaussjacobi.core.GaussJacobiSolver;
import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;
import br.ucsal.gaussjacobi.ui.InterfaceUsuario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GaussJacobiControllerTest {

    @Test
    void continuaAposErroDeValidacao() {
        InterfaceUsuarioSimulada interfaceUsuario = new InterfaceUsuarioSimulada();
        interfaceUsuario.problemas.add(problemaInvalido());
        interfaceUsuario.problemas.add(problemaValido());
        interfaceUsuario.respostas.add(true);
        interfaceUsuario.respostas.add(false);

        new GaussJacobiController(interfaceUsuario, new GaussJacobiSolver()).executar();

        assertEquals(1, interfaceUsuario.erros.size());
        assertTrue(interfaceUsuario.erros.getFirst().contains("diagonal"));
        assertEquals(1, interfaceUsuario.resultados.size());
        assertTrue(interfaceUsuario.resultados.getFirst().convergiu());
    }

    private ProblemaGaussJacobi problemaInvalido() {
        return new ProblemaGaussJacobi(
                new BigDecimal[][]{{BigDecimal.ZERO}},
                new BigDecimal[]{BigDecimal.ONE},
                new BigDecimal[]{BigDecimal.ZERO},
                new BigDecimal("0.001"),
                10);
    }

    private ProblemaGaussJacobi problemaValido() {
        return new ProblemaGaussJacobi(
                new BigDecimal[][]{{new BigDecimal("2")}},
                new BigDecimal[]{new BigDecimal("4")},
                new BigDecimal[]{BigDecimal.ZERO},
                new BigDecimal("0.001"),
                10);
    }

    private static final class InterfaceUsuarioSimulada implements InterfaceUsuario {

        private final Deque<ProblemaGaussJacobi> problemas = new ArrayDeque<>();
        private final Deque<Boolean> respostas = new ArrayDeque<>();
        private final List<ResultadoGaussJacobi> resultados = new ArrayList<>();
        private final List<String> erros = new ArrayList<>();

        @Override
        public Optional<ProblemaGaussJacobi> solicitarProblema() {
            return Optional.ofNullable(problemas.poll());
        }

        @Override
        public void exibirResultado(ResultadoGaussJacobi resultado) {
            resultados.add(resultado);
        }

        @Override
        public void exibirErro(String mensagem) {
            erros.add(mensagem);
        }

        @Override
        public boolean desejaResolverOutroSistema() {
            return respostas.remove();
        }
    }
}
