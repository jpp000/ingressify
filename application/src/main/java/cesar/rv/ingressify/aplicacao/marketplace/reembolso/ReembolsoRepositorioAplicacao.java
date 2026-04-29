package cesar.rv.ingressify.aplicacao.marketplace.reembolso;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface ReembolsoRepositorioAplicacao {

	List<ReembolsoResumo> pesquisarPorUsuario(UsuarioId usuarioId);
}
