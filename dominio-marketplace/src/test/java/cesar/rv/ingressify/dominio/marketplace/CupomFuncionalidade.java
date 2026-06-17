package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.marketplace.cupom.Cupom;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomId;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomRepositorio;
import cesar.rv.ingressify.dominio.marketplace.cupom.CupomServico;
import cesar.rv.ingressify.dominio.marketplace.cupom.TipoCupom;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class CupomFuncionalidade {

    private static final EventoId EVENTO_1 = new EventoId(1);
    private static final EventoId EVENTO_99 = new EventoId(99);

    private final CupomRepositorioMemoria repositorio = new CupomRepositorioMemoria();
    private final CupomServico servico = new CupomServico(repositorio);

    private Cupom cupomAtual;
    private Dinheiro valorComDesconto;
    private Throwable excecao;

    @Dado("um cupom percentual de {int}% para o evento")
    public void cupomPercentual(int percentual) {
        cupomAtual = new Cupom("PROMO" + percentual, TipoCupom.PERCENTUAL,
                BigDecimal.valueOf(percentual), EVENTO_1, Dinheiro.ZERO, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        servico.criar(cupomAtual);
    }

    @Dado("um cupom de valor fixo de R${int},00 para o evento")
    public void cupomValorFixo(int valor) {
        cupomAtual = new Cupom("FIXO" + valor, TipoCupom.VALOR_FIXO,
                BigDecimal.valueOf(valor), EVENTO_1, Dinheiro.ZERO, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        servico.criar(cupomAtual);
    }

    @Dado("um cupom percentual de {int}% já expirado")
    public void cupomExpirado(int percentual) {
        cupomAtual = new Cupom("EXPIRADO", TipoCupom.PERCENTUAL,
                BigDecimal.valueOf(percentual), EVENTO_1, Dinheiro.ZERO, 0,
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
        servico.criar(cupomAtual);
    }

    @Dado("um cupom percentual de {int}% ainda não vigente")
    public void cupomAindaNaoVigente(int percentual) {
        cupomAtual = new Cupom("FUTURO", TipoCupom.PERCENTUAL,
                BigDecimal.valueOf(percentual), EVENTO_1, Dinheiro.ZERO, 0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(30));
        servico.criar(cupomAtual);
    }

    @Dado("um cupom com limite de {int} uso já consumido")
    public void cupomUsosEsgotados(int limite) {
        cupomAtual = new Cupom("ESGOTADO", TipoCupom.PERCENTUAL,
                BigDecimal.valueOf(10), EVENTO_1, Dinheiro.ZERO, limite,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        servico.criar(cupomAtual);
        cupomAtual.registrarUso();
        repositorio.salvar(cupomAtual);
    }

    @Dado("um cupom criado para o evento {int}")
    public void cupomDeOutroEvento(int eventoId) {
        cupomAtual = new Cupom("OUTRO", TipoCupom.PERCENTUAL,
                BigDecimal.valueOf(10), new EventoId(eventoId), Dinheiro.ZERO, 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        servico.criar(cupomAtual);
    }

    @Dado("um cupom com valor mínimo de R${int},00")
    public void cupomComValorMinimo(int minimo) {
        cupomAtual = new Cupom("MINIMO", TipoCupom.PERCENTUAL,
                BigDecimal.valueOf(10), EVENTO_1,
                new Dinheiro(BigDecimal.valueOf(minimo)), 0,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30));
        servico.criar(cupomAtual);
    }

    @Quando("aplico o cupom a uma compra de R${int},00")
    public void aplicarCupom(int valorCompra) {
        try {
            valorComDesconto = servico.previsualizar(cupomAtual.getCodigo(),
                    new Dinheiro(BigDecimal.valueOf(valorCompra)), EVENTO_1, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("aplico o cupom a uma compra do evento {int}")
    public void aplicarCupomOutroEvento(int eventoId) {
        try {
            valorComDesconto = servico.previsualizar(cupomAtual.getCodigo(),
                    new Dinheiro(BigDecimal.valueOf(200)), new EventoId(eventoId), LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("tento aplicar o cupom expirado")
    public void tentarAplicarExpirado() {
        try {
            servico.previsualizar("EXPIRADO", new Dinheiro(BigDecimal.valueOf(200)), EVENTO_1, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("tento aplicar o cupom ainda não vigente")
    public void tentarAplicarAindaNaoVigente() {
        try {
            servico.previsualizar("FUTURO", new Dinheiro(BigDecimal.valueOf(200)), EVENTO_1, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("tento aplicar o cupom esgotado")
    public void tentarAplicarEsgotado() {
        try {
            servico.previsualizar("ESGOTADO", new Dinheiro(BigDecimal.valueOf(200)), EVENTO_1, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("tento aplicar o cupom a uma compra de R${int},00")
    public void tentarAplicarAbaixoDoMinimo(int valorCompra) {
        try {
            servico.previsualizar("MINIMO", new Dinheiro(BigDecimal.valueOf(valorCompra)), EVENTO_1, LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("consumo o cupom uma vez em uma compra de R${int},00")
    public void consumirCupom(int valorCompra) {
        servico.consumir(cupomAtual.getCodigo(),
                new Dinheiro(BigDecimal.valueOf(valorCompra)), EVENTO_1, LocalDateTime.now());
    }

    @Então("o valor com desconto é R${int},00")
    public void verificarValorComDesconto(int esperado) {
        assertNotNull(valorComDesconto, "desconto não foi calculado");
        assertEquals(0, valorComDesconto.getValor().compareTo(BigDecimal.valueOf(esperado)),
                "esperado R$" + esperado + " mas foi R$" + valorComDesconto.getValor());
    }

    @Então("o cupom é rejeitado com mensagem {string}")
    public void cupomRejeitado(String fragmento) {
        assertNotNull(excecao, "era esperada uma exceção mas nenhuma foi lançada");
        assertTrue(excecao.getMessage().contains(fragmento),
                "esperado '" + fragmento + "' em: " + excecao.getMessage());
    }

    @Então("o cupom registra {int} uso")
    public void cupomRegistraUso(int usos) {
        Cupom atualizado = repositorio.buscarPorCodigo(cupomAtual.getCodigo()).orElseThrow();
        assertEquals(usos, atualizado.getUsos());
    }

    // ── Repositório em memória ─────────────────────────────────────────────────

    static class CupomRepositorioMemoria implements CupomRepositorio {
        private final Map<Integer, Cupom> porId = new HashMap<>();
        private final Map<String, Cupom> porCodigo = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(Cupom cupom) {
            if (cupom.getId() == null) cupom.atribuirId(new CupomId(proximoId++));
            porId.put(cupom.getId().getId(), cupom);
            porCodigo.put(cupom.getCodigo(), cupom);
        }

        @Override
        public Cupom obter(CupomId id) {
            Cupom c = porId.get(id.getId());
            if (c == null) throw new IllegalArgumentException("Cupom não encontrado: " + id.getId());
            return c;
        }

        @Override
        public Optional<Cupom> buscarPorCodigo(String codigo) {
            return Optional.ofNullable(porCodigo.get(codigo.trim().toUpperCase()));
        }

        @Override
        public List<Cupom> pesquisarPorEvento(EventoId eventoId) {
            return porId.values().stream()
                    .filter(c -> c.getEventoId().equals(eventoId)).toList();
        }
    }
}
