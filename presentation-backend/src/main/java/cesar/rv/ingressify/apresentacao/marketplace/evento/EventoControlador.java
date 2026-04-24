package cesar.rv.ingressify.apresentacao.marketplace.evento;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoResumoExpandido;
import cesar.rv.ingressify.aplicacao.marketplace.evento.EventoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.BackendMapeador;
import cesar.rv.ingressify.dominio.marketplace.evento.Evento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoServico;

@RestController
@RequestMapping("backend/evento")
public class EventoControlador {

	@Autowired
	private EventoServico eventoServico;

	@Autowired
	private EventoServicoAplicacao eventoServicoAplicacao;

	@Autowired
	private BackendMapeador mapeador;

	@GetMapping("pesquisa")
	public List<EventoResumo> pesquisa() {
		return eventoServicoAplicacao.pesquisarAtivosFuturos();
	}

	@GetMapping("catalogo")
	public List<EventoResumo> catalogo() {
		return eventoServicoAplicacao.pesquisarAtivosFuturos();
	}

	@GetMapping("criacao")
	public EventoFormulario criacao() {
		return new EventoFormulario();
	}

	@PostMapping("salvar")
	public void salvar(@RequestBody EventoFormulario formulario) {
		EventoFormulario.EventoDto dto = formulario.getEvento();
		Evento evento = new Evento(dto.nome(), dto.dataHora(), dto.local(), dto.capacidade());
		eventoServico.salvar(evento);
	}

	@PostMapping("{id}/cancelar")
	public void cancelar(@PathVariable("id") int id) {
		eventoServico.cancelar(mapeador.paraEventoId(id));
	}

	@PutMapping("{id}")
	public void atualizar(@PathVariable("id") int id, @RequestBody EventoFormulario formulario) {
		EventoFormulario.EventoDto dto = formulario.getEvento();
		eventoServico.atualizar(mapeador.paraEventoId(id), dto.nome(), dto.dataHora(), dto.local(), dto.capacidade());
	}

	@DeleteMapping("{id}")
	public void remover(@PathVariable("id") int id) {
		eventoServico.remover(mapeador.paraEventoId(id));
	}

	@GetMapping("{id}/expandido")
	public EventoResumoExpandido expandido(@PathVariable("id") int id) {
		return eventoServicoAplicacao.pesquisarResumoExpandido(new EventoId(id)).orElseThrow();
	}
}
