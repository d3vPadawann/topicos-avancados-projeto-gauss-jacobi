package br.ucsal.gaussjacobi.core;

import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;
import br.ucsal.gaussjacobi.exception.DadosMatematicosInvalidosException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;

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
                matriz(new String[]{"1", "2"}, new String[]{"2", "1"}),
                vetor("1", "1"),
                vetor("0", "0"),
                decimal("1E-30"),
                3);

        ResultadoGaussJacobi resultado = solver.resolver(problema);

        assertFalse(resultado.convergiu());
        assertEquals(3, resultado.iteracoesRealizadas());
        assertEquals(0, resultado.solucao()[0].compareTo(decimal("3")));
        assertEquals(0, resultado.solucao()[1].compareTo(decimal("3")));
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
