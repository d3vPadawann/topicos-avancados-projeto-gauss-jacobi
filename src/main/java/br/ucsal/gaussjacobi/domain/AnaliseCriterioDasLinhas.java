package br.ucsal.gaussjacobi.domain;

import java.math.BigDecimal;
import java.util.Arrays;

public record AnaliseCriterioDasLinhas(BigDecimal[] alfas) {

    public AnaliseCriterioDasLinhas {
        alfas = Arrays.copyOf(alfas, alfas.length);
    }

    @Override
    public BigDecimal[] alfas() {
        return Arrays.copyOf(alfas, alfas.length);
    }

    public BigDecimal alfaMaximo() {
        return Arrays.stream(alfas).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public boolean garanteConvergencia() {
        return alfaMaximo().compareTo(BigDecimal.ONE) < 0;
    }
}
