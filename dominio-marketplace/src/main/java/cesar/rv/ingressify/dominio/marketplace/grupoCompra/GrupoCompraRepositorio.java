package cesar.rv.ingressify.dominio.marketplace.grupoCompra;

import java.time.LocalDateTime;
import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;

public interface GrupoCompraRepositorio {

	void salvar(GrupoCompra grupo);

	GrupoCompra obter(GrupoCompraId id);

	List<GrupoCompra> listarPorEvento(EventoId eventoId);

	List<GrupoCompra> listarPorLider(UsuarioId liderId);

	List<GrupoCompra> listarAbertosExpirados(LocalDateTime agora);
}
