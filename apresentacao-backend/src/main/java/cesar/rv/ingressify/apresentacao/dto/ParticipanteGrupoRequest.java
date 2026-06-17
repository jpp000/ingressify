package cesar.rv.ingressify.apresentacao.dto;

public record ParticipanteGrupoRequest(
        Integer usuarioId,
        String email,
        Integer quantidade,
        Boolean meiaEntrada,
        String documento
) {}
