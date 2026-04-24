package cesar.rv.ingressify.aplicacao.financeiro.transacao;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface TransacaoRepositorioAplicacao {

	List<TransacaoResumo> pesquisarHistorico(UsuarioId usuario);
}
