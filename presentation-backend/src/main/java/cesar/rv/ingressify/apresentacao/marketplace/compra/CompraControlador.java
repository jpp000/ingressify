package cesar.rv.ingressify.apresentacao.marketplace.compra;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.anuncio.ComprarAnuncioServicoAplicacao;
import cesar.rv.ingressify.aplicacao.compra.ComprarIngressoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.BackendMapeador;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;

@RestController
@RequestMapping("backend/compra")
public class CompraControlador {

	public record CompraDiretaFormulario(int tipoIngressoId, int quantidade, int compradorId) {
	}

	public record CompraRevendaFormulario(int compradorId) {
	}

	@Autowired
	private ComprarIngressoServicoAplicacao comprarIngressoServicoAplicacao;

	@Autowired
	private ComprarAnuncioServicoAplicacao comprarAnuncioServicoAplicacao;

	@Autowired
	private BackendMapeador mapeador;

	@PostMapping("direta")
	public UUID direta(@RequestBody CompraDiretaFormulario body) {
		return comprarIngressoServicoAplicacao.iniciarCompraDireta(mapeador.paraTipoIngressoId(body.tipoIngressoId()),
				body.quantidade(), new UsuarioId(body.compradorId()));
	}

	@PostMapping("revenda/{anuncioId}")
	public UUID revenda(@PathVariable("anuncioId") int anuncioId, @RequestBody CompraRevendaFormulario body) {
		return comprarAnuncioServicoAplicacao.iniciarCompraRevenda(mapeador.paraAnuncioRevendaId(anuncioId),
				new UsuarioId(body.compradorId()));
	}
}
