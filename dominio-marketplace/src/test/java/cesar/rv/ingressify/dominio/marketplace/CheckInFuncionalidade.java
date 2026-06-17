package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.checkin.CheckinServico;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckin;
import cesar.rv.ingressify.dominio.marketplace.checkin.RegistroCheckinRepositorio;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CheckInFuncionalidade {

	private static final EventoId EVENTO = new EventoId(1);
	private static final TipoIngressoId TIPO = new TipoIngressoId(1);
	private static final UsuarioId COMPRADOR = new UsuarioId(1);

	private final IngressoRepositorioMemoria ingressoRepo = new IngressoRepositorioMemoria();
	private final RegistroCheckinRepositorioMemoria checkinRepo = new RegistroCheckinRepositorioMemoria();
	private final CheckinServico checkinServico = new CheckinServico(checkinRepo, ingressoRepo);

	private IngressoId ingressoId;
	private RegistroCheckin registroRealizado;
	private int operadorIdAtual = 1;
	private Throwable excecao;

	@Given("um ingresso ativo para check-in")
	public void ingressoAtivoParaCheckIn() {
		Ingresso ingresso = new Ingresso(TIPO, EVENTO, COMPRADOR);
		ingressoRepo.salvar(ingresso);
		ingressoId = ingresso.getId();
	}

	@Given("um ingresso com status UTILIZADO")
	public void ingressoUtilizado() {
		Ingresso ingresso = new Ingresso(new IngressoId(UUID.randomUUID()), TIPO, EVENTO,
				COMPRADOR, StatusIngresso.UTILIZADO, false);
		ingressoRepo.salvar(ingresso);
		ingressoId = ingresso.getId();
	}

	@Given("um ingresso com status CANCELADO")
	public void ingressoCancelado() {
		Ingresso ingresso = new Ingresso(new IngressoId(UUID.randomUUID()), TIPO, EVENTO,
				COMPRADOR, StatusIngresso.CANCELADO, false);
		ingressoRepo.salvar(ingresso);
		ingressoId = ingresso.getId();
	}

	@Given("um ingresso com status EM_REVENDA")
	public void ingressoEmRevenda() {
		Ingresso ingresso = new Ingresso(new IngressoId(UUID.randomUUID()), TIPO, EVENTO,
				COMPRADOR, StatusIngresso.EM_REVENDA, false);
		ingressoRepo.salvar(ingresso);
		ingressoId = ingresso.getId();
	}

	@Given("um ingresso ativo bloqueado para reembolso")
	public void ingressoBloqueado() {
		Ingresso ingresso = new Ingresso(new IngressoId(UUID.randomUUID()), TIPO, EVENTO,
				COMPRADOR, StatusIngresso.ATIVO, true);
		ingressoRepo.salvar(ingresso);
		ingressoId = ingresso.getId();
	}

	@Given("o operador tem id {int}")
	public void operadorComId(int id) {
		operadorIdAtual = id;
	}

	@When("o operador realiza o check-in do ingresso")
	public void realizarCheckIn() {
		Ingresso ingresso = ingressoRepo.obter(ingressoId);
		registroRealizado = checkinServico.registrar(ingresso, new UsuarioId(operadorIdAtual), LocalDateTime.now());
	}

	@When("o operador tenta check-in no ingresso já utilizado")
	public void tentarCheckInUtilizado() { tentarCheckIn(); }

	@When("o operador tenta check-in no ingresso cancelado")
	public void tentarCheckInCancelado() { tentarCheckIn(); }

	@When("o operador tenta check-in no ingresso em revenda")
	public void tentarCheckInEmRevenda() { tentarCheckIn(); }

	@When("o operador tenta check-in no ingresso bloqueado")
	public void tentarCheckInBloqueado() { tentarCheckIn(); }

	@When("bloqueio o ingresso para reembolso")
	public void bloquearIngressoParaReembolso() {
		Ingresso ingresso = ingressoRepo.obter(ingressoId);
		ingresso.bloquearParaReembolso();
		ingressoRepo.salvar(ingresso);
	}

	@Then("o check-in é registrado com sucesso")
	public void checkInRegistrado() { assertNotNull(registroRealizado); }

	@Then("o ingresso fica com status UTILIZADO")
	public void ingressoUtilizadoAposCheckIn() {
		Ingresso ingresso = ingressoRepo.obter(ingressoId);
		assertEquals(StatusIngresso.UTILIZADO, ingresso.getStatus());
	}

	@Then("o check-in é rejeitado")
	public void checkInRejeitado() { assertNotNull(excecao); }

	@Then("o registro de check-in contém o operador {int}")
	public void registroContemOperador(int operadorId) {
		assertEquals(new UsuarioId(operadorId), registroRealizado.getOperadorId());
	}

	@Then("a transferência do ingresso utilizado é rejeitada")
	public void transferenciaBloqueadaAposUso() {
		Throwable ex = null;
		try {
			ingressoRepo.obter(ingressoId).transferir(new UsuarioId(99));
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);
	}

	@Then("a transferência do ingresso bloqueado é rejeitada")
	public void transferenciaBloqueadaPorReembolso() {
		Throwable ex = null;
		try {
			ingressoRepo.obter(ingressoId).transferir(new UsuarioId(99));
		} catch (Exception e) {
			ex = e;
		}
		assertNotNull(ex);
	}

	private void tentarCheckIn() {
		try {
			Ingresso ingresso = ingressoRepo.obter(ingressoId);
			checkinServico.registrar(ingresso, new UsuarioId(operadorIdAtual), LocalDateTime.now());
		} catch (Exception e) {
			excecao = e;
		}
	}

	static class IngressoRepositorioMemoria implements IngressoRepositorio {
		private final Map<IngressoId, Ingresso> dados = new HashMap<>();

		@Override
		public void salvar(Ingresso i) {
			if (i.getId() == null) i.atribuirId(new IngressoId(UUID.randomUUID()));
			dados.put(i.getId(), i);
		}

		@Override
		public Ingresso obter(IngressoId id) {
			Ingresso i = dados.get(id);
			if (i == null) throw new IllegalArgumentException("Ingresso não encontrado: " + id);
			return i;
		}

		@Override
		public Ingresso obterPorCodigo(String codigo) {
			return dados.values().stream()
					.filter(i -> i.getCodigo().equalsIgnoreCase(codigo.trim()))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("Ingresso nao encontrado para o codigo informado"));
		}

		@Override
		public List<Ingresso> pesquisarPorProprietario(UsuarioId p) {
			return dados.values().stream().filter(i -> i.getProprietario().equals(p)).toList();
		}

		@Override
		public List<Ingresso> pesquisarPorEvento(EventoId e) {
			return dados.values().stream().filter(i -> i.getEventoId().equals(e)).toList();
		}

		@Override
		public List<Ingresso> pesquisarPorTipo(TipoIngressoId t) {
			return dados.values().stream().filter(i -> i.getTipoIngressoId().equals(t)).toList();
		}

		@Override
		public int contarVendidosPorTipo(TipoIngressoId t) {
			return (int) dados.values().stream().filter(i -> i.getTipoIngressoId().equals(t)).count();
		}
	}

	static class RegistroCheckinRepositorioMemoria implements RegistroCheckinRepositorio {
		private final List<RegistroCheckin> dados = new ArrayList<>();

		@Override
		public void salvar(RegistroCheckin r) { dados.add(r); }

		@Override
		public List<RegistroCheckin> pesquisarPorEvento(EventoId e) {
			return dados.stream().filter(r -> r.getEventoId().equals(e)).toList();
		}
	}
}
