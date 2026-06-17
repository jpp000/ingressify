package cesar.rv.ingressify.apresentacao.dto;

import java.util.List;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;

public record MapaAssentosResponse(
        int id,
        int eventoId,
        int totalLinhas,
        int totalColunas,
        List<AssentoResponse> assentos
) {
    public static MapaAssentosResponse fromDomain(MapaAssentos m, List<Assento> assentos) {
        return new MapaAssentosResponse(
                m.getId().getId(),
                m.getEventoId().getId(),
                m.getTotalLinhas(),
                m.getTotalColunas(),
                assentos.stream().map(AssentoResponse::fromDomain).toList());
    }
}
