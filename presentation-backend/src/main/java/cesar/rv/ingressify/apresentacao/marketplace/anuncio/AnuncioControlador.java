package cesar.rv.ingressify.apresentacao.marketplace.anuncio;

import java.math.BigDecimal;
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

import cesar.rv.ingressify.aplicacao.marketplace.anuncio.AnuncioResumo;
import cesar.rv.ingressify.aplicacao.marketplace.anuncio.AnuncioServicoAplicacao;
import cesar.rv.ingressify.apresentacao.BackendMapeador;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;

@RestController
@RequestMapping("backend/anuncio")
public class AnuncioControlador {

	public record AnuncioSalvarFormulario(String ingressoId, BigDecimal preco, int vendedorId) {
	}

	@Autowired
	private AnuncioRevendaServico anuncioRevendaServico;

	@Autowired
	private AnuncioServicoAplicacao anuncioServicoAplicacao;

	@Autowired
	private BackendMapeador mapeador;

	@GetMapping("pesquisa")
	public List<AnuncioResumo> pesquisa() {
		return anuncioServicoAplicacao.pesquisarResumos();
	}

	@PostMapping("salvar")
	public void salvar(@RequestBody AnuncioSalvarFormulario form) {
		anuncioRevendaServico.criar(mapeador.paraIngressoId(form.ingressoId()), mapeador.paraDinheiro(form.preco()),
				new UsuarioId(form.vendedorId()));
	}

	@PutMapping("{id}/preco")
	public void alterarPreco(@PathVariable("id") int id, @RequestBody BigDecimal novoPreco) {
		anuncioRevendaServico.alterarPreco(mapeador.paraAnuncioRevendaId(id), mapeador.paraDinheiro(novoPreco));
	}

	@DeleteMapping("{id}")
	public void cancelar(@PathVariable("id") int id) {
		anuncioRevendaServico.cancelar(mapeador.paraAnuncioRevendaId(id));
	}
}
