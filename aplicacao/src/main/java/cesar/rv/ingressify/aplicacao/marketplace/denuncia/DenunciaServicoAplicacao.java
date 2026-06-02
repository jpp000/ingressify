package cesar.rv.ingressify.aplicacao.marketplace.denuncia;

import java.time.LocalDateTime;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioServico;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevenda;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaId;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.AnuncioRevendaServico;
import cesar.rv.ingressify.dominio.marketplace.anuncioRevenda.StatusAnuncio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DecisaoModeracao;
import cesar.rv.ingressify.dominio.marketplace.denuncia.Denuncia;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaServico;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenuncia;

public class DenunciaServicoAplicacao {

	private final DenunciaServico denunciaServico;
	private final AnuncioRevendaServico anuncioRevendaServico;
	private final AnuncioRevendaRepositorio anuncioRevendaRepositorio;
	private final UsuarioServico usuarioServico;

	public DenunciaServicoAplicacao(DenunciaServico denunciaServico, AnuncioRevendaServico anuncioRevendaServico,
			AnuncioRevendaRepositorio anuncioRevendaRepositorio, UsuarioServico usuarioServico) {
		Validate.notNull(denunciaServico, "denunciaServico");
		Validate.notNull(anuncioRevendaServico, "anuncioRevendaServico");
		Validate.notNull(anuncioRevendaRepositorio, "anuncioRevendaRepositorio");
		Validate.notNull(usuarioServico, "usuarioServico");
		this.denunciaServico = denunciaServico;
		this.anuncioRevendaServico = anuncioRevendaServico;
		this.anuncioRevendaRepositorio = anuncioRevendaRepositorio;
		this.usuarioServico = usuarioServico;
	}

	public DenunciaId denunciar(AnuncioRevendaId anuncioId, UsuarioId denuncianteId, MotivoDenuncia motivo,
			String descricao) {
		AnuncioRevenda a = anuncioRevendaRepositorio.obter(anuncioId);
		if (a.getStatus() != StatusAnuncio.DISPONIVEL && a.getStatus() != StatusAnuncio.RESERVADO) {
			throw new IllegalStateException("anúncio não pode ser denunciado neste status");
		}
		Denuncia d = denunciaServico.registrar(anuncioId, denuncianteId, motivo, descricao, LocalDateTime.now());
		return d.getId();
	}

	public void decidir(DenunciaId denunciaId, DecisaoModeracao decisao) {
		LocalDateTime agora = LocalDateTime.now();
		switch (decisao) {
		case ARQUIVADA -> denunciaServico.decidir(denunciaId, decisao, agora);
		case VENDEDOR_AVISADO -> denunciaServico.decidir(denunciaId, decisao, agora);
		case ANUNCIO_REMOVIDO -> {
			Denuncia d = denunciaServico.obter(denunciaId);
			anuncioRevendaServico.cancelar(d.getAnuncioId());
			denunciaServico.decidir(denunciaId, decisao, agora);
		}
		case VENDEDOR_BLOQUEADO -> {
			Denuncia d = denunciaServico.obter(denunciaId);
			AnuncioRevenda anuncio = anuncioRevendaRepositorio.obter(d.getAnuncioId());
			usuarioServico.bloquearRevenda(anuncio.getVendedor());
			anuncioRevendaServico.cancelar(d.getAnuncioId());
			denunciaServico.decidir(denunciaId, decisao, agora);
		}
		}
	}
}
