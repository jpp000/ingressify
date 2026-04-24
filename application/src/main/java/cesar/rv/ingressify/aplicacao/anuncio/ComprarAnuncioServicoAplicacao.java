package cesar.rv.ingressify.aplicacao.anuncio;

import java.util.UUID;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;

@Service
public class ComprarAnuncioServicoAplicacao {

	private final AnuncioRevendaServico anuncioRevendaServico;
	private final PagamentoServico pagamentoServico;

	public ComprarAnuncioServicoAplicacao(AnuncioRevendaServico anuncioRevendaServico,
			PagamentoServico pagamentoServico) {
		Validate.notNull(anuncioRevendaServico, "anuncioRevendaServico");
		Validate.notNull(pagamentoServico, "pagamentoServico");
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.pagamentoServico = pagamentoServico;
	}

	@Transactional
	public UUID iniciarCompraRevenda(AnuncioRevendaId anuncioId, UsuarioId comprador) {
		Validate.notNull(anuncioId, "anuncioId");
		Validate.notNull(comprador, "comprador");
		UUID correlacao = UUID.randomUUID();
		anuncioRevendaServico.reservar(anuncioId, comprador, correlacao);
		AnuncioRevenda anuncio = anuncioRevendaServico.obter(anuncioId);
		pagamentoServico.criarPendente(anuncio.getPreco(), comprador, anuncio.getVendedor(), TipoOperacao.REVENDA,
				correlacao);
		return correlacao;
	}
}
