package cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoServico;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.StatusAssento;

public class CompraAssentoServicoAplicacao {

    private final MapaAssentosServico mapaServico;
    private final SaldoServico saldoServico;
    private final TransacaoServico transacaoServico;

    public CompraAssentoServicoAplicacao(MapaAssentosServico mapaServico,
            SaldoServico saldoServico, TransacaoServico transacaoServico) {
        Validate.notNull(mapaServico, "mapaServico");
        Validate.notNull(saldoServico, "saldoServico");
        Validate.notNull(transacaoServico, "transacaoServico");
        this.mapaServico = mapaServico;
        this.saldoServico = saldoServico;
        this.transacaoServico = transacaoServico;
    }

    public Dinheiro comprar(List<AssentoId> assentoIds, UsuarioId usuarioId) {
        Validate.notEmpty(assentoIds, "assentoIds");
        Validate.notNull(usuarioId, "usuarioId");

        List<Assento> assentos = assentoIds.stream()
                .map(mapaServico::obterAssento)
                .toList();

        // Verifica que todos os assentos pertencem ao usuário e estão RESERVADO não-expirado
        for (Assento a : assentos) {
            if (a.getStatus() != StatusAssento.RESERVADO) {
                throw new IllegalStateException(
                        "assento " + a.getCodigo() + " não está reservado");
            }
            if (!usuarioId.equals(a.getReservadoPor())) {
                throw new IllegalStateException(
                        "assento " + a.getCodigo() + " não pertence ao usuário");
            }
            if (a.reservaExpirada()) {
                throw new IllegalStateException(
                        "reserva do assento " + a.getCodigo() + " expirou — realize nova reserva");
            }
        }

        BigDecimal total = assentos.stream()
                .map(Assento::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Dinheiro totalDinheiro = new Dinheiro(total);

        saldoServico.debitar(usuarioId, totalDinheiro);
        mapaServico.confirmarVenda(assentoIds, usuarioId);
        transacaoServico.registrar(new Transacao(usuarioId, TipoTransacao.COMPRA, totalDinheiro,
                LocalDateTime.now(), UUID.randomUUID()));

        return totalDinheiro;
    }
}
