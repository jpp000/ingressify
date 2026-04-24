package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.stereotype.Component;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.StatusEvento;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.StatusPagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.TipoOperacao;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoId;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;

@Component
public class JpaMapeador {

	private final ModelMapper modelMapper;

	public JpaMapeador() {
		this.modelMapper = new ModelMapper();
		this.modelMapper.getConfiguration().setFieldMatchingEnabled(true).setFieldAccessLevel(AccessLevel.PRIVATE);
		registrar();
	}

	public ModelMapper getModelMapper() {
		return modelMapper;
	}

	private void registrar() {
		modelMapper.addConverter(new AbstractConverter<Integer, EventoId>() {
			@Override
			protected EventoId convert(Integer source) {
				return source == null ? null : new EventoId(source);
			}
		});
		modelMapper.addConverter(new AbstractConverter<Integer, TipoIngressoId>() {
			@Override
			protected TipoIngressoId convert(Integer source) {
				return source == null ? null : new TipoIngressoId(source);
			}
		});
		modelMapper.addConverter(new AbstractConverter<Integer, AnuncioRevendaId>() {
			@Override
			protected AnuncioRevendaId convert(Integer source) {
				return source == null ? null : new AnuncioRevendaId(source);
			}
		});
		modelMapper.addConverter(new AbstractConverter<Integer, PagamentoId>() {
			@Override
			protected PagamentoId convert(Integer source) {
				return source == null ? null : new PagamentoId(source);
			}
		});
		modelMapper.addConverter(new AbstractConverter<Integer, TransacaoId>() {
			@Override
			protected TransacaoId convert(Integer source) {
				return source == null ? null : new TransacaoId(source);
			}
		});
		modelMapper.addConverter(new AbstractConverter<Integer, UsuarioId>() {
			@Override
			protected UsuarioId convert(Integer source) {
				return source == null ? null : new UsuarioId(source);
			}
		});
	}

	public Dinheiro paraDinheiro(DinheiroJpa j) {
		if (j == null || j.getValor() == null) {
			return Dinheiro.ZERO;
		}
		return new Dinheiro(j.getValor());
	}

	public void aplicarDinheiro(DinheiroJpa alvo, Dinheiro valor) {
		alvo.setValor(valor.getValor());
	}

	public Evento paraEvento(EventoJpa j) {
		return new Evento(new EventoId(j.getId()), j.getNome(), j.getDataHora(), j.getLocal(), j.getStatus(), j.getCapacidade());
	}

	public void preencherEventoJpa(EventoJpa j, Evento e) {
		if (e.getId() != null) {
			j.setId(e.getId().getId());
		}
		j.setNome(e.getNome());
		j.setDataHora(e.getDataHora());
		j.setLocal(e.getLocal());
		j.setStatus(e.getStatus());
		j.setCapacidade(e.getCapacidade());
	}

	public TipoIngresso paraTipoIngresso(TipoIngressoJpa j) {
		if (j.getId() == null) {
			return new TipoIngresso(new EventoId(j.getEventoId()), j.getNome(), paraDinheiro(j.getPreco()), j.getQuantidadeDisponivel(),
					j.getQuantidadeTotal());
		}
		return new TipoIngresso(new TipoIngressoId(j.getId()), new EventoId(j.getEventoId()), j.getNome(), paraDinheiro(j.getPreco()),
				j.getQuantidadeDisponivel(), j.getQuantidadeTotal());
	}

	public void preencherTipoIngressoJpa(TipoIngressoJpa j, TipoIngresso t) {
		if (t.getId() != null) {
			j.setId(t.getId().getId());
		}
		j.setEventoId(t.getEventoId().getId());
		j.setNome(t.getNome());
		aplicarDinheiro(j.getPreco(), t.getPreco());
		j.setQuantidadeDisponivel(t.getQuantidadeDisponivel());
		j.setQuantidadeTotal(t.getQuantidadeTotal());
	}

	public Ingresso paraIngresso(IngressoJpa j) {
		return new Ingresso(new IngressoId(j.getId()), new TipoIngressoId(j.getTipoIngressoId()), new EventoId(j.getEventoId()),
				new UsuarioId(j.getProprietarioId()), j.getStatus());
	}

	public void preencherIngressoJpa(IngressoJpa j, Ingresso i) {
		j.setId(i.getId().getId());
		j.setTipoIngressoId(i.getTipoIngressoId().getId());
		j.setEventoId(i.getEventoId().getId());
		j.setProprietarioId(i.getProprietario().getId());
		j.setStatus(i.getStatus());
	}

	public AnuncioRevenda paraAnuncioRevenda(AnuncioRevendaJpa j) {
		UsuarioId comprador = j.getCompradorReservadoId() == null ? null : new UsuarioId(j.getCompradorReservadoId());
		return new AnuncioRevenda(new AnuncioRevendaId(j.getId()), new IngressoId(j.getIngressoId()), new UsuarioId(j.getVendedorId()),
				paraDinheiro(j.getPreco()), comprador, j.getStatus(), j.getCorrelacaoPagamento());
	}

	public void preencherAnuncioRevendaJpa(AnuncioRevendaJpa j, AnuncioRevenda a) {
		if (a.getId() != null) {
			j.setId(a.getId().getId());
		}
		j.setIngressoId(a.getIngressoId().getId());
		j.setVendedorId(a.getVendedor().getId());
		aplicarDinheiro(j.getPreco(), a.getPreco());
		j.setCompradorReservadoId(a.getCompradorReservado() == null ? null : a.getCompradorReservado().getId());
		j.setStatus(a.getStatus());
		j.setCorrelacaoPagamento(a.getCorrelacaoPagamento());
	}

	public Pagamento paraPagamento(PagamentoJpa j) {
		UsuarioId vendedor = j.getVendedorId() == null ? null : new UsuarioId(j.getVendedorId());
		return new Pagamento(new PagamentoId(j.getId()), paraDinheiro(j.getValor()), new UsuarioId(j.getCompradorId()), vendedor,
				j.getTipo(), j.getStatus(), j.getCorrelacaoId());
	}

	public void preencherPagamentoJpa(PagamentoJpa j, Pagamento p) {
		if (p.getId() != null) {
			j.setId(p.getId().getId());
		}
		aplicarDinheiro(j.getValor(), p.getValor());
		j.setCompradorId(p.getComprador().getId());
		j.setVendedorId(p.getVendedor() == null ? null : p.getVendedor().getId());
		j.setTipo(p.getTipo());
		j.setStatus(p.getStatus());
		j.setCorrelacaoId(p.getCorrelacaoId());
	}

	public Saldo paraSaldo(SaldoJpa j) {
		return new Saldo(new UsuarioId(j.getUsuarioId()), paraDinheiro(j.getValor()));
	}

	public void preencherSaldoJpa(SaldoJpa j, Saldo s) {
		j.setUsuarioId(s.getUsuario().getId());
		aplicarDinheiro(j.getValor(), s.getValor());
	}

	public Transacao paraTransacao(TransacaoJpa j) {
		return new Transacao(new TransacaoId(j.getId()), new UsuarioId(j.getUsuarioId()), j.getTipo(), paraDinheiro(j.getValor()),
				j.getData(), j.getReferenciaExternaId());
	}

	public void preencherTransacaoJpa(TransacaoJpa j, Transacao t) {
		if (t.getId() != null) {
			j.setId(t.getId().getId());
		}
		j.setUsuarioId(t.getUsuario().getId());
		j.setTipo(t.getTipo());
		aplicarDinheiro(j.getValor(), t.getValor());
		j.setData(t.getData());
		j.setReferenciaExternaId(t.getReferenciaExternaId());
	}
}
