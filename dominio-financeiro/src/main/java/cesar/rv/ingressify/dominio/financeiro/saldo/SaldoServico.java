package cesar.rv.ingressify.dominio.financeiro.saldo;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class SaldoServico {

	private final SaldoRepositorio repositorio;

	public SaldoServico(SaldoRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public Saldo obter(UsuarioId usuario) {
		return repositorio.obter(usuario);
	}

	public void creditar(UsuarioId usuario, Dinheiro quantia) {
		Saldo s = repositorio.obter(usuario);
		s.creditar(quantia);
		repositorio.salvar(s);
	}

	public void debitar(UsuarioId usuario, Dinheiro quantia) {
		Saldo s = repositorio.obter(usuario);
		s.debitar(quantia);
		repositorio.salvar(s);
	}
}
