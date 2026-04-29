package cesar.rv.ingressify.aplicacao.marketplace.ingresso;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface IngressoRepositorioAplicacao {

	List<IngressoResumo> pesquisarResumosPorProprietario(UsuarioId proprietarioId);
}
