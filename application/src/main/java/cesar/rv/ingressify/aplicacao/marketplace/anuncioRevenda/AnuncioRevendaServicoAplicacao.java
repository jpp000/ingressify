package cesar.rv.ingressify.aplicacao.marketplace.anuncioRevenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;

public class AnuncioRevendaServicoAplicacao {

	private final AnuncioRevendaServico anuncioRevendaServico;
	private final IngressoServico ingressoServico;
	private final EventoRepositorio eventoRepositorio;
	private final UsuarioRepositorio usuarioRepositorio;
	private final PagamentoServico pagamentoServico;
	private final SaldoServico saldoServico;
	private final TransacaoServico transacaoServico;

	public AnuncioRevendaServicoAplicacao(AnuncioRevendaServico anuncioRevendaServico,
			IngressoServico ingressoServico, EventoRepositorio eventoRepositorio, UsuarioRepositorio usuarioRepositorio,
			PagamentoServico pagamentoServico, SaldoServico saldoServico, TransacaoServico transacaoServico) {
		Validate.notNull(anuncioRevendaServico, "anuncioRevendaServico");
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(eventoRepositorio, "eventoRepositorio");
		Validate.notNull(usuarioRepositorio, "usuarioRepositorio");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		Validate.notNull(saldoServico, "saldoServico");
		Validate.notNull(transacaoServico, "transacaoServico");
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.ingressoServico = ingressoServico;
		this.eventoRepositorio = eventoRepositorio;
		this.usuarioRepositorio = usuarioRepositorio;
		this.pagamentoServico = pagamentoServico;
		this.saldoServico = saldoServico;
		this.transacaoServico = transacaoServico;
	}

	public AnuncioRevendaId anunciar(UsuarioId vendedorId, List<IngressoId> ingressoIds, BigDecimal precoRevenda) {
		if (usuarioRepositorio.obter(vendedorId).isBloqueadoRevenda()) {
			throw new IllegalStateException("usuário bloqueado para revenda");
		}
		IngressoId primeiro = ingressoIds.get(0);
		Evento evento = eventoRepositorio.obter(ingressoServico.obter(primeiro).getEventoId());
		if (evento.getStatus() == StatusEvento.CANCELADO) {
			throw new IllegalStateException("evento cancelado");
		}
		Dinheiro preco = new Dinheiro(precoRevenda);
		AnuncioRevenda criado = anuncioRevendaServico.criar(ingressoIds, preco, vendedorId);
		return criado.getId();
	}

	public void reservar(AnuncioRevendaId anuncioId, UsuarioId compradorId) {
		AnuncioRevenda a = anuncioRevendaServico.obter(anuncioId);
		UUID correlacao = UUID.randomUUID();
		pagamentoServico.criarPendente(a.getPreco(), compradorId, a.getVendedor(), TipoOperacao.REVENDA, correlacao);
		anuncioRevendaServico.reservar(anuncioId, compradorId, correlacao);
	}

	public void confirmarCompra(AnuncioRevendaId anuncioId) {
		AnuncioRevenda a = anuncioRevendaServico.obter(anuncioId);
		UsuarioId comprador = a.getCompradorReservado();
		if (comprador == null) {
			throw new IllegalStateException("anúncio sem comprador reservado");
		}
		Pagamento pagamento = pagamentoServico.obterPorCorrelacao(a.getCorrelacaoPagamento())
				.orElseThrow(() -> new IllegalStateException("pagamento não encontrado"));
		pagamentoServico.confirmar(pagamento.getId());
		Pagamento.Divisao divisao = pagamento.dividir(new BigDecimal("0.10"));
		saldoServico.creditar(a.getVendedor(), divisao.vendedor());
		anuncioRevendaServico.concluir(anuncioId);
		LocalDateTime agora = LocalDateTime.now();
		for (IngressoId iid : a.getIngressoIds()) {
			ingressoServico.concluirRevenda(iid, comprador);
		}
		transacaoServico.registrar(
				new Transacao(a.getVendedor(), TipoTransacao.VENDA, divisao.vendedor(), agora, pagamento.getCorrelacaoId()));
		transacaoServico.registrar(new Transacao(comprador, TipoTransacao.COMPRA, pagamento.getValor(), agora,
				pagamento.getCorrelacaoId()));
	}

	public void cancelar(AnuncioRevendaId anuncioId, UsuarioId vendedorId) {
		AnuncioRevenda a = anuncioRevendaServico.obter(anuncioId);
		if (!a.getVendedor().equals(vendedorId)) {
			throw new IllegalStateException("anúncio não pertence ao vendedor");
		}
		anuncioRevendaServico.cancelar(anuncioId);
	}

	public void alterarPreco(AnuncioRevendaId anuncioId, UsuarioId vendedorId, BigDecimal novoPreco) {
		AnuncioRevenda a = anuncioRevendaServico.obter(anuncioId);
		if (!a.getVendedor().equals(vendedorId)) {
			throw new IllegalStateException("anúncio não pertence ao vendedor");
		}
		anuncioRevendaServico.alterarPreco(anuncioId, new Dinheiro(novoPreco));
	}
}
