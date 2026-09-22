package br.ucsal.gaussjacobi.ui;

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
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class InterfaceUsuarioTerminal implements InterfaceUsuario {

    private static final int SCALE = 6;

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
                        "Linha " + (linha + 1) + " da matriz A (valores separados por espaco): ",
                        tamanho);
            }

            BigDecimal[] vetor = lerVetor("Vetor B (valores separados por espaco): ", tamanho);
            BigDecimal[] chute = lerVetor("Chute inicial (valores separados por espaco): ", tamanho);
            BigDecimal tolerancia = lerDecimalPositivo("Tolerancia: ");
            int iteracoes = lerInteiroPositivo("Maximo de iteracoes: ");

            return Optional.of(new ProblemaGaussJacobi(matriz, vetor, chute, tolerancia, iteracoes));
        } catch (FimDaEntradaException excecao) {
            return Optional.empty();
        } catch (IOException excecao) {
            throw new FalhaInterfaceUsuarioException("Nao foi possivel ler os dados de entrada.", excecao);
        }
    }

    @Override
    public void exibirResultado(ResultadoGaussJacobi resultado) {
        saida.println();
        if (resultado.convergiu()) {
            saida.println("Solucao encontrada:");
        } else {
            saida.println("O metodo nao convergiu dentro do limite de iteracoes.");
            saida.println("Ultima aproximacao calculada:");
        }

        BigDecimal[] solucao = resultado.solucao();
        for (int indice = 0; indice < solucao.length; indice++) {
            saida.println("x" + (indice + 1) + " = " + formatar(solucao[indice]));
        }

        saida.println("Iteracoes realizadas: " + resultado.iteracoesRealizadas());
        saida.println("Erro maximo: " + resultado.erroMaximo().stripTrailingZeros().toEngineeringString());
        saida.flush();
    }

    @Override
    public void exibirErro(String mensagem) {
        saida.println("Erro: " + removerAcentos(mensagem));
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
                if (resposta.equals("n") || resposta.equals("nao")) {
                    return false;
                }
                saida.println("Resposta invalida. Digite s ou n.");
            }
        } catch (FimDaEntradaException excecao) {
            return false;
        } catch (IOException excecao) {
            throw new FalhaInterfaceUsuarioException("Nao foi possivel ler a resposta do usuario.", excecao);
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
                saida.println("Valor invalido. Digite um numero inteiro maior que zero.");
                continue;
            }
            saida.println("Valor invalido. Digite um numero inteiro maior que zero.");
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
                saida.println("Valor invalido. Digite um numero decimal maior que zero.");
                continue;
            }
            saida.println("Valor invalido. Digite um numero decimal maior que zero.");
        }
    }

    private BigDecimal[] lerVetor(String mensagem, int tamanho) throws IOException, FimDaEntradaException {
        while (true) {
            String linha = lerLinha(mensagem).trim();
            String[] valores = linha.isEmpty() ? new String[0] : linha.split("\\s+");
            if (valores.length != tamanho) {
                saida.println("Quantidade invalida. Informe exatamente " + tamanho + " valores.");
                continue;
            }

            try {
                BigDecimal[] vetor = new BigDecimal[tamanho];
                for (int indice = 0; indice < tamanho; indice++) {
                    vetor[indice] = converterDecimal(valores[indice]);
                }
                return vetor;
            } catch (NumberFormatException excecao) {
                saida.println("Valor invalido. Informe apenas numeros decimais separados por espaco.");
            }
        }
    }

    private BigDecimal converterDecimal(String valor) {
        return new BigDecimal(valor.replace(',', '.'));
    }

    private String formatar(BigDecimal valor) {
        return valor
            .setScale(SCALE, RoundingMode.DOWN)
            .stripTrailingZeros()
            .toPlainString();
    }

    private String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
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
