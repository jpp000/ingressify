package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.aplicacao.financeiro.transacao.TransacaoRepositorioAplicacao;
import cesar.rv.ingressify.aplicacao.financeiro.transacao.TransacaoResumo;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoId;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Repository
public class TransacaoRepositorioImpl implements TransacaoRepositorio, TransacaoRepositorioAplicacao {

	private final TransacaoJpaRepository jpaRepository;
	private final JpaMapeador mapeador;

	public TransacaoRepositorioImpl(TransacaoJpaRepository jpaRepository, JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(Transacao transacao) {
		TransacaoJpa jpa = transacao.getId() == null ? new TransacaoJpa()
				: jpaRepository.findById(transacao.getId().getId()).orElseGet(TransacaoJpa::new);
		mapeador.preencherTransacaoJpa(jpa, transacao);
		TransacaoJpa salvo = jpaRepository.save(jpa);
		if (transacao.getId() == null) {
			transacao.atribuirId(new TransacaoId(salvo.getId()));
		}
	}

	@Override
	public List<Transacao> pesquisarPorUsuario(UsuarioId usuario) {
		return jpaRepository.findByUsuarioIdOrderByDataDesc(usuario.getId()).stream().map(mapeador::paraTransacao).toList();
	}

	@Override
	public List<TransacaoResumo> pesquisarHistorico(UsuarioId usuario) {
		return jpaRepository.pesquisarHistorico(usuario.getId());
	}
}
