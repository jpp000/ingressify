package cesar.rv.ingressify.aplicacao.marketplace.evento;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.aplicacao.marketplace.reembolso.ReembolsoServicoAplicacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;

public class EventoServicoAplicacao {

	private final EventoServico eventoServico;
	private final EventoRepositorio eventoRepositorio;
	private final IngressoRepositorio ingressoRepositorio;
	private final IngressoServico ingressoServico;
	private final AnuncioRevendaRepositorio anuncioRevendaRepositorio;
	private final AnuncioRevendaServico anuncioRevendaServico;
	private final ReembolsoServicoAplicacao reembolsoServicoAplicacao;
	private final TipoIngressoRepositorio tipoIngressoRepositorio;
	private final UsuarioRepositorio usuarioRepositorio;

	public EventoServicoAplicacao(EventoServico eventoServico, EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio, IngressoServico ingressoServico,
			AnuncioRevendaRepositorio anuncioRevendaRepositorio, AnuncioRevendaServico anuncioRevendaServico,
			ReembolsoServicoAplicacao reembolsoServicoAplicacao, TipoIngressoRepositorio tipoIngressoRepositorio,
			UsuarioRepositorio usuarioRepositorio) {
		Validate.notNull(eventoServico, "eventoServico");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(anuncioRevendaRepositorio, "anuncioRevendaRepositorio");
		Validate.notNull(anuncioRevendaServico, "anuncioRevendaServico");
		Validate.notNull(reembolsoServicoAplicacao, "reembolsoServicoAplicacao");
		Validate.notNull(tipoIngressoRepositorio, "tipoIngressoRepositorio");
		Validate.notNull(usuarioRepositorio, "usuarioRepositorio");
		this.eventoServico = eventoServico;
		this.eventoRepositorio = eventoRepositorio;
		this.ingressoRepositorio = ingressoRepositorio;
		this.ingressoServico = ingressoServico;
		this.anuncioRevendaRepositorio = anuncioRevendaRepositorio;
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.reembolsoServicoAplicacao = reembolsoServicoAplicacao;
		this.tipoIngressoRepositorio = tipoIngressoRepositorio;
		this.usuarioRepositorio = usuarioRepositorio;
	}

	public EventoId criarEvento(UsuarioId organizadorId, String nome, LocalDateTime dataHora, String local,
			String descricao, int capacidade, String imagemCapaUrl, int prazoReembolsoDias,
			LocalDateTime aberturaPortoes) {
		var organizador = usuarioRepositorio.obter(organizadorId);
		if (!organizador.temPapel(Papel.ORGANIZADOR)) {
			throw new IllegalStateException("usuário deve ser organizador");
		}
		Evento evento = new Evento(organizadorId, nome, dataHora, local, descricao, capacidade, imagemCapaUrl,
				prazoReembolsoDias, aberturaPortoes);
		eventoServico.salvar(evento);
		return evento.getId();
	}

	public void editarEvento(EventoId eventoId, UsuarioId organizadorId, String nome, LocalDateTime dataHora,
			String local, String descricao, int capacidade, String imagemCapaUrl, int prazoReembolsoDias,
			LocalDateTime aberturaPortoes) {
		Evento evento = eventoServico.obter(eventoId);
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		eventoServico.atualizar(eventoId, nome, dataHora, local, descricao, capacidade, imagemCapaUrl,
				prazoReembolsoDias, aberturaPortoes);
	}

	public void cancelarEvento(EventoId eventoId, UsuarioId organizadorId) {
		Evento evento = eventoServico.obter(eventoId);
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		List<Ingresso> afetados = ingressoRepositorio.pesquisarPorEvento(eventoId).stream()
				.filter(i -> i.getStatus() == StatusIngresso.ATIVO || i.getStatus() == StatusIngresso.EM_REVENDA)
				.toList();
		eventoServico.cancelar(eventoId);
		for (AnuncioRevenda a : anuncioRevendaRepositorio.pesquisarPorEvento(eventoId)) {
			if (a.getStatus() == StatusAnuncio.DISPONIVEL || a.getStatus() == StatusAnuncio.RESERVADO) {
				anuncioRevendaServico.cancelar(a.getId());
			}
		}
		for (Ingresso ingresso : afetados) {
			Ingresso atual = ingressoRepositorio.obter(ingresso.getId());
			if (atual.getStatus() == StatusIngresso.ATIVO || atual.getStatus() == StatusIngresso.EM_REVENDA) {
				ingressoServico.cancelar(atual.getId());
				TipoIngresso tipo = tipoIngressoRepositorio.obter(atual.getTipoIngressoId());
				reembolsoServicoAplicacao.aprovarPorCancelamento(atual.getId(), atual.getProprietario(),
						tipo.getPreco());
			}
		}
	}

	public void removerEvento(EventoId eventoId, UsuarioId organizadorId) {
		Evento evento = eventoServico.obter(eventoId);
		if (!evento.getOrganizadorId().equals(organizadorId)) {
			throw new IllegalStateException("evento não pertence ao organizador");
		}
		for (TipoIngresso t : tipoIngressoRepositorio.pesquisarPorEvento(eventoId)) {
			if (ingressoRepositorio.contarVendidosPorTipo(t.getId()) > 0) {
				throw new IllegalStateException("existem ingressos vendidos para o tipo: " + t.getId());
			}
		}
		eventoServico.remover(eventoId);
	}
}
