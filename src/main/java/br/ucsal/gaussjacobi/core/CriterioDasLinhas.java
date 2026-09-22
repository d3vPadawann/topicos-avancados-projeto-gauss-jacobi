package br.ucsal.gaussjacobi.core;

import br.ucsal.gaussjacobi.domain.AnaliseCriterioDasLinhas;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public final class CriterioDasLinhas {

    private final MathContext contextoMatematico;

    public CriterioDasLinhas(MathContext contextoMatematico) {
        this.contextoMatematico = Objects.requireNonNull(contextoMatematico);
    }

    public AnaliseCriterioDasLinhas analisar(BigDecimal[][] matriz) {
        BigDecimal[] alfas = new BigDecimal[matriz.length];
        for (int linha = 0; linha < matriz.length; linha++) {
            alfas[linha] = calcularAlfa(matriz, linha);
        }
        return new AnaliseCriterioDasLinhas(alfas);
    }

    private BigDecimal calcularAlfa(BigDecimal[][] matriz, int linha) {
        BigDecimal somaForaDaDiagonal = BigDecimal.ZERO;
        for (int coluna = 0; coluna < matriz[linha].length; coluna++) {
            if (coluna != linha) {
                somaForaDaDiagonal = somaForaDaDiagonal.add(matriz[linha][coluna].abs(), contextoMatematico);
            }
        }
        return somaForaDaDiagonal.divide(matriz[linha][linha].abs(), contextoMatematico);
    }
}
