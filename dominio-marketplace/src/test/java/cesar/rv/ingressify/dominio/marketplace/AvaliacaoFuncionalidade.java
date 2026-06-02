package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.Avaliacao;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoId;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.avaliacao.AvaliacaoServico;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class AvaliacaoFuncionalidade {

	private static final EventoId EVENTO = new EventoId(1);
	private static final UsuarioId USUARIO = new UsuarioId(1);
	private static final UsuarioId ORGANIZADOR = new UsuarioId(2);

	private final AvaliacaoRepositorioMemoria avaliacaoRepo = new AvaliacaoRepositorioMemoria();
	private final AvaliacaoServico avaliacaoServico = new AvaliacaoServico(avaliacaoRepo);

	private Avaliacao avaliacao;
	private Throwable excecao;

	@Given("uma avaliação com nota {int} e comentário {string}")
	public void avaliacaoComNotaEComentario(int nota, String comentario) {
		avaliacao = new Avaliacao(EVENTO, USUARIO, nota, comentario, LocalDateTime.now());
	}

	@Given("uma avaliação com nota {int} e sem comentário")
	public void avaliacaoSemComentario(int nota) {
		avaliacao = new Avaliacao(EVENTO, USUARIO, nota, null, LocalDateTime.now());
	}

	@Given("avaliações com notas {int}, {int} e {int} para o mesmo evento")
	public void avaliacoesComNotas(int n1, int n2, int n3) {
		avaliacaoServico.salvar(new Avaliacao(EVENTO, new UsuarioId(1), n1, null, LocalDateTime.now()));
		avaliacaoServico.salvar(new Avaliacao(EVENTO, new UsuarioId(2), n2, null, LocalDateTime.now()));
		avaliacaoServico.salvar(new Avaliacao(EVENTO, new UsuarioId(3), n3, null, LocalDateTime.now()));
	}

	@When("salvo a avaliação")
	public void salvarAvaliacao() {
		avaliacaoServico.salvar(avaliacao);
	}

	@When("tento criar uma avaliação com nota {int}")
	public void criarAvaliacaoComNota(int nota) {
		try {
			avaliacao = new Avaliacao(EVENTO, USUARIO, nota, null, LocalDateTime.now());
		} catch (Exception e) {
			excecao = e;
		}
	}

	@When("edito a avaliação para nota {int} e comentário {string}")
	public void editarAvaliacao(int nota, String comentario) {
		avaliacao.editar(nota, comentario, LocalDateTime.now());
	}

	@And("o organizador responde com {string}")
	public void organizadorResponde(String texto) {
		avaliacaoServico.responder(avaliacao.getId(), texto, LocalDateTime.now());
		avaliacao = avaliacaoRepo.obter(avaliacao.getId());
	}

	@When("calculo a média do evento")
	public void calcularMedia() {
	}

	@Then("a avaliação é persistida com nota {int}")
	public void avaliacaoComNota(int nota) {
		assertNotNull(avaliacao.getId());
		assertEquals(nota, avaliacao.getNota());
	}

	@Then("a criação é rejeitada por nota inválida")
	public void criacaoRejeitadaNota() {
		assertNotNull(excecao);
	}

	@Then("a avaliação fica com nota {int} e comentário {string}")
	public void avaliacaoFicaComNotaEComentario(int nota, String comentario) {
		assertEquals(nota, avaliacao.getNota());
		assertEquals(comentario, avaliacao.getComentario());
	}

	@Then("a avaliação contém resposta do organizador {string}")
	public void avaliacaoContemResposta(String texto) {
		assertEquals(texto, avaliacao.getRespostaOrganizador());
	}

	@Then("a média é {double}")
	public void mediaDasAvaliacoes(double esperada) {
		assertEquals(esperada, avaliacaoServico.mediaPorEvento(EVENTO), 0.01);
	}

	static class AvaliacaoRepositorioMemoria implements AvaliacaoRepositorio {
		private final Map<AvaliacaoId, Avaliacao> dados = new HashMap<>();
		private int proximoId = 1;

		@Override
		public Avaliacao obter(AvaliacaoId id) {
			Avaliacao a = dados.get(id);
			if (a == null) throw new IllegalArgumentException("Avaliação não encontrada: " + id);
			return a;
		}

		@Override
		public void salvar(Avaliacao avaliacao) {
			if (avaliacao.getId() == null) {
				avaliacao.atribuirId(new AvaliacaoId(proximoId++));
			}
			dados.put(avaliacao.getId(), avaliacao);
		}

		@Override
		public Optional<Avaliacao> buscarPorEventoEUsuario(EventoId eventoId, UsuarioId usuarioId) {
			return dados.values().stream()
					.filter(a -> a.getEventoId().equals(eventoId) && a.getUsuarioId().equals(usuarioId))
					.findFirst();
		}

		@Override
		public List<Avaliacao> pesquisarPorEvento(EventoId eventoId) {
			return dados.values().stream().filter(a -> a.getEventoId().equals(eventoId)).toList();
		}
	}
}
