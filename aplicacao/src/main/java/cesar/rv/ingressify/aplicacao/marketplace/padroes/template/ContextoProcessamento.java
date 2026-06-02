package cesar.rv.ingressify.aplicacao.marketplace.padroes.template;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public class ContextoProcessamento {

	private final UsuarioId compradorId;
	private final TipoIngressoId tipoIngressoId;
	private final int quantidade;
	private final Dinheiro valorTotal;
	private final UUID correlacao;

	private Pagamento pagamento;
	private final List<IngressoId> ingressosCriados = new ArrayList<>();

	public ContextoProcessamento(UsuarioId compradorId, TipoIngressoId tipoIngressoId,
			int quantidade, Dinheiro valorTotal, UUID correlacao) {
		this.compradorId = compradorId;
		this.tipoIngressoId = tipoIngressoId;
		this.quantidade = quantidade;
		this.valorTotal = valorTotal;
		this.correlacao = correlacao;
	}

	public UsuarioId getCompradorId() { return compradorId; }
	public TipoIngressoId getTipoIngressoId() { return tipoIngressoId; }
	public int getQuantidade() { return quantidade; }
	public Dinheiro getValorTotal() { return valorTotal; }
	public UUID getCorrelacao() { return correlacao; }

	public Pagamento getPagamento() { return pagamento; }
	public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }

	public List<IngressoId> getIngressosCriados() { return ingressosCriados; }
	public void adicionarIngresso(IngressoId id) { ingressosCriados.add(id); }
}
