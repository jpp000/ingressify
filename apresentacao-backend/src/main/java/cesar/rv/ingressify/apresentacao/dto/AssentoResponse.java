package cesar.rv.ingressify.apresentacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.StatusAssento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;

public record AssentoResponse(
        int id,
        int eventoId,
        String secao,
        String codigo,
        TipoAssento tipo,
        BigDecimal preco,
        StatusAssento status,
        Integer reservadoPor,
        LocalDateTime reservadoAte
) {
    public static AssentoResponse fromDomain(Assento a) {
        return new AssentoResponse(
                a.getId().getId(),
                a.getEventoId().getId(),
                a.getSecao(),
                a.getCodigo(),
                a.getTipo(),
                a.getPreco(),
                a.getStatus(),
                a.getReservadoPor() != null ? a.getReservadoPor().getId() : null,
                a.getReservadoAte());
    }
}
