package br.ucsal.gaussjacobi.core;

import br.ucsal.gaussjacobi.domain.AnaliseCriterioDasLinhas;
import br.ucsal.gaussjacobi.domain.IteracaoGaussJacobi;
import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;
import br.ucsal.gaussjacobi.domain.SituacaoConvergencia;
import br.ucsal.gaussjacobi.exception.DadosMatematicosInvalidosException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GaussJacobiSolverTest {

    private final GaussJacobiSolver solver = new GaussJacobiSolver();

    @Test
    void resolveSistemaConvergenteComAltaPrecisao() {
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz(new String[]{"4", "1"}, new String[]{"2", "3"}),
                vetor("1", "2"),
                vetor("0", "0"),
                decimal("1E-25"),
                200);

        ResultadoGaussJacobi resultado = solver.resolver(problema);

        assertTrue(resultado.convergiu());
        assertDecimalProximo(decimal("0.1"), resultado.solucao()[0], decimal("1E-24"));
        assertDecimalProximo(decimal("0.6"), resultado.solucao()[1], decimal("1E-24"));
        assertTrue(resultado.erroMaximo().compareTo(problema.tolerancia()) < 0);
    }

    @Test
    void convergeEmUmaIteracaoQuandoChuteJaEhSolucao() {
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz(new String[]{"4", "1"}, new String[]{"2", "3"}),
                vetor("1", "2"),
                vetor("0.1", "0.6"),
                decimal("1E-30"),
                10);

        ResultadoGaussJacobi resultado = solver.resolver(problema);

        assertTrue(resultado.convergiu());
        assertEquals(1, resultado.iteracoesRealizadas());
        assertEquals(0, resultado.solucao()[0].compareTo(decimal("0.1")));
        assertEquals(0, resultado.solucao()[1].compareTo(decimal("0.6")));
    }

    @Test
    void retornaResultadoQuandoNaoConverge() {
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz(new String[]{"1", "2"}, new String[]{"1", "1"}),
                vetor("1", "1"),
                vetor("0", "0"),
                decimal("1E-30"),
                3);

        ResultadoGaussJacobi resultado = solver.resolver(problema);

        assertFalse(resultado.convergiu());
        assertEquals(SituacaoConvergencia.NAO_CONVERGIU, resultado.situacao());
        assertEquals(3, resultado.iteracoesRealizadas());
        assertEquals(0, resultado.solucao()[0].compareTo(decimal("1")));
        assertEquals(0, resultado.solucao()[1].compareTo(decimal("2")));
    }

    @Test
    void registraCadaIteracaoAPartirDoChuteInicial() {
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz(new String[]{"4", "1"}, new String[]{"2", "3"}),
                vetor("1", "2"),
                vetor("0", "0"),
                decimal("1E-6"),
                100);

        ResultadoGaussJacobi resultado = solver.resolver(problema);
        List<IteracaoGaussJacobi> iteracoes = resultado.iteracoes();

        assertEquals(resultado.iteracoesRealizadas() + 1, iteracoes.size());
        assertEquals(0, iteracoes.getFirst().numero());
        assertTrue(iteracoes.getFirst().erro().isEmpty());
        assertEquals(0, iteracoes.get(1).aproximacao()[0].compareTo(decimal("0.25")));
        assertEquals(0, iteracoes.get(1).erroMaximo().compareTo(decimal("0.6666666666666666666666666666666667")));
    }

    @Test
    void mantemOrdemQuandoCriterioDasLinhasJaEhSatisfeito() {
        ResultadoGaussJacobi resultado = solver.resolver(sistemaDominante(
                matriz(new String[]{"10", "2", "1"}, new String[]{"1", "5", "1"}, new String[]{"2", "3", "10"}),
                vetor("7", "-8", "6")));

        assertFalse(resultado.equacoesForamReordenadas());
        assertArrayEquals(new int[]{0, 1, 2}, resultado.ordemDasEquacoes());
    }

    @Test
    void reordenaEquacoesParaObterDiagonalDominante() {
        ResultadoGaussJacobi resultado = solver.resolver(sistemaDominante(
                matriz(new String[]{"1", "5", "1"}, new String[]{"2", "3", "10"}, new String[]{"10", "2", "1"}),
                vetor("-8", "6", "7")));

        assertTrue(resultado.equacoesForamReordenadas());
        assertArrayEquals(new int[]{2, 0, 1}, resultado.ordemDasEquacoes());
        assertEquals(0, resultado.vetorUtilizado()[0].compareTo(decimal("7")));
        assertTrue(resultado.criterioDasLinhas().garanteConvergencia());
        assertTrue(resultado.convergiu());
        assertDecimalProximo(decimal("1"), resultado.solucao()[0], decimal("1E-9"));
        assertDecimalProximo(decimal("-2"), resultado.solucao()[1], decimal("1E-9"));
        assertDecimalProximo(decimal("1"), resultado.solucao()[2], decimal("1E-9"));
    }

    @Test
    void reordenaEquacoesQuandoDiagonalPossuiZero() {
        ResultadoGaussJacobi resultado = solver.resolver(sistemaDominante(
                matriz(new String[]{"0", "1"}, new String[]{"1", "1"}),
                vetor("1", "2")));

        assertArrayEquals(new int[]{1, 0}, resultado.ordemDasEquacoes());
        assertTrue(resultado.convergiu());
        assertDecimalProximo(decimal("1"), resultado.solucao()[0], decimal("1E-9"));
        assertDecimalProximo(decimal("1"), resultado.solucao()[1], decimal("1E-9"));
    }

    @Test
    void rejeitaQuandoNenhumaOrdemEliminaZeroDaDiagonal() {
        ProblemaGaussJacobi problema = sistemaDominante(
                matriz(new String[]{"0", "1"}, new String[]{"0", "1"}),
                vetor("1", "2"));

        DadosMatematicosInvalidosException excecao =
                assertThrows(DadosMatematicosInvalidosException.class, () -> solver.resolver(problema));
        assertTrue(excecao.getMessage().contains("diagonal"));
    }

    @Test
    void calculaCriterioDasLinhas() {
        ResultadoGaussJacobi resultado = solver.resolver(sistemaDominante(
                matriz(new String[]{"10", "2", "1"}, new String[]{"1", "5", "1"}, new String[]{"2", "3", "10"}),
                vetor("7", "-8", "6")));
        AnaliseCriterioDasLinhas analise = resultado.criterioDasLinhas();

        assertEquals(0, analise.alfas()[0].compareTo(decimal("0.3")));
        assertEquals(0, analise.alfas()[1].compareTo(decimal("0.4")));
        assertEquals(0, analise.alfas()[2].compareTo(decimal("0.5")));
        assertEquals(0, analise.alfaMaximo().compareTo(decimal("0.5")));
        assertTrue(analise.garanteConvergencia());
    }

    @Test
    void detectaDivergenciaAntesDoLimiteDeIteracoes() {
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz(new String[]{"1", "2", "2"}, new String[]{"2", "1", "2"}, new String[]{"2", "2", "1"}),
                vetor("5", "5", "5"),
                vetor("0", "0", "0"),
                decimal("1E-6"),
                1000);

        ResultadoGaussJacobi resultado = solver.resolver(problema);

        assertFalse(resultado.criterioDasLinhas().garanteConvergencia());
        assertEquals(0, resultado.criterioDasLinhas().alfaMaximo().compareTo(decimal("4")));
        assertEquals(SituacaoConvergencia.DIVERGIU, resultado.situacao());
        assertTrue(resultado.iteracoesRealizadas() < 1000);
    }

    private ProblemaGaussJacobi sistemaDominante(BigDecimal[][] matriz, BigDecimal[] vetor) {
        BigDecimal[] chute = new BigDecimal[vetor.length];
        Arrays.fill(chute, BigDecimal.ZERO);
        return new ProblemaGaussJacobi(matriz, vetor, chute, decimal("1E-12"), 200);
    }

    @Test
    void respeitaPrecisaoConfigurada() {
        GaussJacobiSolver solverComDezDigitos = new GaussJacobiSolver(new MathContext(10));
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz(new String[]{"3"}),
                vetor("1"),
                vetor("0"),
                decimal("1E-20"),
                1);

        ResultadoGaussJacobi resultado = solverComDezDigitos.resolver(problema);

        assertEquals("0.3333333333", resultado.solucao()[0].toPlainString());
        assertThrows(IllegalArgumentException.class, () -> new GaussJacobiSolver(MathContext.UNLIMITED));
    }

    @Test
    void rejeitaProblemasInvalidos() {
        assertThrows(DadosMatematicosInvalidosException.class, () -> solver.resolver(null));
        assertInvalido(new BigDecimal[0][0], new BigDecimal[0], new BigDecimal[0], decimal("0.1"), 10);
        assertInvalido(matriz(new String[]{"1", "2"}), vetor("1"), vetor("0"), decimal("0.1"), 10);
        assertInvalido(matriz(new String[]{"0"}), vetor("1"), vetor("0"), decimal("0.1"), 10);
        assertInvalido(matriz(new String[]{"1"}), vetor("1", "2"), vetor("0"), decimal("0.1"), 10);
        assertInvalido(matriz(new String[]{"1"}), vetor("1"), vetor("0", "1"), decimal("0.1"), 10);
        assertInvalido(new BigDecimal[][]{{null}}, vetor("1"), vetor("0"), decimal("0.1"), 10);
        assertInvalido(matriz(new String[]{"1"}), new BigDecimal[]{null}, vetor("0"), decimal("0.1"), 10);
        assertInvalido(matriz(new String[]{"1"}), vetor("1"), new BigDecimal[]{null}, decimal("0.1"), 10);
        assertInvalido(matriz(new String[]{"1"}), vetor("1"), vetor("0"), BigDecimal.ZERO, 10);
        assertInvalido(matriz(new String[]{"1"}), vetor("1"), vetor("0"), null, 10);
        assertInvalido(matriz(new String[]{"1"}), vetor("1"), vetor("0"), decimal("0.1"), 0);
    }

    @Test
    void preservaOsDadosFornecidos() {
        BigDecimal[][] matriz = matriz(new String[]{"4", "1"}, new String[]{"2", "3"});
        BigDecimal[] vetor = vetor("1", "2");
        BigDecimal[] chute = vetor("0", "0");
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz,
                vetor,
                chute,
                decimal("1E-6"),
                30);

        matriz[0][0] = decimal("99");
        vetor[0] = decimal("99");
        chute[0] = decimal("99");
        BigDecimal[][] matrizObtida = problema.matrizA();
        matrizObtida[0][0] = decimal("88");

        solver.resolver(problema);

        assertEquals(0, problema.matrizA()[0][0].compareTo(decimal("4")));
        assertEquals(0, problema.vetorB()[0].compareTo(decimal("1")));
        assertEquals(0, problema.chuteInicial()[0].compareTo(decimal("0")));
    }

    private void assertInvalido(
            BigDecimal[][] matriz,
            BigDecimal[] vetor,
            BigDecimal[] chute,
            BigDecimal tolerancia,
            int iteracoes) {
        ProblemaGaussJacobi problema = new ProblemaGaussJacobi(
                matriz,
                vetor,
                chute,
                tolerancia,
                iteracoes);
        assertThrows(DadosMatematicosInvalidosException.class, () -> solver.resolver(problema));
    }

    private void assertDecimalProximo(BigDecimal esperado, BigDecimal atual, BigDecimal tolerancia) {
        assertTrue(esperado.subtract(atual).abs().compareTo(tolerancia) <= 0);
    }

    private BigDecimal decimal(String valor) {
        return new BigDecimal(valor);
    }

    private BigDecimal[] vetor(String... valores) {
        BigDecimal[] resultado = new BigDecimal[valores.length];
        for (int indice = 0; indice < valores.length; indice++) {
            resultado[indice] = decimal(valores[indice]);
        }
        return resultado;
    }

    private BigDecimal[][] matriz(String[]... linhas) {
        BigDecimal[][] resultado = new BigDecimal[linhas.length][];
        for (int linha = 0; linha < linhas.length; linha++) {
            resultado[linha] = vetor(linhas[linha]);
        }
        return resultado;
    }
}
