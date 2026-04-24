package cesar.rv.ingressify.apresentacao.marketplace.tipoIngresso;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.tipoIngresso.TipoIngressoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.BackendMapeador;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngresso;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoServico;

@RestController
@RequestMapping("backend/tipoIngresso")
public class TipoIngressoControlador {

	public record TipoIngressoFormulario(int eventoId, String nome, BigDecimal preco, int quantidadeDisponivel,
			int quantidadeTotal) {
	}

	@Autowired
	private TipoIngressoServico tipoIngressoServico;

	@Autowired
	private TipoIngressoServicoAplicacao tipoIngressoServicoAplicacao;

	@Autowired
	private BackendMapeador mapeador;

	@GetMapping("pesquisa")
	public List<TipoIngressoResumo> pesquisa(@RequestParam("eventoId") int eventoId) {
		return tipoIngressoServicoAplicacao.pesquisarResumosPorEvento(new EventoId(eventoId));
	}

	@PostMapping("salvar")
	public void salvar(@RequestBody TipoIngressoFormulario form) {
		TipoIngresso tipo = new TipoIngresso(new EventoId(form.eventoId()), form.nome(), mapeador.paraDinheiro(form.preco()),
				form.quantidadeDisponivel(), form.quantidadeTotal());
		tipoIngressoServico.salvar(tipo);
	}

	@PutMapping("{id}")
	public void atualizar(@PathVariable("id") int id, @RequestBody TipoIngressoFormulario form) {
		TipoIngresso atualizado = new TipoIngresso(mapeador.paraTipoIngressoId(id), new EventoId(form.eventoId()), form.nome(),
				mapeador.paraDinheiro(form.preco()), form.quantidadeDisponivel(), form.quantidadeTotal());
		tipoIngressoServico.salvar(atualizado);
	}
}
