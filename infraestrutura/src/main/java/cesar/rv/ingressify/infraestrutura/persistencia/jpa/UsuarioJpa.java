package cesar.rv.ingressify.infraestrutura.persistencia.jpa;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.Usuario;

@Entity
@Table(name = "usuarios")
public class UsuarioJpa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	@Column(name = "foto_url")
	private String fotoUrl;

	private String cidade;

	@Column(name = "data_nascimento")
	private LocalDate dataNascimento;

	@Column(name = "bloqueado_revenda", nullable = false)
	private boolean bloqueadoRevenda;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_papeis", joinColumns = @JoinColumn(name = "usuario_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "papel")
	private Set<Papel> papeis;

	protected UsuarioJpa() {}

	public static UsuarioJpa fromDomain(Usuario u) {
		var jpa = new UsuarioJpa();
		if (u.getId() != null) jpa.id = u.getId().getId();
		jpa.nome = u.getNome();
		jpa.email = u.getEmail();
		jpa.senhaHash = u.getSenhaHash();
		jpa.fotoUrl = u.getFotoUrl();
		jpa.cidade = u.getCidade();
		jpa.dataNascimento = u.getDataNascimento();
		jpa.bloqueadoRevenda = u.isBloqueadoRevenda();
		jpa.papeis = Set.copyOf(u.getPapeis());
		return jpa;
	}

	public Usuario toDomain() {
		return new Usuario(
				new UsuarioId(id), nome, email, senhaHash, fotoUrl, cidade,
				dataNascimento, bloqueadoRevenda, papeis.stream().collect(Collectors.toSet()));
	}

	public Integer getId() { return id; }
}
