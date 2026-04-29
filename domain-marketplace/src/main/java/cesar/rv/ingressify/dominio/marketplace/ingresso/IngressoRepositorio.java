package cesar.rv.ingressify.dominio.marketplace.ingresso;

import java.util.List;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

public interface IngressoRepositorio {

	void salvar(Ingresso ingresso);

	Ingresso obter(IngressoId id);

	List<Ingresso> pesquisarPorProprietario(UsuarioId proprietario);

	List<Ingresso> pesquisarPorEvento(EventoId eventoId);

	List<Ingresso> pesquisarPorTipo(TipoIngressoId tipoIngressoId);

	int contarVendidosPorTipo(TipoIngressoId tipoIngressoId);
}
