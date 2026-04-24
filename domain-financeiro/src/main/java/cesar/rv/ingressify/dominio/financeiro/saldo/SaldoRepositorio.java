package cesar.rv.ingressify.dominio.financeiro.saldo;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface SaldoRepositorio {

	void salvar(Saldo saldo);

	Saldo obter(UsuarioId usuario);
}
