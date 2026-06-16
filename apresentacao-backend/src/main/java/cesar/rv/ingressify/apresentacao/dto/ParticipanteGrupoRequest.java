package cesar.rv.ingressify.apresentacao.dto;

public record ParticipanteGrupoRequest(
        Integer usuarioId,
        Integer quantidade,
        Boolean meiaEntrada,
        String documento
) {}
