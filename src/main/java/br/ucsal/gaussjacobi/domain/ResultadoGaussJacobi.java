package br.ucsal.gaussjacobi.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record ResultadoGaussJacobi(
        int[] ordemDasEquacoes,
        BigDecimal[][] matrizUtilizada,
        BigDecimal[] vetorUtilizado,
        AnaliseCriterioDasLinhas criterioDasLinhas,
        List<IteracaoGaussJacobi> iteracoes,
        SituacaoConvergencia situacao) {

    public ResultadoGaussJacobi {
        ordemDasEquacoes = Arrays.copyOf(ordemDasEquacoes, ordemDasEquacoes.length);
        matrizUtilizada = copiarMatriz(matrizUtilizada);
        vetorUtilizado = Arrays.copyOf(vetorUtilizado, vetorUtilizado.length);
        iteracoes = List.copyOf(iteracoes);
    }

    @Override
    public int[] ordemDasEquacoes() {
        return Arrays.copyOf(ordemDasEquacoes, ordemDasEquacoes.length);
    }

    @Override
    public BigDecimal[][] matrizUtilizada() {
        return copiarMatriz(matrizUtilizada);
    }

    @Override
    public BigDecimal[] vetorUtilizado() {
        return Arrays.copyOf(vetorUtilizado, vetorUtilizado.length);
    }

    public boolean equacoesForamReordenadas() {
        for (int posicao = 0; posicao < ordemDasEquacoes.length; posicao++) {
            if (ordemDasEquacoes[posicao] != posicao) {
                return true;
            }
        }
        return false;
    }

    public boolean convergiu() {
        return situacao == SituacaoConvergencia.CONVERGIU;
    }

    public BigDecimal[] solucao() {
        return ultimaIteracao().aproximacao();
    }

    public int iteracoesRealizadas() {
        return ultimaIteracao().numero();
    }

    public BigDecimal erroMaximo() {
        return ultimaIteracao().erro().orElse(BigDecimal.ZERO);
    }

    private IteracaoGaussJacobi ultimaIteracao() {
        return iteracoes.getLast();
    }

    private static BigDecimal[][] copiarMatriz(BigDecimal[][] matriz) {
        BigDecimal[][] copia = new BigDecimal[matriz.length][];
        for (int linha = 0; linha < matriz.length; linha++) {
            copia[linha] = Arrays.copyOf(matriz[linha], matriz[linha].length);
        }
        return copia;
    }
}
