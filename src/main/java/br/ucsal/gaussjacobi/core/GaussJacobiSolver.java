package br.ucsal.gaussjacobi.core;

import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;
import br.ucsal.gaussjacobi.exception.DadosMatematicosInvalidosException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public final class GaussJacobiSolver {

    private final MathContext contextoMatematico;

    public GaussJacobiSolver() {
        this(MathContext.DECIMAL128);
    }

    public GaussJacobiSolver(MathContext contextoMatematico) {
        this.contextoMatematico = Objects.requireNonNull(contextoMatematico);
        if (contextoMatematico.getPrecision() == 0) {
            throw new IllegalArgumentException("A precisão matemática deve ser limitada.");
        }
    }

    public ResultadoGaussJacobi resolver(ProblemaGaussJacobi problema) {
        validarProblema(problema);

        BigDecimal[][] matriz = problema.matrizA();
        BigDecimal[] vetor = problema.vetorB();
        BigDecimal[] atual = problema.chuteInicial();
        BigDecimal[] proximo = new BigDecimal[atual.length];
        BigDecimal erroMaximo = BigDecimal.ZERO;

        for (int iteracao = 1; iteracao <= problema.iteracoesMaximas(); iteracao++) {
            calcularProximaAproximacao(matriz, vetor, atual, proximo);
            erroMaximo = calcularErroMaximo(atual, proximo);

            if (erroMaximo.compareTo(problema.tolerancia()) < 0) {
                return new ResultadoGaussJacobi(proximo, iteracao, erroMaximo, true);
            }

            BigDecimal[] temporario = atual;
            atual = proximo;
            proximo = temporario;
        }

        return new ResultadoGaussJacobi(atual, problema.iteracoesMaximas(), erroMaximo, false);
    }

    private void calcularProximaAproximacao(
            BigDecimal[][] matriz,
            BigDecimal[] vetor,
            BigDecimal[] atual,
            BigDecimal[] proximo) {
        for (int linha = 0; linha < vetor.length; linha++) {
            BigDecimal soma = BigDecimal.ZERO;
            for (int coluna = 0; coluna < vetor.length; coluna++) {
                if (linha != coluna) {
                    BigDecimal parcela = matriz[linha][coluna].multiply(atual[coluna], contextoMatematico);
                    soma = soma.add(parcela, contextoMatematico);
                }
            }
            BigDecimal numerador = vetor[linha].subtract(soma, contextoMatematico);
            proximo[linha] = numerador.divide(matriz[linha][linha], contextoMatematico);
        }
    }

    private BigDecimal calcularErroMaximo(BigDecimal[] atual, BigDecimal[] proximo) {
        BigDecimal erroMaximo = BigDecimal.ZERO;
        for (int indice = 0; indice < atual.length; indice++) {
            BigDecimal erro = proximo[indice].subtract(atual[indice], contextoMatematico).abs();
            if (erro.compareTo(erroMaximo) > 0) {
                erroMaximo = erro;
            }
        }
        return erroMaximo;
    }

    private void validarProblema(ProblemaGaussJacobi problema) {
        if (problema == null) {
            throw new DadosMatematicosInvalidosException("O problema não pode ser nulo.");
        }

        BigDecimal[][] matriz = problema.matrizA();
        BigDecimal[] vetor = problema.vetorB();
        BigDecimal[] chute = problema.chuteInicial();

        if (matriz == null || matriz.length == 0) {
            throw new DadosMatematicosInvalidosException("A matriz deve possuir pelo menos uma linha.");
        }

        int tamanho = matriz.length;
        for (BigDecimal[] linha : matriz) {
            if (linha == null || linha.length != tamanho) {
                throw new DadosMatematicosInvalidosException("A matriz deve ser quadrada.");
            }
        }

        if (vetor == null || vetor.length != tamanho) {
            throw new DadosMatematicosInvalidosException("O vetor B deve ter o mesmo tamanho da matriz.");
        }
        if (chute == null || chute.length != tamanho) {
            throw new DadosMatematicosInvalidosException("O chute inicial deve ter o mesmo tamanho da matriz.");
        }
        if (problema.tolerancia() == null || problema.tolerancia().signum() <= 0) {
            throw new DadosMatematicosInvalidosException("A tolerância deve ser maior que zero.");
        }
        if (problema.iteracoesMaximas() <= 0) {
            throw new DadosMatematicosInvalidosException("O máximo de iterações deve ser maior que zero.");
        }

        validarValoresDaMatriz(matriz);
        validarValoresDoVetor(vetor, "O vetor B contém um valor nulo.");
        validarValoresDoVetor(chute, "O chute inicial contém um valor nulo.");
    }

    private void validarValoresDaMatriz(BigDecimal[][] matriz) {
        for (int linha = 0; linha < matriz.length; linha++) {
            for (BigDecimal valor : matriz[linha]) {
                if (valor == null) {
                    throw new DadosMatematicosInvalidosException("A matriz contém um valor nulo.");
                }
            }
            if (matriz[linha][linha].signum() == 0) {
                throw new DadosMatematicosInvalidosException("A diagonal principal não pode conter zero.");
            }
        }
    }

    private void validarValoresDoVetor(BigDecimal[] valores, String mensagem) {
        for (BigDecimal valor : valores) {
            if (valor == null) {
                throw new DadosMatematicosInvalidosException(mensagem);
            }
        }
    }
}
