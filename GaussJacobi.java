import java.util.Scanner;
import java.util.Arrays;
import java.util.Locale;

public class GaussJacobi {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        scanner.useLocale(Locale.US);

        System.out.print("Tamanho do sistema (n): ");
        int tamanho = scanner.nextInt();

        double[][] matrizA = new double[tamanho][tamanho];
        for (int i = 0; i < tamanho; i++) {
            System.out.print("Linha " + (i + 1) + " da matriz A (valores separados por espaco): ");
            for (int j = 0; j < tamanho; j++) {
                matrizA[i][j] = scanner.nextDouble();
            }
        }

        double[] vetorB = new double[tamanho];
        System.out.print("Vetor B (valores separados por espaco): ");
        for (int i = 0; i < tamanho; i++) {
            vetorB[i] = scanner.nextDouble();
        }

        double[] chuteInicial = new double[tamanho];
        System.out.print("Chute inicial (valores separados por espaco): ");
        for (int i = 0; i < tamanho; i++) {
            chuteInicial[i] = scanner.nextDouble();
        }

        System.out.print("Tolerancia: ");
        double tolerancia = scanner.nextDouble();

        System.out.print("Maximo de iteracoes: ");
        int iteracoesMaximas = scanner.nextInt();

        try {
            resolverGaussJacobi(matrizA, vetorB, tolerancia, iteracoesMaximas, chuteInicial);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        scanner.close();
    }

    public static void resolverGaussJacobi(double[][] matrizA, double[] vetorB, double tolerancia, int iteracoesMaximas, double[] chuteInicial) {
        int tamanho = vetorB.length;
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
            throw new RuntimeException("\nO metodo nao convergiu dentro do limite de iteracoes.");
        }
    }
}
