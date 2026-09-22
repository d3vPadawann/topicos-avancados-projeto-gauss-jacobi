package br.ucsal.gaussjacobi;

import br.ucsal.gaussjacobi.application.GaussJacobiController;
import br.ucsal.gaussjacobi.core.GaussJacobiSolver;
import br.ucsal.gaussjacobi.ui.InterfaceUsuario;
import br.ucsal.gaussjacobi.ui.InterfaceUsuarioTerminal;

public final class GaussJacobiApplication {

    private GaussJacobiApplication() {
    }

    public static void main(String[] args) {
        try {
            InterfaceUsuario interfaceUsuario = new InterfaceUsuarioTerminal();
            GaussJacobiSolver solver = new GaussJacobiSolver();
            new GaussJacobiController(interfaceUsuario, solver).executar();
        } catch (RuntimeException excecao) {
            System.err.println("Não foi possível executar a aplicação: " + mensagemDa(excecao));
        }
    }

    private static String mensagemDa(RuntimeException excecao) {
        String mensagem = excecao.getMessage();
        return mensagem == null || mensagem.isBlank() ? "erro inesperado" : mensagem;
    }
}
