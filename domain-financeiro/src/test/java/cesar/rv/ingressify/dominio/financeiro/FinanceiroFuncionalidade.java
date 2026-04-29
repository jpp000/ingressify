package cesar.rv.ingressify.dominio.financeiro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.pagamento.Pagamento;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoId;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoRepositorio;
import cesar.rv.ingressify.dominio.financeiro.pagamento.PagamentoServico;
import cesar.rv.ingressify.dominio.financeiro.saldo.Saldo;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoRepositorio;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoId;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class FinanceiroFuncionalidade {

    protected final PagamentoServico pagamentoServico;
    protected final SaldoServico saldoServico;
    protected final SaldoRepositorioMemoria saldoRepositorio;
    protected final TransacaoRepositorioMemoria transacaoRepositorio;
    protected Throwable excecao;

    public FinanceiroFuncionalidade() {
        PagamentoRepositorioMemoria pagamentoRepo = new PagamentoRepositorioMemoria();
        saldoRepositorio = new SaldoRepositorioMemoria();
        transacaoRepositorio = new TransacaoRepositorioMemoria();

        pagamentoServico = new PagamentoServico(pagamentoRepo);
        saldoServico = new SaldoServico(saldoRepositorio);
    }

    static class PagamentoRepositorioMemoria implements PagamentoRepositorio {
        private final Map<PagamentoId, Pagamento> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(Pagamento pagamento) {
            if (pagamento.getId() == null) {
                pagamento.atribuirId(new PagamentoId(proximoId++));
            }
            dados.put(pagamento.getId(), pagamento);
        }

        @Override
        public Pagamento obter(PagamentoId id) {
            Pagamento p = dados.get(id);
            if (p == null) {
                throw new IllegalArgumentException("Pagamento não encontrado: " + id);
            }
            return p;
        }

        @Override
        public Optional<Pagamento> obterPorCorrelacao(UUID correlacaoId) {
            for (Pagamento p : dados.values()) {
                if (correlacaoId.equals(p.getCorrelacaoId())) {
                    return Optional.of(p);
                }
            }
            return Optional.empty();
        }
    }

    static class SaldoRepositorioMemoria implements SaldoRepositorio {
        private final Map<UsuarioId, Saldo> dados = new HashMap<>();

        @Override
        public void salvar(Saldo saldo) {
            dados.put(saldo.getUsuario(), saldo);
        }

        @Override
        public Saldo obter(UsuarioId usuario) {
            Saldo s = dados.get(usuario);
            if (s == null) {
                throw new IllegalArgumentException("Saldo não encontrado para usuário: " + usuario);
            }
            return s;
        }
    }

    static class TransacaoRepositorioMemoria implements TransacaoRepositorio {
        private final Map<TransacaoId, Transacao> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(Transacao transacao) {
            if (transacao.getId() == null) {
                transacao.atribuirId(new TransacaoId(proximoId++));
            }
            dados.put(transacao.getId(), transacao);
        }

		@Override
		public List<Transacao> pesquisarPorUsuario(UsuarioId usuario) {
			List<Transacao> resultado = new ArrayList<>();
			for (Transacao t : dados.values()) {
				if (t.getUsuario().equals(usuario)) {
					resultado.add(t);
				}
			}
			return resultado;
		}

		@Override
		public List<Transacao> pesquisarPorUsuarioOrdenadoDesc(UsuarioId usuario) {
			List<Transacao> lista = pesquisarPorUsuario(usuario);
			lista.sort((a, b) -> b.getData().compareTo(a.getData()));
			return lista;
		}
	}
}
