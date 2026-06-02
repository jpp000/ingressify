package cesar.rv.ingressify.dominio.padroes.observador;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PublicadorBaseTest {

	@Test
	void notificaUmObservadorRegistrado() {
		var publicador = new PublicadorBase<String>();
		List<String> recebidos = new ArrayList<>();
		publicador.registrar(recebidos::add);

		publicador.notificarTodos("evento-cancelado");

		assertEquals(1, recebidos.size());
		assertEquals("evento-cancelado", recebidos.get(0));
	}

	@Test
	void notificaTresObservadoresSimultaneamente() {
		var publicador = new PublicadorBase<String>();
		List<String> log = new ArrayList<>();

		publicador.registrar(dado -> log.add("observador-1: " + dado));
		publicador.registrar(dado -> log.add("observador-2: " + dado));
		publicador.registrar(dado -> log.add("observador-3: " + dado));

		publicador.notificarTodos("cancelamento");

		assertEquals(3, log.size());
	}

	@Test
	void observadorRemovidoNaoRecebeNotificacao() {
		var publicador = new PublicadorBase<String>();
		List<String> recebidos = new ArrayList<>();
		Observador<String> obs = recebidos::add;
		publicador.registrar(obs);
		publicador.remover(obs);

		publicador.notificarTodos("evento");

		assertEquals(0, recebidos.size());
	}

	@Test
	void totalObservadoresRefleteTamanhoCorreto() {
		var publicador = new PublicadorBase<Integer>();
		assertEquals(0, publicador.totalObservadores());

		publicador.registrar(x -> {});
		publicador.registrar(x -> {});
		assertEquals(2, publicador.totalObservadores());
	}

	@Test
	void cadaObservadorRecebeExatamenteUmaChamadaPorNotificacao() {
		var publicador = new PublicadorBase<String>();
		int[] contagem = {0};
		publicador.registrar(dado -> contagem[0]++);

		publicador.notificarTodos("a");
		publicador.notificarTodos("b");
		publicador.notificarTodos("c");

		assertEquals(3, contagem[0]);
	}
}
