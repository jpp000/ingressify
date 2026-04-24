package cesar.rv.ingressify.apresentacao.marketplace.ingresso;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoResumo;
import cesar.rv.ingressify.aplicacao.marketplace.ingresso.IngressoServicoAplicacao;
import cesar.rv.ingressify.apresentacao.BackendMapeador;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;

@RestController
@RequestMapping("backend/ingresso")
public class IngressoControlador {

	public record TransferirFormulario(int novoUsuario) {
	}

	@Autowired
	private IngressoServico ingressoServico;

	@Autowired
	private IngressoServicoAplicacao ingressoServicoAplicacao;

	@Autowired
	private BackendMapeador mapeador;

	@GetMapping("pesquisa")
	public List<IngressoResumo> pesquisa(@RequestParam("usuarioId") int usuarioId) {
		return ingressoServicoAplicacao.pesquisarResumosPorProprietario(new UsuarioId(usuarioId));
	}

	@PostMapping("{id}/transferir")
	public void transferir(@PathVariable("id") String id, @RequestBody TransferirFormulario body) {
		ingressoServico.transferir(mapeador.paraIngressoId(id), new UsuarioId(body.novoUsuario()));
	}
}
