package cesar.rv.ingressify.infraestrutura;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cesar.rv.ingressify.aplicacao.financeiro.carteira.CarteiraServicoAplicacao;
import cesar.rv.ingressify.aplicacao.financeiro.extrato.ExtratoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.identidade.usuario.UsuarioServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.analytics.AnalyticsServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.anuncioRevenda.AnuncioRevendaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.avaliacao.AvaliacaoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.catalogo.CatalogoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.checkin.CheckinServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.compra.CompraServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.cupom.CupomServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaEventoServicoAplicacao;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoRepositorio;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.feed.FeedServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.grupoCompra.GrupoCompraServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.CompraAssentoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.FilaEsperaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.MapaAssentosServicoAplicacao;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.FilaEsperaRepositorio;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.estrategia.SorteioAleatorioEstrategia;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.ObservadorCancelamento;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.PublicadorEvento;
import cesar.rv.ingressify.aplicacao.marketplace.reembolso.ReembolsoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.sorteio.SorteioServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoServicoAplicacao;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoRepositorio;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoRepositorio;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoRepositorio;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioServico;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosRepositorio;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioServico;
import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaSorteio;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoServico;
import cesar.rv.ingressify.dominio.marketplace.checkin.CheckinServico;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckinRepositorio;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomRepositorio;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomServico;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaServico;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.feed.FeedServico;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemRepositorio;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraRepositorio;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraServico;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;
import cesar.rv.ingressify.dominio.padroes.proxy.UsuarioServicoProxy;
import cesar.rv.ingressify.dominio.padroes.proxy.VerificadorBloqueioRevenda;
import cesar.rv.ingressify.infraestrutura.memoria.PagamentoRepositorioMemoria;

@Configuration
public class IngressifyConfiguration {

	// ── Repositórios em memória (apenas Pagamento — sem gateway real) ────────

	@Bean
	public PagamentoRepositorio pagamentoRepositorio() {
		return new PagamentoRepositorioMemoria();
	}

	// ── Serviços de domínio ──────────────────────────────────────────────────

	@Bean
	public SaldoServico saldoServico(SaldoRepositorio saldoRepositorio) {
		return new SaldoServico(saldoRepositorio);
	}

	@Bean
	public TransacaoServico transacaoServico(TransacaoRepositorio transacaoRepositorio) {
		return new TransacaoServico(transacaoRepositorio);
	}

	@Bean
	public PagamentoServico pagamentoServico(PagamentoRepositorio pagamentoRepositorio) {
		return new PagamentoServico(pagamentoRepositorio);
	}

	@Bean
	public EventoServico eventoServico(EventoRepositorio eventoRepositorio) {
		return new EventoServico(eventoRepositorio);
	}

	@Bean
	public IngressoServico ingressoServico(IngressoRepositorio ingressoRepositorio) {
		return new IngressoServico(ingressoRepositorio);
	}

	@Bean
	public TipoIngressoServico tipoIngressoServico(TipoIngressoRepositorio tipoIngressoRepositorio,
			EventoRepositorio eventoRepositorio) {
		return new TipoIngressoServico(tipoIngressoRepositorio, eventoRepositorio);
	}

	@Bean
	public AnuncioRevendaServico anuncioRevendaServico(AnuncioRevendaRepositorio anuncioRevendaRepositorio,
			IngressoRepositorio ingressoRepositorio, TipoIngressoRepositorio tipoIngressoRepositorio) {
		return new AnuncioRevendaServico(anuncioRevendaRepositorio, ingressoRepositorio, tipoIngressoRepositorio);
	}

	@Bean
	public AvaliacaoServico avaliacaoServico(AvaliacaoRepositorio avaliacaoRepositorio) {
		return new AvaliacaoServico(avaliacaoRepositorio);
	}

	@Bean
	public CheckinServico checkinServico(RegistroCheckinRepositorio registroCheckinRepositorio,
			IngressoRepositorio ingressoRepositorio) {
		return new CheckinServico(registroCheckinRepositorio, ingressoRepositorio);
	}

	@Bean
	public DenunciaServico denunciaServico(DenunciaRepositorio denunciaRepositorio) {
		return new DenunciaServico(denunciaRepositorio);
	}

	@Bean
	public FeedServico feedServico(PostagemRepositorio postagemRepositorio,
			ComentarioRepositorio comentarioRepositorio) {
		return new FeedServico(postagemRepositorio, comentarioRepositorio);
	}

	@Bean
	public SolicitacaoReembolsoServico solicitacaoReembolsoServico(
			SolicitacaoReembolsoRepositorio solicitacaoReembolsoRepositorio) {
		return new SolicitacaoReembolsoServico(solicitacaoReembolsoRepositorio);
	}

	@Bean
	public UsuarioServico usuarioServico(UsuarioRepositorio usuarioRepositorio) {
		return new UsuarioServico(usuarioRepositorio);
	}

	@Bean
	public UsuarioServicoAplicacao usuarioServicoAplicacao(
			UsuarioServico usuarioServico,
			IngressoRepositorio ingressoRepositorio,
			SaldoRepositorio saldoRepositorio) {
		return new UsuarioServicoAplicacao(usuarioServico, ingressoRepositorio, saldoRepositorio);
	}

	@Bean
	public cesar.rv.ingressify.dominio.padroes.proxy.UsuarioServico usuarioServicoProxy(
			UsuarioRepositorio usuarioRepositorio) {
		cesar.rv.ingressify.dominio.padroes.proxy.UsuarioServico real =
				new cesar.rv.ingressify.dominio.padroes.proxy.UsuarioServico() {
					@Override
					public boolean podeCriarAnuncioRevenda(UsuarioId usuarioId) {
						return true;
					}
					@Override
					public boolean podeComprarIngresso(UsuarioId usuarioId) {
						return true;
					}
				};
		VerificadorBloqueioRevenda verificador = id -> usuarioRepositorio.obter(id).isBloqueadoRevenda();
		return new UsuarioServicoProxy(real, verificador);
	}

	// ── Serviços de aplicação ────────────────────────────────────────────────

	@Bean
	public ReembolsoServicoAplicacao reembolsoServicoAplicacao(
			SolicitacaoReembolsoServico solicitacaoReembolsoServico,
			SolicitacaoReembolsoRepositorio solicitacaoReembolsoRepositorio,
			IngressoServico ingressoServico,
			TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			PedidoRepositorio pedidoRepositorio,
			EventoRepositorio eventoRepositorio,
			SaldoServico saldoServico,
			TransacaoServico transacaoServico,
			UsuarioServico usuarioServico) {
		return new ReembolsoServicoAplicacao(solicitacaoReembolsoServico, solicitacaoReembolsoRepositorio,
				ingressoServico, tipoIngressoServico, tipoIngressoRepositorio, pedidoRepositorio,
				eventoRepositorio, saldoServico, transacaoServico, usuarioServico);
	}

	@Bean
	public PublicadorEvento publicadorEvento(ReembolsoServicoAplicacao reembolsoServicoAplicacao) {
		PublicadorEvento pub = new PublicadorEvento();
		pub.registrar(new ObservadorCancelamento(reembolsoServicoAplicacao));
		return pub;
	}

	@Bean
	public EventoServicoAplicacao eventoServicoAplicacao(
			EventoServico eventoServico,
			EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio,
			IngressoServico ingressoServico,
			AnuncioRevendaRepositorio anuncioRevendaRepositorio,
			AnuncioRevendaServico anuncioRevendaServico,
			ReembolsoServicoAplicacao reembolsoServicoAplicacao,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			UsuarioRepositorio usuarioRepositorio,
			PublicadorEvento publicadorEvento) {
		return new EventoServicoAplicacao(eventoServico, eventoRepositorio, ingressoRepositorio,
				ingressoServico, anuncioRevendaRepositorio, anuncioRevendaServico,
				reembolsoServicoAplicacao, tipoIngressoRepositorio, usuarioRepositorio, publicadorEvento);
	}

	@Bean
	public IngressoServicoAplicacao ingressoServicoAplicacao(
			IngressoServico ingressoServico,
			IngressoRepositorio ingressoRepositorio,
			UsuarioRepositorio usuarioRepositorio) {
		return new IngressoServicoAplicacao(ingressoServico, ingressoRepositorio, usuarioRepositorio);
	}

	@Bean
	public AnuncioRevendaServicoAplicacao anuncioRevendaServicoAplicacao(
			AnuncioRevendaServico anuncioRevendaServico,
			AnuncioRevendaRepositorio anuncioRevendaRepositorio,
			IngressoServico ingressoServico,
			EventoRepositorio eventoRepositorio,
			UsuarioRepositorio usuarioRepositorio,
			PagamentoServico pagamentoServico,
			SaldoServico saldoServico,
			TransacaoServico transacaoServico,
			cesar.rv.ingressify.dominio.padroes.proxy.UsuarioServico usuarioServicoProxy) {
		return new AnuncioRevendaServicoAplicacao(anuncioRevendaServico, anuncioRevendaRepositorio,
				ingressoServico, eventoRepositorio, usuarioRepositorio, pagamentoServico,
				saldoServico, transacaoServico, usuarioServicoProxy);
	}

	@Bean
	public AvaliacaoServicoAplicacao avaliacaoServicoAplicacao(
			AvaliacaoServico avaliacaoServico,
			AvaliacaoRepositorio avaliacaoRepositorio,
			EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio) {
		return new AvaliacaoServicoAplicacao(avaliacaoServico, avaliacaoRepositorio,
				eventoRepositorio, ingressoRepositorio);
	}

	@Bean
	public CheckinServicoAplicacao checkinServicoAplicacao(
			CheckinServico checkinServico,
			IngressoRepositorio ingressoRepositorio,
			EventoRepositorio eventoRepositorio,
			UsuarioRepositorio usuarioRepositorio,
			RegistroCheckinRepositorio registroCheckinRepositorio) {
		return new CheckinServicoAplicacao(checkinServico, ingressoRepositorio,
				eventoRepositorio, usuarioRepositorio, registroCheckinRepositorio);
	}

	@Bean
	public DenunciaServicoAplicacao denunciaServicoAplicacao(
			DenunciaServico denunciaServico,
			AnuncioRevendaServico anuncioRevendaServico,
			AnuncioRevendaRepositorio anuncioRevendaRepositorio,
			UsuarioServico usuarioServico) {
		return new DenunciaServicoAplicacao(denunciaServico, anuncioRevendaServico,
				anuncioRevendaRepositorio, usuarioServico);
	}

	@Bean
	public DenunciaEventoServicoAplicacao denunciaEventoServicoAplicacao(
			DenunciaEventoRepositorio denunciaEventoRepositorio,
			EventoRepositorio eventoRepositorio,
			UsuarioServico usuarioServico) {
		return new DenunciaEventoServicoAplicacao(denunciaEventoRepositorio, eventoRepositorio, usuarioServico);
	}

	@Bean
	public FeedServicoAplicacao feedServicoAplicacao(
			FeedServico feedServico,
			EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio,
			UsuarioRepositorio usuarioRepositorio,
			PostagemRepositorio postagemRepositorio) {
		return new FeedServicoAplicacao(feedServico, eventoRepositorio, ingressoRepositorio,
				usuarioRepositorio, postagemRepositorio);
	}

	@Bean
	public CupomServico cupomServico(CupomRepositorio cupomRepositorio) {
		return new CupomServico(cupomRepositorio);
	}

	@Bean
	public CupomServicoAplicacao cupomServicoAplicacao(CupomServico cupomServico,
			EventoRepositorio eventoRepositorio) {
		return new CupomServicoAplicacao(cupomServico, eventoRepositorio);
	}

	@Bean
	public CarteiraServicoAplicacao carteiraServicoAplicacao(SaldoServico saldoServico,
			TransacaoServico transacaoServico) {
		return new CarteiraServicoAplicacao(saldoServico, transacaoServico);
	}

	@Bean
	public CompraServicoAplicacao compraServicoAplicacao(
			TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			IngressoServico ingressoServico,
			PagamentoServico pagamentoServico,
			SaldoServico saldoServico,
			TransacaoServico transacaoServico,
			PedidoRepositorio pedidoRepositorio,
			CupomServico cupomServico) {
		return new CompraServicoAplicacao(tipoIngressoServico, tipoIngressoRepositorio, ingressoServico,
				pagamentoServico, saldoServico, transacaoServico, pedidoRepositorio, cupomServico);
	}

	@Bean
	public ExtratoServicoAplicacao extratoServicoAplicacao(
			TransacaoServico transacaoServico,
			SaldoServico saldoServico) {
		return new ExtratoServicoAplicacao(transacaoServico, saldoServico);
	}

	@Bean
	public TipoIngressoServicoAplicacao tipoIngressoServicoAplicacao(
			TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio) {
		return new TipoIngressoServicoAplicacao(tipoIngressoServico, tipoIngressoRepositorio,
				eventoRepositorio, ingressoRepositorio);
	}

	@Bean
	public CatalogoServicoAplicacao catalogoServicoAplicacao(
			EventoRepositorio eventoRepositorio,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			AnuncioRevendaRepositorio anuncioRevendaRepositorio,
			AvaliacaoServico avaliacaoServico) {
		return new CatalogoServicoAplicacao(eventoRepositorio, tipoIngressoRepositorio,
				anuncioRevendaRepositorio, avaliacaoServico);
	}

	@Bean
	public AnalyticsServicoAplicacao analyticsServicoAplicacao(
			EventoRepositorio eventoRepositorio,
			IngressoRepositorio ingressoRepositorio,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			AvaliacaoRepositorio avaliacaoRepositorio) {
		return new AnalyticsServicoAplicacao(eventoRepositorio, ingressoRepositorio,
				tipoIngressoRepositorio, avaliacaoRepositorio);
	}

	// ── Sorteio de Ingressos ─────────────────────────────────────────────────

	@Bean
	public EstrategiaSorteio estrategiaSorteio() {
		return new SorteioAleatorioEstrategia();
	}

	@Bean
	public SorteioServico sorteioServico(SorteioRepositorio sorteioRepositorio,
			InscricaoSorteioRepositorio inscricaoSorteioRepositorio) {
		return new SorteioServico(sorteioRepositorio, inscricaoSorteioRepositorio);
	}

	@Bean
	public SorteioServicoAplicacao sorteioServicoAplicacao(SorteioServico sorteioServico,
			InscricaoSorteioRepositorio inscricaoSorteioRepositorio,
			EstrategiaSorteio estrategiaSorteio,
			IngressoServico ingressoServico,
			TransacaoServico transacaoServico,
			UsuarioServico usuarioServico) {
		return new SorteioServicoAplicacao(sorteioServico, inscricaoSorteioRepositorio, estrategiaSorteio,
				ingressoServico, transacaoServico, usuarioServico);
	}

	// ── Mapa de Assentos ─────────────────────────────────────────────────────

	@Bean
	public MapaAssentosServico mapaAssentosServico(MapaAssentosRepositorio mapaAssentosRepositorio) {
		return new MapaAssentosServico(mapaAssentosRepositorio);
	}

	@Bean
	public MapaAssentosServicoAplicacao mapaAssentosServicoAplicacao(
			MapaAssentosServico mapaAssentosServico,
			EventoServico eventoServico) {
		return new MapaAssentosServicoAplicacao(mapaAssentosServico, eventoServico);
	}

	@Bean
	public CompraAssentoServicoAplicacao compraAssentoServicoAplicacao(
			MapaAssentosServico mapaAssentosServico,
			SaldoServico saldoServico,
			TransacaoServico transacaoServico) {
		return new CompraAssentoServicoAplicacao(mapaAssentosServico, saldoServico, transacaoServico);
	}

	@Bean
	public FilaEsperaServicoAplicacao filaEsperaServicoAplicacao(
			FilaEsperaRepositorio filaEsperaRepositorio,
			MapaAssentosServico mapaAssentosServico) {
		return new FilaEsperaServicoAplicacao(filaEsperaRepositorio, mapaAssentosServico);
	}

	// ── Compra em Grupo ──────────────────────────────────────────────────────

	@Bean
	public GrupoCompraServico grupoCompraServico(GrupoCompraRepositorio grupoCompraRepositorio,
			ParticipanteGrupoRepositorio participanteGrupoRepositorio, TipoIngressoServico tipoIngressoServico,
			EventoRepositorio eventoRepositorio) {
		return new GrupoCompraServico(grupoCompraRepositorio, participanteGrupoRepositorio, tipoIngressoServico,
				eventoRepositorio);
	}

	@Bean
	public GrupoCompraServicoAplicacao grupoCompraServicoAplicacao(GrupoCompraServico grupoCompraServico,
			GrupoCompraRepositorio grupoCompraRepositorio, ParticipanteGrupoRepositorio participanteGrupoRepositorio,
			IngressoServico ingressoServico, PagamentoServico pagamentoServico, SaldoServico saldoServico,
			TransacaoServico transacaoServico) {
		return new GrupoCompraServicoAplicacao(grupoCompraServico, grupoCompraRepositorio,
				participanteGrupoRepositorio, ingressoServico, pagamentoServico, saldoServico, transacaoServico);
	}
}
