package cesar.rv.ingressify.aplicacao.marketplace.ingresso;

import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;

public class IngressoServicoAplicacao {

	private final IngressoServico ingressoServico;
	private final IngressoRepositorio ingressoRepositorio;
	private final UsuarioRepositorio usuarioRepositorio;

	public IngressoServicoAplicacao(IngressoServico ingressoServico, IngressoRepositorio ingressoRepositorio,
			UsuarioRepositorio usuarioRepositorio) {
		Validate.notNull(ingressoServico, "ingressoServico");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(usuarioRepositorio, "usuarioRepositorio");
		this.ingressoServico = ingressoServico;
		this.ingressoRepositorio = ingressoRepositorio;
		this.usuarioRepositorio = usuarioRepositorio;
	}

	public Ingresso obter(IngressoId ingressoId) {
		return ingressoServico.obter(ingressoId);
	}

	public List<Ingresso> pesquisarPorProprietario(UsuarioId proprietario) {
		return ingressoRepositorio.pesquisarPorProprietario(proprietario);
	}

	public void transferir(IngressoId ingressoId, UsuarioId donoAtualId, String emailDestinatario) {
		Ingresso ingresso = ingressoServico.obter(ingressoId);
		if (!ingresso.getProprietario().equals(donoAtualId)) {
			throw new IllegalStateException("ingresso não pertence ao usuário atual");
		}
		UsuarioId destino = usuarioRepositorio.obterPorEmail(emailDestinatario).getId();
		ingressoServico.transferir(ingressoId, destino);
	}
}
