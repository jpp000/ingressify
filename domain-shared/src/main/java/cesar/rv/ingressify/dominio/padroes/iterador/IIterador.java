package cesar.rv.ingressify.dominio.padroes.iterador;

public interface IIterador<T> {

	boolean temProximo();

	T proximo();
}
