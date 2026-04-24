package cesar.rv.ingressify.dominio.marketplace.evento;

public interface EventoRepositorio {

	void salvar(Evento evento);

	Evento obter(EventoId id);

	void remover(EventoId id);
}
