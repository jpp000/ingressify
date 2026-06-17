package cesar.rv.ingressify.infraestrutura.memoria;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class SaldoRepositorioMemoria implements SaldoRepositorio {

	private final ConcurrentHashMap<Integer, BigDecimal> saldos = new ConcurrentHashMap<>();

	@Override
	public synchronized void salvar(Saldo saldo) {
		saldos.put(saldo.getUsuario().getId(), saldo.getValor().getValor());
	}

	@Override
	public Saldo obter(UsuarioId usuario) {
		BigDecimal valor = saldos.getOrDefault(usuario.getId(), BigDecimal.ZERO);
		return new Saldo(usuario, new Dinheiro(valor));
	}

	public synchronized void creditar(UsuarioId usuario, Dinheiro valor) {
		saldos.merge(usuario.getId(), valor.getValor(), BigDecimal::add);
	}

	public synchronized void debitar(UsuarioId usuario, Dinheiro valor) {
		BigDecimal atual = saldos.getOrDefault(usuario.getId(), BigDecimal.ZERO);
		BigDecimal novo = atual.subtract(valor.getValor());
		if (novo.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalStateException("saldo insuficiente");
		}
		saldos.put(usuario.getId(), novo);
	}

	public boolean existe(UsuarioId usuario) {
		return saldos.containsKey(usuario.getId());
	}

	public void limpar() {
		saldos.clear();
	}
}
