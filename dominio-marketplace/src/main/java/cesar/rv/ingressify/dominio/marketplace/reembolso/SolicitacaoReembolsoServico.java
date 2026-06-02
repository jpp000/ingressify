package cesar.rv.ingressify.dominio.marketplace.reembolso;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

public class SolicitacaoReembolsoServico {

	private final SolicitacaoReembolsoRepositorio repositorio;

	public SolicitacaoReembolsoServico(SolicitacaoReembolsoRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public void salvar(SolicitacaoReembolso s) {
		repositorio.salvar(s);
	}

	public SolicitacaoReembolso obter(SolicitacaoReembolsoId id) {
		return repositorio.obter(id);
	}

	public void aprovar(SolicitacaoReembolsoId id, LocalDateTime agora) {
		SolicitacaoReembolso s = repositorio.obter(id);
		s.aprovar(agora);
		repositorio.salvar(s);
	}

	public void recusar(SolicitacaoReembolsoId id, LocalDateTime agora) {
		SolicitacaoReembolso s = repositorio.obter(id);
		s.recusar(agora);
		repositorio.salvar(s);
	}
}
