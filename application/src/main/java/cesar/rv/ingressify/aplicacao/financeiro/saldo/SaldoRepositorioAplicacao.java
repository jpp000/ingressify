package cesar.rv.ingressify.aplicacao.financeiro.saldo;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface SaldoRepositorioAplicacao {

	SaldoResumo obterResumo(UsuarioId usuario);
}
