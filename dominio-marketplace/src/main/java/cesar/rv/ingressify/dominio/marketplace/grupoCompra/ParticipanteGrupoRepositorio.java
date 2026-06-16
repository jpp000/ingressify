package cesar.rv.ingressify.dominio.marketplace.grupoCompra;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface ParticipanteGrupoRepositorio {

	void salvar(ParticipanteGrupo participante);

	List<ParticipanteGrupo> listarPorGrupo(GrupoCompraId grupoCompraId);

	Optional<ParticipanteGrupo> buscarPorGrupoEUsuario(GrupoCompraId grupoCompraId, UsuarioId usuarioId);

	List<ParticipanteGrupo> listarPorUsuario(UsuarioId usuarioId);
}
