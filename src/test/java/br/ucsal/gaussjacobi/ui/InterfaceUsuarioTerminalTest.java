package br.ucsal.gaussjacobi.ui;

import br.ucsal.gaussjacobi.core.GaussJacobiSolver;
import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
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
        assertTrue(saida.toString().contains("Valor invalido"));
        assertTrue(saida.toString().contains("Quantidade invalida"));
    }

    @Test
    void encerraNormalmenteQuandoEntradaTermina() {
        InterfaceUsuarioTerminal terminal = new InterfaceUsuarioTerminal(new StringReader(""), new StringWriter());

        assertTrue(terminal.solicitarProblema().isEmpty());
        assertFalse(terminal.desejaResolverOutroSistema());
    }

    @Test
    void exibePassoAPassoComTodosOsDigitosESolucaoComSeisCasasDecimais() {
        String saida = exibir(problema(
                new String[][]{{"4", "1"}, {"2", "3"}},
                new String[]{"1", "2"},
                "1E-6",
                100));

        assertTrue(saida.contains("alfa1 = 0.25"));
        assertTrue(saida.contains("alfa2 = 0.6666666666666666666666666666666667"));
        assertTrue(saida.contains("a convergencia e garantida"));
        assertTrue(saida.contains("Iteracoes (erro = maior |x(k) - x(k-1)|):"));
        assertTrue(linhaDaTabela(saida, "1").matches(
                "\\s+1\\s+0\\.25\\s+0\\.6666666666666666666666666666666667\\s+0\\.6666666666666666666666666666666667"));
        assertTrue(linhaDaTabela(saida, "0").matches("\\s+0\\s+0\\s+0\\s+-"));
        assertTrue(saida.contains("Solucao encontrada:"));
        assertTrue(saida.contains("x1 = 0.099999"));
        assertTrue(saida.contains("x2 = 0.599999"));
        assertFalse(saida.contains("reordenadas"));
    }

    @Test
    void exibeNovaOrdemQuandoEquacoesSaoReordenadas() {
        String saida = exibir(problema(
                new String[][]{{"0", "1"}, {"1", "1"}},
                new String[]{"1", "2"},
                "1E-6",
                100));

        assertTrue(saida.contains("As equacoes foram reordenadas"));
        assertTrue(saida.contains("Nova ordem: E2 E1"));
        assertTrue(saida.contains("x1 = 1.000000"));
    }

    @Test
    void exibeAvisoDeCriterioNaoSatisfeitoEDivergencia() {
        String saida = exibir(problema(
                new String[][]{{"1", "2", "2"}, {"2", "1", "2"}, {"2", "2", "1"}},
                new String[]{"5", "5", "5"},
                "1E-6",
                1000));

        assertTrue(saida.contains("alfa maximo = 4"));
        assertTrue(saida.contains("a convergencia nao e garantida"));
        assertTrue(saida.contains("O metodo divergiu"));
        assertFalse(saida.contains("E+"));
    }

    @Test
    void exibeUltimaAproximacaoQuandoNaoConverge() {
        String saida = exibir(problema(
                new String[][]{{"1", "2"}, {"1", "1"}},
                new String[]{"1", "1"},
                "1E-30",
                3));

        assertTrue(saida.contains("nao convergiu"));
        assertTrue(saida.contains("Ultima aproximacao calculada:"));
        assertTrue(saida.contains("x2 = 2.000000"));
    }

    @Test
    void removeAcentosDeMensagensDeErro() {
        StringWriter saida = new StringWriter();
        InterfaceUsuarioTerminal terminal = new InterfaceUsuarioTerminal(new StringReader(""), saida);

        terminal.exibirErro("Não há solução possível.");

        assertTrue(saida.toString().contains("Nao ha solucao possivel."));
    }

    private String linhaDaTabela(String saida, String iteracao) {
        return saida.lines()
                .filter(linha -> linha.trim().startsWith(iteracao + " "))
                .findFirst()
                .orElse("");
    }

    private String exibir(ProblemaGaussJacobi problema) {
        StringWriter saida = new StringWriter();
        InterfaceUsuarioTerminal terminal = new InterfaceUsuarioTerminal(new StringReader(""), saida);
        terminal.exibirResultado(new GaussJacobiSolver().resolver(problema));
        return saida.toString();
    }

    private ProblemaGaussJacobi problema(String[][] linhas, String[] termos, String tolerancia, int iteracoes) {
        BigDecimal[][] matriz = new BigDecimal[linhas.length][];
        for (int linha = 0; linha < linhas.length; linha++) {
            matriz[linha] = vetor(linhas[linha]);
        }
        BigDecimal[] chute = new BigDecimal[termos.length];
        Arrays.fill(chute, BigDecimal.ZERO);
        return new ProblemaGaussJacobi(matriz, vetor(termos), chute, new BigDecimal(tolerancia), iteracoes);
    }

    private BigDecimal[] vetor(String[] valores) {
        return Arrays.stream(valores).map(BigDecimal::new).toArray(BigDecimal[]::new);
    }
}
