package cesar.rv.ingressify.dominio.marketplace.ingresso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.compra.CompraPendente;

public class IngressoServico {

	private final IngressoRepositorio repositorio;

	public IngressoServico(IngressoRepositorio repositorio) {
		Validate.notNull(repositorio, "repositorio");
		this.repositorio = repositorio;
	}

	public void salvar(Ingresso ingresso) {
		repositorio.salvar(ingresso);
	}

	public Ingresso obter(IngressoId id) {
		return repositorio.obter(id);
	}

	public void transferir(IngressoId id, UsuarioId novoDono) {
		Ingresso i = repositorio.obter(id);
		i.transferir(novoDono);
		repositorio.salvar(i);
	}

	public void concluirRevenda(IngressoId id, UsuarioId comprador) {
		Ingresso i = repositorio.obter(id);
		i.concluirRevenda(comprador);
		repositorio.salvar(i);
	}

	public List<Ingresso> emitir(CompraPendente compra, UsuarioId comprador) {
		Validate.notNull(compra, "compra");
		Validate.notNull(comprador, "comprador");
		List<Ingresso> criados = new ArrayList<>();
		for (int n = 0; n < compra.getQuantidade(); n++) {
			Ingresso ing = new Ingresso(compra.getTipoIngressoId(), compra.getEventoId(), comprador);
			IngressoId novoId = new IngressoId(UUID.randomUUID());
			ing.atribuirId(novoId);
			repositorio.salvar(ing);
			criados.add(ing);
		}
		return criados;
	}
}
