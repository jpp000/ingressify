package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.financeiro.pontos.Pontos;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;

@Entity
@Table(name = "pontos")
public class PontosJpa {

	@Id
	@Column(name = "usuario_id")
	private Integer usuarioId;

	@Column(nullable = false)
	private int quantidade;

	protected PontosJpa() {}

	public static PontosJpa fromDomain(Pontos p) {
		var jpa = new PontosJpa();
		jpa.usuarioId = p.getUsuario().getId();
		jpa.quantidade = p.getQuantidade();
		return jpa;
	}

	public Pontos toDomain() {
		return new Pontos(new UsuarioId(usuarioId), quantidade);
	}

	public Integer getUsuarioId() { return usuarioId; }
}
