package cesar.rv.ingressify.dominio.marketplace.ingresso;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

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

	public void cancelar(IngressoId id) {
		Ingresso i = repositorio.obter(id);
		i.cancelar();
		repositorio.salvar(i);
	}
}
