package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.aplicacao.financeiro.saldo.SaldoRepositorioAplicacao;
import cesar.rv.ingressify.aplicacao.financeiro.saldo.SaldoResumo;
import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Repository
public class SaldoRepositorioImpl implements SaldoRepositorio, SaldoRepositorioAplicacao {

	private final SaldoJpaRepository jpaRepository;
	private final JpaMapeador mapeador;

	public SaldoRepositorioImpl(SaldoJpaRepository jpaRepository, JpaMapeador mapeador) {
		this.jpaRepository = jpaRepository;
		this.mapeador = mapeador;
	}

	@Override
	public void salvar(Saldo saldo) {
		SaldoJpa jpa = jpaRepository.findById(saldo.getUsuario().getId()).orElseGet(SaldoJpa::new);
		mapeador.preencherSaldoJpa(jpa, saldo);
		jpaRepository.save(jpa);
	}

	@Override
	public Saldo obter(UsuarioId usuario) {
		return jpaRepository.findById(usuario.getId()).map(mapeador::paraSaldo).orElseGet(() -> new Saldo(usuario, Dinheiro.ZERO));
	}

	@Override
	public SaldoResumo obterResumo(UsuarioId usuario) {
		Saldo saldo = obter(usuario);
		return new SaldoResumo() {
			@Override
			public int getUsuarioId() {
				return saldo.getUsuario().getId();
			}

			@Override
			public java.math.BigDecimal getValor() {
				return saldo.getValor().getValor();
			}
		};
	}
}
