import java.util.Scanner;
import java.util.Arrays;
import java.util.Locale;

public class GaussJacobi {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);

        int tamanho = lerInteiroPositivo(scanner, "Tamanho do sistema (n): ");

        double[][] matrizA = new double[tamanho][tamanho];
        for (int i = 0; i < tamanho; i++) {
            System.out.print("Linha " + (i + 1) + " da matriz A (valores separados por espaco): ");
            for (int j = 0; j < tamanho; j++) {
                matrizA[i][j] = lerDouble(scanner);
            }
        }

        double[] vetorB = new double[tamanho];
        System.out.print("Vetor B (valores separados por espaco): ");
        for (int i = 0; i < tamanho; i++) {
            vetorB[i] = lerDouble(scanner);
        }

        double[] chuteInicial = new double[tamanho];
        System.out.print("Chute inicial (valores separados por espaco): ");
        for (int i = 0; i < tamanho; i++) {
            chuteInicial[i] = lerDouble(scanner);
        }

        double tolerancia = lerDoublePositivo(scanner, "Tolerancia: ");
        int iteracoesMaximas = lerInteiroPositivo(scanner, "Maximo de iteracoes: ");

        try {
            resolverGaussJacobi(matrizA, vetorB, tolerancia, iteracoesMaximas, chuteInicial);
        } catch (DiagonalNulaException | NaoConvergiuException e) {
            System.out.println(e.getMessage());
        }

        scanner.close();
    }

    public static int lerInteiroPositivo(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (true) {
            if (!scanner.hasNextInt()) {
                System.out.print("Entrada invalida. Digite um numero inteiro maior que zero: ");
                scanner.next();
                continue;
            }
            int valor = scanner.nextInt();
            if (valor <= 0) {
                System.out.print("O valor deve ser maior que zero. Digite novamente: ");
                continue;
            }
            return valor;
        }
    }

    public static double lerDoublePositivo(Scanner scanner, String prompt) {
        System.out.print(prompt);
        while (true) {
            if (!scanner.hasNextDouble()) {
                System.out.print("Entrada invalida. Digite um numero decimal valido e maior que zero: ");
                scanner.next();
                continue;
            }
            double valor = scanner.nextDouble();
            if (valor <= 0) {
                System.out.print("O valor deve ser maior que zero. Digite novamente: ");
                continue;
            }
            return valor;
        }
    }

    public static double lerDouble(Scanner scanner) {
        while (!scanner.hasNextDouble()) {
            System.out.print("Entrada invalida. Digite um numero valido: ");
            scanner.next();
        }
        return scanner.nextDouble();
    }

    public static void resolverGaussJacobi(double[][] matrizA, double[] vetorB, double tolerancia, int iteracoesMaximas, double[] chuteInicial) throws DiagonalNulaException, NaoConvergiuException {
        int tamanho = vetorB.length;

        for (int i = 0; i < tamanho; i++) {
            if (matrizA[i][i] == 0.0) {
                throw new DiagonalNulaException("\nErro: Elemento nulo encontrado na diagonal principal na linha " + (i + 1) + ". O metodo de Gauss-Jacobi nao permite divisao por zero.");
            }
        }

        double[] xAtual = Arrays.copyOf(chuteInicial, tamanho);
        double[] xNovo = new double[tamanho];
        int iteracoesRealizadas = 0;
        boolean convergiu = false;

        for (int k = 0; k < iteracoesMaximas; k++) {
            for (int i = 0; i < tamanho; i++) {
                double soma = 0.0;
                for (int j = 0; j < tamanho; j++) {
                    if (i != j) {
                        soma += matrizA[i][j] * xAtual[j];
                    }
                }
                xNovo[i] = (vetorB[i] - soma) / matrizA[i][i];
            }

            double erroMaximo = 0.0;
            for (int i = 0; i < tamanho; i++) {
                double erroAbsoluto = Math.abs(xNovo[i] - xAtual[i]);
                if (erroAbsoluto > erroMaximo) {
                    erroMaximo = erroAbsoluto;
                }
            }

            if (erroMaximo < tolerancia) {
                convergiu = true;
                iteracoesRealizadas = k + 1;
                break;
            }

            System.arraycopy(xNovo, 0, xAtual, 0, tamanho);
            iteracoesRealizadas = k + 1;
        }

        if (convergiu) {
            System.out.println("\nSolucao encontrada:");
            for (int i = 0; i < tamanho; i++) {
                System.out.printf("x%d = %.6f\n", i + 1, xNovo[i]);
            }
            System.out.println("Iteracoes realizadas: " + iteracoesRealizadas);
        } else {
            throw new NaoConvergiuException("\nO metodo nao convergiu dentro do limite de " + iteracoesMaximas + " iteracoes.");
        }
    }
}

class NaoConvergiuException extends Exception {
    public NaoConvergiuException(String mensagem) {
        super(mensagem);
    }
}

class DiagonalNulaException extends Exception {
    public DiagonalNulaException(String mensagem) {
        super(mensagem);
    }
}
