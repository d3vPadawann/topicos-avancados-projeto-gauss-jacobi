package br.ucsal.gaussjacobi.ui;

import br.ucsal.gaussjacobi.domain.AnaliseCriterioDasLinhas;
import br.ucsal.gaussjacobi.domain.IteracaoGaussJacobi;
import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.domain.ResultadoGaussJacobi;
import br.ucsal.gaussjacobi.exception.FalhaInterfaceUsuarioException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class InterfaceUsuarioTerminal implements InterfaceUsuario {

    private static final int CASAS_DECIMAIS_DA_RESPOSTA = 6;
    private static final String SEPARADOR_DE_COLUNAS = "   ";

    private final BufferedReader entrada;
    private final PrintWriter saida;

    public InterfaceUsuarioTerminal() {
        this(System.in, System.out);
    }

    public InterfaceUsuarioTerminal(InputStream entrada, OutputStream saida) {
        this(
                new InputStreamReader(Objects.requireNonNull(entrada), StandardCharsets.UTF_8),
                new OutputStreamWriter(Objects.requireNonNull(saida), StandardCharsets.UTF_8));
    }

    public InterfaceUsuarioTerminal(Reader entrada, Writer saida) {
        this.entrada = new BufferedReader(Objects.requireNonNull(entrada));
        this.saida = new PrintWriter(Objects.requireNonNull(saida), true);
    }

    @Override
    public Optional<ProblemaGaussJacobi> solicitarProblema() {
        try {
            int tamanho = lerInteiroPositivo("Tamanho do sistema (n): ");
            BigDecimal[][] matriz = new BigDecimal[tamanho][tamanho];
            for (int linha = 0; linha < tamanho; linha++) {
                matriz[linha] = lerVetor(
                        "Linha " + (linha + 1) + " da matriz A (valores separados por espaço): ",
                        tamanho);
            }

            BigDecimal[] vetor = lerVetor("Vetor B (valores separados por espaço): ", tamanho);
            BigDecimal[] chute = lerVetor("Chute inicial (valores separados por espaço): ", tamanho);
            BigDecimal tolerancia = lerDecimalPositivo("Tolerância: ");
            int iteracoes = lerInteiroPositivo("Máximo de iterações: ");

            return Optional.of(new ProblemaGaussJacobi(matriz, vetor, chute, tolerancia, iteracoes));
        } catch (FimDaEntradaException excecao) {
            return Optional.empty();
        } catch (IOException excecao) {
            throw new FalhaInterfaceUsuarioException("Não foi possível ler os dados de entrada.", excecao);
        }
    }

    @Override
    public void exibirResultado(ResultadoGaussJacobi resultado) {
        if (resultado.equacoesForamReordenadas()) {
            exibirReordenacao(resultado);
        }
        exibirCriterioDasLinhas(resultado.criterioDasLinhas());
        exibirIteracoes(resultado.iteracoes());
        exibirConclusao(resultado);
        saida.flush();
    }

    private void exibirReordenacao(ResultadoGaussJacobi resultado) {
        saida.println();
        saida.println("As equações foram reordenadas para colocar os maiores coeficientes na diagonal principal.");
        StringBuilder novaOrdem = new StringBuilder("Nova ordem:");
        for (int equacaoOriginal : resultado.ordemDasEquacoes()) {
            novaOrdem.append(" E").append(equacaoOriginal + 1);
        }
        saida.println(novaOrdem);
        saida.println("Sistema utilizado:");

        BigDecimal[][] matriz = resultado.matrizUtilizada();
        BigDecimal[] vetor = resultado.vetorUtilizado();
        List<String[]> linhasDoSistema = new ArrayList<>();
        for (int linha = 0; linha < matriz.length; linha++) {
            String[] celulas = new String[matriz.length + 1];
            for (int coluna = 0; coluna < matriz.length; coluna++) {
                celulas[coluna] = formatarCompleto(matriz[linha][coluna]) + "*x" + (coluna + 1);
            }
            celulas[matriz.length] = "= " + formatarCompleto(vetor[linha]);
            linhasDoSistema.add(celulas);
        }
        exibirTabela(linhasDoSistema);
    }

    private void exibirCriterioDasLinhas(AnaliseCriterioDasLinhas analise) {
        saida.println();
        saida.println("Critério das linhas (alfa = soma dos |coeficientes fora da diagonal| / |coeficiente da diagonal|):");
        BigDecimal[] alfas = analise.alfas();
        for (int linha = 0; linha < alfas.length; linha++) {
            saida.println("  alfa" + (linha + 1) + " = " + formatarCompleto(alfas[linha]));
        }
        saida.println("  alfa máximo = " + formatarCompleto(analise.alfaMaximo()));
        if (analise.garanteConvergencia()) {
            saida.println("Critério satisfeito (alfa máximo < 1): a convergência é garantida.");
        } else {
            saida.println("Critério NÃO satisfeito (alfa máximo >= 1): a convergência não é garantida.");
            saida.println("O método ainda pode convergir, mas também pode divergir.");
        }
    }

    private void exibirIteracoes(List<IteracaoGaussJacobi> iteracoes) {
        saida.println();
        saida.println("Iterações (erro = maior |x(k) - x(k-1)|):");
        int quantidadeDeIncognitas = iteracoes.getFirst().aproximacao().length;
        List<String[]> linhasDaTabela = new ArrayList<>();

        String[] cabecalho = new String[quantidadeDeIncognitas + 2];
        cabecalho[0] = "k";
        for (int indice = 0; indice < quantidadeDeIncognitas; indice++) {
            cabecalho[indice + 1] = "x" + (indice + 1);
        }
        cabecalho[quantidadeDeIncognitas + 1] = "erro";
        linhasDaTabela.add(cabecalho);

        for (IteracaoGaussJacobi iteracao : iteracoes) {
            String[] celulas = new String[quantidadeDeIncognitas + 2];
            celulas[0] = String.valueOf(iteracao.numero());
            BigDecimal[] aproximacao = iteracao.aproximacao();
            for (int indice = 0; indice < quantidadeDeIncognitas; indice++) {
                celulas[indice + 1] = formatarCompleto(aproximacao[indice]);
            }
            celulas[quantidadeDeIncognitas + 1] = iteracao.erro().map(this::formatarCompleto).orElse("-");
            linhasDaTabela.add(celulas);
        }
        exibirTabela(linhasDaTabela);
    }

    private void exibirTabela(List<String[]> linhas) {
        int[] larguras = new int[linhas.getFirst().length];
        for (String[] celulas : linhas) {
            for (int coluna = 0; coluna < celulas.length; coluna++) {
                larguras[coluna] = Math.max(larguras[coluna], celulas[coluna].length());
            }
        }

        for (String[] celulas : linhas) {
            StringBuilder linha = new StringBuilder();
            for (int coluna = 0; coluna < celulas.length; coluna++) {
                linha.append(SEPARADOR_DE_COLUNAS).append(" ".repeat(larguras[coluna] - celulas[coluna].length()))
                        .append(celulas[coluna]);
            }
            saida.println(linha);
        }
    }

    private void exibirConclusao(ResultadoGaussJacobi resultado) {
        saida.println();
        switch (resultado.situacao()) {
            case CONVERGIU -> saida.println("Solução encontrada:");
            case NAO_CONVERGIU -> {
                saida.println("O método não convergiu dentro do limite de iterações.");
                saida.println("Última aproximação calculada:");
            }
            case DIVERGIU -> {
                saida.println("O método divergiu: o erro cresce a cada iteração e os valores se afastam da solução.");
                saida.println("Última aproximação calculada:");
            }
        }

        BigDecimal[] solucao = resultado.solucao();
        for (int indice = 0; indice < solucao.length; indice++) {
            saida.println("x" + (indice + 1) + " = " + formatarResposta(solucao[indice]));
        }

        saida.println("Iterações realizadas: " + resultado.iteracoesRealizadas());
        saida.println("Erro máximo: " + formatarCompleto(resultado.erroMaximo()));
    }

    @Override
    public void exibirErro(String mensagem) {
        saida.println("Erro: " + mensagem);
        saida.flush();
    }

    @Override
    public boolean desejaResolverOutroSistema() {
        try {
            while (true) {
                String resposta = lerLinha("Deseja resolver outro sistema? [s/n]: ").trim().toLowerCase(Locale.ROOT);
                if (resposta.equals("s") || resposta.equals("sim")) {
                    return true;
                }
                if (resposta.equals("n") || resposta.equals("não") || resposta.equals("nao")) {
                    return false;
                }
                saida.println("Resposta inválida. Digite s ou n.");
            }
        } catch (FimDaEntradaException excecao) {
            return false;
        } catch (IOException excecao) {
            throw new FalhaInterfaceUsuarioException("Não foi possível ler a resposta do usuário.", excecao);
        }
    }

    private int lerInteiroPositivo(String mensagem) throws IOException, FimDaEntradaException {
        while (true) {
            String valor = lerLinha(mensagem).trim();
            try {
                int numero = Integer.parseInt(valor);
                if (numero > 0) {
                    return numero;
                }
            } catch (NumberFormatException excecao) {
                saida.println("Valor inválido. Digite um número inteiro maior que zero.");
                continue;
            }
            saida.println("Valor inválido. Digite um número inteiro maior que zero.");
        }
    }

    private BigDecimal lerDecimalPositivo(String mensagem) throws IOException, FimDaEntradaException {
        while (true) {
            String valor = lerLinha(mensagem).trim();
            try {
                BigDecimal numero = converterDecimal(valor);
                if (numero.signum() > 0) {
                    return numero;
                }
            } catch (NumberFormatException excecao) {
                saida.println("Valor inválido. Digite um número decimal maior que zero.");
                continue;
            }
            saida.println("Valor inválido. Digite um número decimal maior que zero.");
        }
    }

    private BigDecimal[] lerVetor(String mensagem, int tamanho) throws IOException, FimDaEntradaException {
        while (true) {
            String linha = lerLinha(mensagem).trim();
            String[] valores = linha.isEmpty() ? new String[0] : linha.split("\\s+");
            if (valores.length != tamanho) {
                saida.println("Quantidade inválida. Informe exatamente " + tamanho + " valores.");
                continue;
            }

            try {
                BigDecimal[] vetor = new BigDecimal[tamanho];
                for (int indice = 0; indice < tamanho; indice++) {
                    vetor[indice] = converterDecimal(valores[indice]);
                }
                return vetor;
            } catch (NumberFormatException excecao) {
                saida.println("Valor inválido. Informe apenas números decimais separados por espaço.");
            }
        }
    }

    private BigDecimal converterDecimal(String valor) {
        return new BigDecimal(valor.replace(',', '.'));
    }

    private String formatarCompleto(BigDecimal valor) {
        return valor.stripTrailingZeros().toPlainString();
    }

    private String formatarResposta(BigDecimal valor) {
        return valor.setScale(CASAS_DECIMAIS_DA_RESPOSTA, RoundingMode.HALF_UP).toPlainString();
    }

    private String lerLinha(String mensagem) throws IOException, FimDaEntradaException {
        saida.print(mensagem);
        saida.flush();
        String linha = entrada.readLine();
        if (linha == null) {
            throw new FimDaEntradaException();
        }
        return linha;
    }

    private static final class FimDaEntradaException extends Exception {
    }
}
