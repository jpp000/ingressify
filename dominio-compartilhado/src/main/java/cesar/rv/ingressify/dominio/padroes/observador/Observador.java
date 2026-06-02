package cesar.rv.ingressify.dominio.padroes.observador;

public interface Observador<T> {

	void notificar(T dado);
}
