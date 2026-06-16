package cesar.rv.ingressify.apresentacao.dto;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusGrupoCompra;

public record GrupoCompraResponse(
        int id,
        int eventoId,
        int tipoIngressoId,
        int liderId,
        int quantidadeTotal,
        LocalDateTime prazoPagamento,
        StatusGrupoCompra status,
        LocalDateTime criadoEm
) {
    public static GrupoCompraResponse fromDomain(GrupoCompra g) {
        return new GrupoCompraResponse(
                g.getId().getId(),
                g.getEventoId().getId(),
                g.getTipoIngressoId().getId(),
                g.getLiderId().getId(),
                g.getQuantidadeTotal(),
                g.getPrazoPagamento(),
                g.getStatus(),
                g.getCriadoEm());
    }
}
