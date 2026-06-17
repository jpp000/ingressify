package cesar.rv.ingressify.dominio.padroes.observador;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;

public class PublicadorBase<T> {

	private final List<Observador<T>> observadores = new ArrayList<>();

	public void registrar(Observador<T> observador) {
		Validate.notNull(observador, "observador");
		observadores.add(observador);
	}

	public void remover(Observador<T> observador) {
		observadores.remove(observador);
	}

	public int totalObservadores() {
		return observadores.size();
	}

	protected void notificarTodos(T dado) {
		Validate.notNull(dado, "dado");
		for (Observador<T> o : new ArrayList<>(observadores)) {
			o.notificar(dado);
		}
	}
}
