package cesar.rv.ingressify.aplicacao.compra;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.CompraPendente;
import cesar.rv.ingressify.dominio.marketplace.compra.CompraPendenteRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

@Service
public class ComprarIngressoServicoAplicacao {

	private final TipoIngressoServico tipoIngressoServico;
	private final EventoServico eventoServico;
	private final CompraPendenteRepositorio compraPendenteRepositorio;
	private final PagamentoServico pagamentoServico;

	public ComprarIngressoServicoAplicacao(TipoIngressoServico tipoIngressoServico, EventoServico eventoServico,
			CompraPendenteRepositorio compraPendenteRepositorio, PagamentoServico pagamentoServico) {
		Validate.notNull(tipoIngressoServico, "tipoIngressoServico");
		Validate.notNull(eventoServico, "eventoServico");
		Validate.notNull(compraPendenteRepositorio, "compraPendenteRepositorio");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		this.tipoIngressoServico = tipoIngressoServico;
		this.eventoServico = eventoServico;
		this.compraPendenteRepositorio = compraPendenteRepositorio;
		this.pagamentoServico = pagamentoServico;
	}

	@Transactional
	public UUID iniciarCompraDireta(TipoIngressoId tipo, int qtd, UsuarioId comprador) {
		Validate.notNull(tipo, "tipo");
		Validate.isTrue(qtd > 0, "quantidade deve ser > 0");
		Validate.notNull(comprador, "comprador");
		TipoIngresso tipoIng = tipoIngressoServico.obter(tipo);
		Evento evento = eventoServico.obter(tipoIng.getEventoId());
		Validate.isTrue(evento.ativo(), "evento deve estar ativo e futuro");
		tipoIng.reservar(qtd);
		tipoIngressoServico.salvar(tipoIng);
		Dinheiro valorTotal = tipoIng.getPreco().multiplicar(qtd);
		UUID correlacaoId = UUID.randomUUID();
		CompraPendente compra = new CompraPendente(correlacaoId, tipo, tipoIng.getEventoId(), qtd, comprador, valorTotal,
				LocalDateTime.now());
		compraPendenteRepositorio.salvar(compra);
		pagamentoServico.criarPendente(valorTotal, comprador, null, TipoOperacao.COMPRA_DIRETA, correlacaoId);
		return correlacaoId;
	}
}
