package br.ucsal.gaussjacobi.domain;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

public record IteracaoGaussJacobi(int numero, BigDecimal[] aproximacao, BigDecimal erroMaximo) {

    public IteracaoGaussJacobi {
        aproximacao = Arrays.copyOf(aproximacao, aproximacao.length);
    }

    public static IteracaoGaussJacobi inicial(BigDecimal[] chuteInicial) {
        return new IteracaoGaussJacobi(0, chuteInicial, null);
    }

    @Override
    public BigDecimal[] aproximacao() {
        return Arrays.copyOf(aproximacao, aproximacao.length);
    }

    public Optional<BigDecimal> erro() {
        return Optional.ofNullable(erroMaximo);
    }
}
