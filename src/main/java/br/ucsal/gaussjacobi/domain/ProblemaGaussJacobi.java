package br.ucsal.gaussjacobi.domain;

import java.math.BigDecimal;
import java.util.Arrays;

public record ProblemaGaussJacobi(
        BigDecimal[][] matrizA,
        BigDecimal[] vetorB,
        BigDecimal[] chuteInicial,
        BigDecimal tolerancia,
        int iteracoesMaximas) {

    public ProblemaGaussJacobi {
        matrizA = copiarMatriz(matrizA);
        vetorB = copiarVetor(vetorB);
        chuteInicial = copiarVetor(chuteInicial);
    }

    @Override
    public BigDecimal[][] matrizA() {
        return copiarMatriz(matrizA);
    }

    @Override
    public BigDecimal[] vetorB() {
        return copiarVetor(vetorB);
    }

    @Override
    public BigDecimal[] chuteInicial() {
        return copiarVetor(chuteInicial);
    }

    private static BigDecimal[][] copiarMatriz(BigDecimal[][] matriz) {
        if (matriz == null) {
            return null;
        }
        BigDecimal[][] copia = new BigDecimal[matriz.length][];
        for (int linha = 0; linha < matriz.length; linha++) {
            copia[linha] = copiarVetor(matriz[linha]);
        }
        return copia;
    }

    private static BigDecimal[] copiarVetor(BigDecimal[] vetor) {
        return vetor == null ? null : Arrays.copyOf(vetor, vetor.length);
    }
}
