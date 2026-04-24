package cesar.rv.ingressify.aplicacao.financeiro.saldo;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Service
public class SaldoServicoAplicacao {

	private final SaldoRepositorioAplicacao repositorio;

	public SaldoServicoAplicacao(SaldoRepositorioAplicacao repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public SaldoResumo obterResumo(UsuarioId usuario) {
		return repositorio.obterResumo(usuario);
	}
}
