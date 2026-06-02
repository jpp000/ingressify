package cesar.rv.ingressify.infraestrutura;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import cesar.rv.ingressify.aplicacao.financeiro.extrato.ExtratoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.anuncioRevenda.AnuncioRevendaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.avaliacao.AvaliacaoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.catalogo.CatalogoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.checkin.CheckinServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.compra.CompraServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.denuncia.DenunciaServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.feed.FeedServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.ObservadorCancelamento;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.PublicadorEvento;
import cesar.rv.ingressify.aplicacao.marketplace.reembolso.ReembolsoServicoAplicacao;
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
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoServico;
import cesar.rv.ingressify.dominio.marketplace.checkin.CheckinServico;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckinRepositorio;
import cesar.rv.ingressify.dominio.marketplace.compra.PedidoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaServico;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.feed.ComentarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.feed.FeedServico;
import cesar.rv.ingressify.dominio.marketplace.feed.PostagemRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;
import cesar.rv.ingressify.dominio.padroes.proxy.UsuarioServicoProxy;
import cesar.rv.ingressify.dominio.padroes.proxy.VerificadorBloqueioRevenda;
import cesar.rv.ingressify.infraestrutura.memoria.AnuncioRevendaRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.AvaliacaoRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.ComentarioRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.DenunciaRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.PagamentoRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.PedidoRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.PostagemRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.RegistroCheckinRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.SaldoRepositorioMemoria;
import cesar.rv.ingressify.infraestrutura.memoria.SolicitacaoReembolsoRepositorioMemoria;

@Configuration
public class IngressifyConfiguration {

	// ── Repositórios em memória ──────────────────────────────────────────────

	@Bean
	@Primary
	public SaldoRepositorio saldoRepositorio() {
		return new SaldoRepositorioMemoria();
	}

	@Bean
	public PagamentoRepositorio pagamentoRepositorio() {
		return new PagamentoRepositorioMemoria();
	}

	@Bean
	public AnuncioRevendaRepositorio anuncioRevendaRepositorio() {
		return new AnuncioRevendaRepositorioMemoria();
	}

	@Bean
	public AvaliacaoRepositorio avaliacaoRepositorio() {
		return new AvaliacaoRepositorioMemoria();
	}

	@Bean
	public DenunciaRepositorio denunciaRepositorio() {
		return new DenunciaRepositorioMemoria();
	}

	@Bean
	public PostagemRepositorio postagemRepositorio() {
		return new PostagemRepositorioMemoria();
	}

	@Bean
	public ComentarioRepositorio comentarioRepositorio() {
		return new ComentarioRepositorioMemoria();
	}

	@Bean
	public RegistroCheckinRepositorio registroCheckinRepositorio() {
		return new RegistroCheckinRepositorioMemoria();
	}

	@Bean
	public SolicitacaoReembolsoRepositorio solicitacaoReembolsoRepositorio() {
		return new SolicitacaoReembolsoRepositorioMemoria();
	}

	@Bean
	public PedidoRepositorio pedidoRepositorio() {
		return new PedidoRepositorioMemoria();
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
			TransacaoServico transacaoServico) {
		return new ReembolsoServicoAplicacao(solicitacaoReembolsoServico, solicitacaoReembolsoRepositorio,
				ingressoServico, tipoIngressoServico, tipoIngressoRepositorio, pedidoRepositorio,
				eventoRepositorio, saldoServico, transacaoServico);
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
	public CompraServicoAplicacao compraServicoAplicacao(
			TipoIngressoServico tipoIngressoServico,
			TipoIngressoRepositorio tipoIngressoRepositorio,
			IngressoServico ingressoServico,
			PagamentoServico pagamentoServico,
			SaldoServico saldoServico,
			TransacaoServico transacaoServico,
			PedidoRepositorio pedidoRepositorio) {
		return new CompraServicoAplicacao(tipoIngressoServico, tipoIngressoRepositorio, ingressoServico,
				pagamentoServico, saldoServico, transacaoServico, pedidoRepositorio);
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
}
