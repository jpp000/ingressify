package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupo;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusParticipanteGrupo;

public record ParticipanteGrupoResponse(
        int id,
        int grupoCompraId,
        int usuarioId,
        int quantidade,
        boolean meiaEntrada,
        String documento,
        BigDecimal valor,
        StatusParticipanteGrupo status,
        LocalDateTime pagoEm
) {
    public static ParticipanteGrupoResponse fromDomain(ParticipanteGrupo p) {
        return new ParticipanteGrupoResponse(
                p.getId().getId(),
                p.getGrupoCompraId().getId(),
                p.getUsuarioId().getId(),
                p.getQuantidade(),
                p.isMeiaEntrada(),
                p.getDocumento(),
                p.getValor().getValor(),
                p.getStatus(),
                p.getPagoEm());
    }
}
