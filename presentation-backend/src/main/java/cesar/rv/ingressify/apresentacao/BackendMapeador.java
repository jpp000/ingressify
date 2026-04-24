package cesar.rv.ingressify.apresentacao;

import java.math.BigDecimal;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;

@Component
public class BackendMapeador {

	private final ModelMapper modelMapper = new ModelMapper();

	public EventoId paraEventoId(Integer id) {
		return id == null ? null : new EventoId(id);
	}

	public TipoIngressoId paraTipoIngressoId(Integer id) {
		return id == null ? null : new TipoIngressoId(id);
	}

	public UsuarioId paraUsuarioId(Integer id) {
		return id == null ? null : new UsuarioId(id);
	}

	public IngressoId paraIngressoId(String uuid) {
		return uuid == null ? null : new IngressoId(java.util.UUID.fromString(uuid));
	}

	public AnuncioRevendaId paraAnuncioRevendaId(Integer id) {
		return id == null ? null : new AnuncioRevendaId(id);
	}

	public PagamentoId paraPagamentoId(Integer id) {
		return id == null ? null : new PagamentoId(id);
	}

	public Dinheiro paraDinheiro(BigDecimal valor) {
		return valor == null ? null : new Dinheiro(valor);
	}

	public ModelMapper getModelMapper() {
		return modelMapper;
	}
}
