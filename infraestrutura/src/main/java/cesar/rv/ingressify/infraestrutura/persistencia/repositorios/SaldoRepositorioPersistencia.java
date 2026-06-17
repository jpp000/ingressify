package cesar.rv.ingressify.infraestrutura.persistencia.repositorios;

import java.math.BigDecimal;

import org.springframework.stereotype.Repository;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.infraestrutura.persistencia.jpa.SaldoJpa;
import cesar.rv.ingressify.infraestrutura.persistencia.springdata.SaldoSpringDataRepository;

@Repository
public class SaldoRepositorioPersistencia implements SaldoRepositorio {

	private final SaldoSpringDataRepository jpa;

	public SaldoRepositorioPersistencia(SaldoSpringDataRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void salvar(Saldo saldo) {
		jpa.save(SaldoJpa.fromDomain(saldo));
	}

	@Override
	public Saldo obter(UsuarioId usuario) {
		return jpa.findByUsuarioId(usuario.getId())
				.map(SaldoJpa::toDomain)
				.orElseGet(() -> {
					Saldo novo = new Saldo(usuario, new Dinheiro(BigDecimal.ZERO));
					salvar(novo);
					return novo;
				});
	}
}
