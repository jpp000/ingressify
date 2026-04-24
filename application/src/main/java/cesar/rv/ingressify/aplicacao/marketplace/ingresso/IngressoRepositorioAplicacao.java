package cesar.rv.ingressify.aplicacao.marketplace.ingresso;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;

public interface IngressoRepositorioAplicacao {

	List<IngressoResumo> pesquisarResumos();

	List<IngressoResumo> pesquisarResumosPorProprietario(UsuarioId usuario);

	IngressoResumoExpandido pesquisarResumoExpandido(IngressoId id);
}
