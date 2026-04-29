package cesar.rv.ingressify.aplicacao.marketplace.catalogo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoServico;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;

public class CatalogoServicoAplicacao {

	private final EventoRepositorio eventoRepositorio;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;
	private final AnuncioRevendaRepositorio anuncioRevendaRepositorio;
	private final AvaliacaoServico avaliacaoServico;

	public CatalogoServicoAplicacao(EventoRepositorio eventoRepositorio,
			TipoIngressoRepositorio tipoIngressoRepositorio, AnuncioRevendaRepositorio anuncioRevendaRepositorio,
			AvaliacaoServico avaliacaoServico) {
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(anuncioRevendaRepositorio, "anuncioRevendaRepositorio");
		Validate.notNull(avaliacaoServico, "avaliacaoServico");
		this.eventoRepositorio = eventoRepositorio;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.anuncioRevendaRepositorio = anuncioRevendaRepositorio;
		this.avaliacaoServico = avaliacaoServico;
	}

	public List<EventoCatalogoResumo> listar(FiltroCatalogo filtro) {
		Validate.notNull(filtro, "filtro");
		List<Evento> candidatos = new ArrayList<>();
		for (Evento e : eventoRepositorio.listarAtivos()) {
			if (!e.iniciado()) {
				candidatos.add(e);
			}
		}
		List<EventoCatalogoResumo> resultado = new ArrayList<>();
		for (Evento evento : candidatos) {
			if (!passaFiltro(evento, filtro)) {
				continue;
			}
			List<TipoIngresso> tipos = tipoIngressoRepositorio.pesquisarPorEvento(evento.getId());
			Dinheiro precoMinimo = Dinheiro.ZERO;
			if (!tipos.isEmpty()) {
				precoMinimo = tipos.stream().map(TipoIngresso::getPreco).min(Comparator.comparing(Dinheiro::getValor))
						.orElse(Dinheiro.ZERO);
			}
			boolean temEstoquePrimario = tipos.stream().anyMatch(t -> t.getQuantidadeDisponivel() > 0);
			boolean temRevendaDisponivel = !temEstoquePrimario
					&& anuncioRevendaRepositorio.existeDisponivelOuReservadoParaEvento(evento.getId());
			double media = avaliacaoServico.mediaPorEvento(evento.getId());
			EventoId id = evento.getId();
			String nomeEv = evento.getNome();
			LocalDateTime dataHora = evento.getDataHora();
			String local = evento.getLocal();
			String imagem = evento.getImagemCapaUrl();
			boolean estoque = temEstoquePrimario;
			boolean revenda = temRevendaDisponivel;
			Dinheiro precoMin = precoMinimo;
			resultado.add(new EventoCatalogoResumo() {
				@Override
				public EventoId getId() {
					return id;
				}

				@Override
				public String getNome() {
					return nomeEv;
				}

				@Override
				public LocalDateTime getDataHora() {
					return dataHora;
				}

				@Override
				public String getLocal() {
					return local;
				}

				@Override
				public String getImagemCapaUrl() {
					return imagem;
				}

				@Override
				public Dinheiro getPrecoMinimo() {
					return precoMin;
				}

				@Override
				public double getMediaAvaliacao() {
					return media;
				}

				@Override
				public boolean isTemEstoquePrimario() {
					return estoque;
				}

				@Override
				public boolean isTemRevendaDisponivel() {
					return revenda;
				}
			});
		}
		resultado.sort(Comparator
				.<EventoCatalogoResumo>comparingInt(e -> e.getMediaAvaliacao() > 0 ? 0 : 1)
				.thenComparing(EventoCatalogoResumo::getDataHora));
		return resultado;
	}

	private boolean passaFiltro(Evento evento, FiltroCatalogo f) {
		if (f.getCidade() != null && !f.getCidade().isBlank()) {
			if (evento.getLocal() == null
					|| !evento.getLocal().toLowerCase(Locale.ROOT).contains(f.getCidade().toLowerCase(Locale.ROOT))) {
				return false;
			}
		}
		if (f.getNome() != null && !f.getNome().isBlank()) {
			if (!evento.getNome().toLowerCase(Locale.ROOT).contains(f.getNome().toLowerCase(Locale.ROOT))) {
				return false;
			}
		}
		if (f.getDataInicio() != null && evento.getDataHora().isBefore(f.getDataInicio())) {
			return false;
		}
		if (f.getDataFim() != null && evento.getDataHora().isAfter(f.getDataFim())) {
			return false;
		}
		if (f.getPrecoMin() != null || f.getPrecoMax() != null) {
			List<TipoIngresso> tipos = tipoIngressoRepositorio.pesquisarPorEvento(evento.getId());
			if (tipos.isEmpty()) {
				return false;
			}
			BigDecimal minPreco = tipos.stream().map(t -> t.getPreco().getValor()).min(BigDecimal::compareTo).get();
			if (f.getPrecoMin() != null && minPreco.compareTo(f.getPrecoMin()) < 0) {
				return false;
			}
			if (f.getPrecoMax() != null && minPreco.compareTo(f.getPrecoMax()) > 0) {
				return false;
			}
		}
		return true;
	}
}
