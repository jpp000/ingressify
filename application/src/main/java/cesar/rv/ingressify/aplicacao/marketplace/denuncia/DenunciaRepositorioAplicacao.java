package cesar.rv.ingressify.aplicacao.marketplace.denuncia;

import java.util.List;

public interface DenunciaRepositorioAplicacao {

	List<DenunciaResumo> pesquisarPendentes();
}
