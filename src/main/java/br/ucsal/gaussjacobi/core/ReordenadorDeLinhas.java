package br.ucsal.gaussjacobi.core;

import br.ucsal.gaussjacobi.exception.DadosMatematicosInvalidosException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.IntStream;

public final class ReordenadorDeLinhas {

    @FunctionalInterface
    private interface CondicaoDaPosicao {
        boolean aceita(int linha, int posicao);
    }

    public int[] definirOrdem(BigDecimal[][] matriz) {
        int[] ordemOriginal = IntStream.range(0, matriz.length).toArray();
        CondicaoDaPosicao dominante = (linha, posicao) -> ehDominanteNaPosicao(matriz[linha], posicao);
        CondicaoDaPosicao semZero = (linha, posicao) -> matriz[linha][posicao].signum() != 0;

        if (ordemAtende(ordemOriginal, dominante)) {
            return ordemOriginal;
        }

        int[] ordemDominante = buscarOrdem(matriz.length, dominante);
        if (ordemDominante != null) {
            return ordemDominante;
        }

        if (ordemAtende(ordemOriginal, semZero)) {
            return ordemOriginal;
        }

        int[] ordemSemZeroNaDiagonal = buscarOrdem(matriz.length, semZero);
        if (ordemSemZeroNaDiagonal != null) {
            return ordemSemZeroNaDiagonal;
        }

        throw new DadosMatematicosInvalidosException(
                "A diagonal principal contém zero e nenhuma troca de ordem das equações consegue eliminá-lo.");
    }

    private boolean ehDominanteNaPosicao(BigDecimal[] linha, int posicao) {
        BigDecimal somaForaDaDiagonal = BigDecimal.ZERO;
        for (int coluna = 0; coluna < linha.length; coluna++) {
            if (coluna != posicao) {
                somaForaDaDiagonal = somaForaDaDiagonal.add(linha[coluna].abs());
            }
        }
        return linha[posicao].abs().compareTo(somaForaDaDiagonal) > 0;
    }

    private boolean ordemAtende(int[] ordem, CondicaoDaPosicao condicao) {
        for (int posicao = 0; posicao < ordem.length; posicao++) {
            if (!condicao.aceita(ordem[posicao], posicao)) {
                return false;
            }
        }
        return true;
    }

    private int[] buscarOrdem(int tamanho, CondicaoDaPosicao condicao) {
        int[] linhaNaPosicao = new int[tamanho];
        Arrays.fill(linhaNaPosicao, -1);
        for (int linha = 0; linha < tamanho; linha++) {
            if (!encaixarLinha(linha, condicao, linhaNaPosicao, new boolean[tamanho])) {
                return null;
            }
        }
        return linhaNaPosicao;
    }

    private boolean encaixarLinha(int linha, CondicaoDaPosicao condicao, int[] linhaNaPosicao, boolean[] posicaoVisitada) {
        int tamanho = linhaNaPosicao.length;
        for (int deslocamento = 0; deslocamento < tamanho; deslocamento++) {
            int posicao = (linha + deslocamento) % tamanho;
            if (posicaoVisitada[posicao] || !condicao.aceita(linha, posicao)) {
                continue;
            }
            posicaoVisitada[posicao] = true;
            int linhaOcupante = linhaNaPosicao[posicao];
            if (linhaOcupante == -1 || encaixarLinha(linhaOcupante, condicao, linhaNaPosicao, posicaoVisitada)) {
                linhaNaPosicao[posicao] = linha;
                return true;
            }
        }
        return false;
    }
}
