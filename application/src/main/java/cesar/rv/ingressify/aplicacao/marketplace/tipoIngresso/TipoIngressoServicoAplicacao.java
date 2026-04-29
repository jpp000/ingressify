package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import java.math.BigDecimal;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

public class TipoIngressoServicoAplicacao {

	private final TipoIngressoServico tipoIngressoServico;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;
	private final EventoRepositorio eventoRepositorio;
	private final IngressoRepositorio ingressoRepositorio;

	public TipoIngressoServicoAplicacao(TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio, EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio) {
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		this.tipoIngressoServico = tipoIngressoServico;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.eventoRepositorio = eventoRepositorio;
		this.ingressoRepositorio = ingressoRepositorio;
	}

	public TipoIngressoId criarTipoIngresso(EventoId eventoId, UsuarioId organizadorId, String nome, BigDecimal preco,
			int quantidade, String descricao) {
		Evento evento = eventoRepositorio.obter(eventoId);
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		if (!evento.ativo()) {
			throw new IllegalStateException("evento não está ativo para novos tipos");
		}
		int somaExistentes = tipoIngressoRepositorio.pesquisarPorEvento(eventoId).stream()
				.mapToInt(TipoIngresso::getQuantidadeTotal)
				.sum();
		if (somaExistentes + quantidade > evento.getCapacidade()) {
			throw new IllegalStateException("soma das quantidades excede capacidade do evento");
		}
		Dinheiro dinheiro = new Dinheiro(preco);
		TipoIngresso tipo = new TipoIngresso(eventoId, nome, dinheiro, quantidade, quantidade, descricao);
		tipoIngressoServico.salvar(tipo);
		return tipo.getId();
	}

	public void editarTipoIngresso(TipoIngressoId tipoId, UsuarioId organizadorId, String nome, BigDecimal preco,
			int novaQuantidade, String descricao) {
		TipoIngresso tipo = tipoIngressoServico.obter(tipoId);
		Evento evento = eventoRepositorio.obter(tipo.getEventoId());
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		if (!evento.ativo()) {
			throw new IllegalStateException("evento não está ativo para edição");
		}
		tipo.atualizar(nome, new Dinheiro(preco), novaQuantidade, descricao);
		tipoIngressoServico.salvar(tipo);
	}

	public void removerTipoIngresso(TipoIngressoId tipoId, UsuarioId organizadorId) {
		TipoIngresso tipo = tipoIngressoServico.obter(tipoId);
		Evento evento = eventoRepositorio.obter(tipo.getEventoId());
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		if (ingressoRepositorio.contarVendidosPorTipo(tipoId) > 0) {
			throw new IllegalStateException("existem ingressos vendidos para este tipo");
		}
		tipoIngressoServico.remover(tipoId);
	}
}
