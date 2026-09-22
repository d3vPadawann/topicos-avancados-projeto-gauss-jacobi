package br.ucsal.gaussjacobi.ui;

import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterfaceUsuarioTerminalTest {

    @Test
    void repeteEntradasInvalidasEAceitaVirgulaDecimal() {
        String dados = String.join("\n",
                "abc",
                "0",
                "2",
                "1 2 3",
                "4 1",
                "2 3",
                "1 2",
                "0 0",
                "0",
                "0,0000000000000000000000001",
                "-2",
                "10");
        StringWriter saida = new StringWriter();
        InterfaceUsuarioTerminal terminal = new InterfaceUsuarioTerminal(new StringReader(dados), saida);

        Optional<ProblemaGaussJacobi> problema = terminal.solicitarProblema();

        assertTrue(problema.isPresent());
        assertEquals(0, problema.orElseThrow().matrizA()[0][0].compareTo(new BigDecimal("4")));
        assertEquals(0, problema.orElseThrow().tolerancia().compareTo(new BigDecimal("1E-25")));
        assertEquals(10, problema.orElseThrow().iteracoesMaximas());
        assertTrue(saida.toString().contains("Valor inválido"));
        assertTrue(saida.toString().contains("Quantidade inválida"));
    }

    @Test
    void encerraNormalmenteQuandoEntradaTermina() {
        InterfaceUsuarioTerminal terminal = new InterfaceUsuarioTerminal(new StringReader(""), new StringWriter());

        assertTrue(terminal.solicitarProblema().isEmpty());
        assertFalse(terminal.desejaResolverOutroSistema());
    }

    @Test
    void apresentaTodaAPrecisaoDoResultado() {
        StringWriter saida = new StringWriter();
        InterfaceUsuarioTerminal terminal = new InterfaceUsuarioTerminal(new StringReader(""), saida);
        BigDecimal valorPreciso = new BigDecimal("0.1234567890123456789012345678901234");

        terminal.exibirResultado(new ResultadoGaussJacobi(
                new BigDecimal[]{valorPreciso},
                2,
                new BigDecimal("1E-30"),
                true));
        terminal.exibirResultado(new ResultadoGaussJacobi(
                new BigDecimal[]{new BigDecimal("2")},
                3,
                BigDecimal.ONE,
                false));

        assertTrue(saida.toString().contains(valorPreciso.toPlainString()));
        assertTrue(saida.toString().contains("1E-30"));
        assertTrue(saida.toString().contains("não convergiu"));
    }
}
