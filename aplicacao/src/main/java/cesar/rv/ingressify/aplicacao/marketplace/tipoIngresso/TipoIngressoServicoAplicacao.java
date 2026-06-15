package cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.Lote;
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
			int quantidade, String descricao, List<String> beneficios, List<CriarLoteDto> lotesDto,
			boolean meiaEntradaHabilitada, int percentualMeia, int cotaMeia) {
		Evento evento = eventoRepositorio.obter(eventoId);
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		if (!evento.ativo()) {
			throw new IllegalStateException("evento não está ativo para novos tipos");
		}

		boolean usarLotes = lotesDto != null && !lotesDto.isEmpty();
		int quantidadeTotal = usarLotes
				? lotesDto.stream().mapToInt(CriarLoteDto::quantidade).sum()
				: quantidade;

		int somaExistentes = tipoIngressoRepositorio.pesquisarPorEvento(eventoId).stream()
				.mapToInt(TipoIngresso::getQuantidadeTotal)
				.sum();
		if (somaExistentes + quantidadeTotal > evento.getCapacidade()) {
			throw new IllegalStateException("soma das quantidades excede capacidade do evento");
		}

		Dinheiro dinheiroDisplay = usarLotes
				? new Dinheiro(lotesDto.get(0).preco())
				: new Dinheiro(preco);

		TipoIngresso tipo = new TipoIngresso(eventoId, nome, dinheiroDisplay,
				quantidadeTotal, quantidadeTotal, descricao);

		if (beneficios != null && !beneficios.isEmpty()) {
			tipo.definirBeneficios(beneficios);
		}

		if (usarLotes) {
			for (int i = 0; i < lotesDto.size(); i++) {
				CriarLoteDto l = lotesDto.get(i);
				tipo.adicionarLote(new Lote(i + 1, l.nome(), new Dinheiro(l.preco()),
						l.quantidade(), l.dataInicio(), l.dataFim()));
			}
		}

		if (meiaEntradaHabilitada) {
			tipo.configurarMeiaEntrada(true, percentualMeia, cotaMeia);
		}

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

	public cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso obter(TipoIngressoId id) {
		return tipoIngressoServico.obter(id);
	}

	public java.util.List<cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso> listarPorEvento(
			EventoId eventoId) {
		return tipoIngressoRepositorio.pesquisarPorEvento(eventoId);
	}
}
