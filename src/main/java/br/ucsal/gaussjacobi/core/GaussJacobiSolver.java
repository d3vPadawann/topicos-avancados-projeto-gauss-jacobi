package br.ucsal.gaussjacobi.core;

import br.ucsal.gaussjacobi.domain.AnaliseCriterioDasLinhas;
import br.ucsal.gaussjacobi.domain.IteracaoGaussJacobi;
import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;
import br.ucsal.gaussjacobi.domain.SituacaoConvergencia;
import br.ucsal.gaussjacobi.exception.DadosMatematicosInvalidosException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GaussJacobiSolver {

    private static final BigDecimal FATOR_DE_CRESCIMENTO_DIVERGENTE = new BigDecimal("1E10");

    private final MathContext contextoMatematico;
    private final ReordenadorDeLinhas reordenadorDeLinhas;
    private final CriterioDasLinhas criterioDasLinhas;

    public GaussJacobiSolver() {
        this(MathContext.DECIMAL128);
    }

    public GaussJacobiSolver(MathContext contextoMatematico) {
        this.contextoMatematico = Objects.requireNonNull(contextoMatematico);
        if (contextoMatematico.getPrecision() == 0) {
            throw new IllegalArgumentException("A precisão matemática deve ser limitada.");
        }
        this.reordenadorDeLinhas = new ReordenadorDeLinhas();
        this.criterioDasLinhas = new CriterioDasLinhas(contextoMatematico);
    }

    public ResultadoGaussJacobi resolver(ProblemaGaussJacobi problema) {
        validarProblema(problema);

        int[] ordemDasEquacoes = reordenadorDeLinhas.definirOrdem(problema.matrizA());
        BigDecimal[][] matriz = reordenarLinhas(problema.matrizA(), ordemDasEquacoes);
        BigDecimal[] vetor = reordenarElementos(problema.vetorB(), ordemDasEquacoes);
        AnaliseCriterioDasLinhas analise = criterioDasLinhas.analisar(matriz);

        List<IteracaoGaussJacobi> iteracoes = new ArrayList<>();
        BigDecimal[] atual = problema.chuteInicial();
        BigDecimal[] proximo = new BigDecimal[atual.length];
        BigDecimal primeiroErro = null;
        SituacaoConvergencia situacao = SituacaoConvergencia.NAO_CONVERGIU;
        iteracoes.add(IteracaoGaussJacobi.inicial(atual));

        for (int iteracao = 1; iteracao <= problema.iteracoesMaximas(); iteracao++) {
            calcularProximaAproximacao(matriz, vetor, atual, proximo);
            BigDecimal erroMaximo = calcularErroMaximo(atual, proximo);
            iteracoes.add(new IteracaoGaussJacobi(iteracao, proximo, erroMaximo));

            if (erroMaximo.compareTo(problema.tolerancia()) < 0) {
                situacao = SituacaoConvergencia.CONVERGIU;
                break;
            }
            if (primeiroErro == null) {
                primeiroErro = erroMaximo;
            } else if (estaDivergindo(primeiroErro, erroMaximo)) {
                situacao = SituacaoConvergencia.DIVERGIU;
                break;
            }

            BigDecimal[] temporario = atual;
            atual = proximo;
            proximo = temporario;
        }

        return new ResultadoGaussJacobi(ordemDasEquacoes, matriz, vetor, analise, iteracoes, situacao);
    }

    private boolean estaDivergindo(BigDecimal primeiroErro, BigDecimal erroAtual) {
        BigDecimal limite = primeiroErro.multiply(FATOR_DE_CRESCIMENTO_DIVERGENTE, contextoMatematico);
        return erroAtual.compareTo(limite) > 0;
    }

    private BigDecimal[][] reordenarLinhas(BigDecimal[][] matriz, int[] ordemDasEquacoes) {
        BigDecimal[][] reordenada = new BigDecimal[matriz.length][];
        for (int posicao = 0; posicao < matriz.length; posicao++) {
            reordenada[posicao] = matriz[ordemDasEquacoes[posicao]];
        }
        return reordenada;
    }

    private BigDecimal[] reordenarElementos(BigDecimal[] vetor, int[] ordemDasEquacoes) {
        BigDecimal[] reordenado = new BigDecimal[vetor.length];
        for (int posicao = 0; posicao < vetor.length; posicao++) {
            reordenado[posicao] = vetor[ordemDasEquacoes[posicao]];
        }
        return reordenado;
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
        for (BigDecimal[] linha : matriz) {
            for (BigDecimal valor : linha) {
                if (valor == null) {
                    throw new DadosMatematicosInvalidosException("A matriz contém um valor nulo.");
                }
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
