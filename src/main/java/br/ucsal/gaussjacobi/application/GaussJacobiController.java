package br.ucsal.gaussjacobi.application;

import br.ucsal.gaussjacobi.core.GaussJacobiSolver;
import br.ucsal.gaussjacobi.domain.ProblemaGaussJacobi;
import br.ucsal.gaussjacobi.exception.DadosMatematicosInvalidosException;
import br.ucsal.gaussjacobi.exception.FalhaInterfaceUsuarioException;
import br.ucsal.gaussjacobi.ui.InterfaceUsuario;

import java.util.Objects;
import java.util.Optional;

public final class GaussJacobiController {

    private final InterfaceUsuario interfaceUsuario;
    private final GaussJacobiSolver solver;

    public GaussJacobiController(InterfaceUsuario interfaceUsuario, GaussJacobiSolver solver) {
        this.interfaceUsuario = Objects.requireNonNull(interfaceUsuario);
        this.solver = Objects.requireNonNull(solver);
    }

    public void executar() {
        boolean continuar = true;
        while (continuar) {
            continuar = executarResolucao();
        }
    }

    private boolean executarResolucao() {
        try {
            Optional<ProblemaGaussJacobi> problema = interfaceUsuario.solicitarProblema();
            if (problema.isEmpty()) {
                return false;
            }
            interfaceUsuario.exibirResultado(solver.resolver(problema.get()));
            return desejaResolverOutroSistema();
        } catch (FalhaInterfaceUsuarioException excecao) {
            exibirErroSemPropagar(excecao.getMessage());
            return false;
        } catch (DadosMatematicosInvalidosException excecao) {
            if (!exibirErroSemPropagar(excecao.getMessage())) {
                return false;
            }
            return desejaResolverOutroSistema();
        } catch (RuntimeException excecao) {
            if (!exibirErroSemPropagar("Ocorreu um erro inesperado durante a resolução.")) {
                return false;
            }
            return desejaResolverOutroSistema();
        }
    }

    private boolean desejaResolverOutroSistema() {
        try {
            return interfaceUsuario.desejaResolverOutroSistema();
        } catch (RuntimeException excecao) {
            exibirErroSemPropagar("Não foi possível continuar a leitura dos dados.");
            return false;
        }
    }

    private boolean exibirErroSemPropagar(String mensagem) {
        try {
            interfaceUsuario.exibirErro(mensagem);
            return true;
        } catch (RuntimeException excecao) {
            return false;
        }
    }
}
