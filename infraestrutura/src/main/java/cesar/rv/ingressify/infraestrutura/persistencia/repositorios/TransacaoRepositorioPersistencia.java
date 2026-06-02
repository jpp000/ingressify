package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.util.List;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoId;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.TransacaoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.TransacaoSpringDataRepository;

@Repository
public class TransacaoRepositorioPersistencia implements TransacaoRepositorio {

	private final TransacaoSpringDataRepository jpa;

	public TransacaoRepositorioPersistencia(TransacaoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Transacao transacao) {
		TransacaoJpa saved = jpa.save(TransacaoJpa.fromDomain(transacao));
		if (transacao.getId() == null) {
			transacao.atribuirId(new TransacaoId(saved.getId()));
		}
	}

	@Override
	public List<Transacao> pesquisarPorUsuario(UsuarioId usuario) {
		return jpa.findByUsuarioId(usuario.getId()).stream()
				.map(TransacaoJpa::toDomain).toList();
	}

	@Override
	public List<Transacao> pesquisarPorUsuarioOrdenadoDesc(UsuarioId usuario) {
		return jpa.findByUsuarioIdOrderByDataDesc(usuario.getId()).stream()
				.map(TransacaoJpa::toDomain).toList();
	}
}
