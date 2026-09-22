package br.ucsal.gaussjacobi.domain;

import java.math.BigDecimal;
import java.util.Arrays;

public record ResultadoGaussJacobi(
        BigDecimal[] solucao,
        int iteracoesRealizadas,
        BigDecimal erroMaximo,
        boolean convergiu) {

    public ResultadoGaussJacobi {
        solucao = solucao == null ? null : Arrays.copyOf(solucao, solucao.length);
    }

    @Override
    public BigDecimal[] solucao() {
        return solucao == null ? null : Arrays.copyOf(solucao, solucao.length);
    }
}
