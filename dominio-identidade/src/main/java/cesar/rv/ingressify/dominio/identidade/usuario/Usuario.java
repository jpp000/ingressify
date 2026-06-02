package cesar.rv.ingressify.dominio.identidade.usuario;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public class Usuario {

	private UsuarioId id;
	private String nome;
	private String email;
	private String senhaHash;
	private String fotoUrl;
	private String cidade;
	private LocalDate dataNascimento;
	private boolean bloqueadoRevenda;
	private final Set<Papel> papeis = new HashSet<>();

	public Usuario(String nome, String email, String senhaHash) {
		Validate.notBlank(nome, "nome");
		Validate.notBlank(email, "email");
		Validate.notBlank(senhaHash, "senhaHash");
		this.nome = nome;
		this.email = email;
		this.senhaHash = senhaHash;
		this.papeis.add(Papel.COMPRADOR);
	}

	public Usuario(UsuarioId id, String nome, String email, String senhaHash, String fotoUrl, String cidade,
			LocalDate dataNascimento, boolean bloqueadoRevenda, Set<Papel> papeis) {
		Validate.notNull(id, "id");
		Validate.notBlank(nome, "nome");
		Validate.notBlank(email, "email");
		Validate.notBlank(senhaHash, "senhaHash");
		Validate.notNull(papeis, "papeis");
		this.id = id;
		this.nome = nome;
		this.email = email;
		this.senhaHash = senhaHash;
		this.fotoUrl = fotoUrl;
		this.cidade = cidade;
		this.dataNascimento = dataNascimento;
		this.bloqueadoRevenda = bloqueadoRevenda;
		this.papeis.addAll(papeis);
	}

	public void atribuirId(UsuarioId novoId) {
		Validate.notNull(novoId, "novoId");
		this.id = novoId;
	}

	public void atualizarPerfil(String nome, String fotoUrl, String cidade, LocalDate dataNascimento) {
		Validate.notBlank(nome, "nome");
		this.nome = nome;
		this.fotoUrl = fotoUrl;
		this.cidade = cidade;
		this.dataNascimento = dataNascimento;
	}

	public void alterarEmail(String novoEmail, String senhaAtualPlana) {
		Validate.notBlank(novoEmail, "novoEmail");
		Validate.notBlank(senhaAtualPlana, "senhaAtualPlana");
		if (!PasswordHasher.verificar(senhaAtualPlana, senhaHash)) {
			throw new IllegalStateException("senha atual incorreta");
		}
		this.email = novoEmail;
	}

	public void alterarSenha(String senhaAtualPlana, String novaSenhaPlana) {
		Validate.notBlank(senhaAtualPlana, "senhaAtualPlana");
		Validate.notBlank(novaSenhaPlana, "novaSenhaPlana");
		if (!PasswordHasher.verificar(senhaAtualPlana, senhaHash)) {
			throw new IllegalStateException("senha atual incorreta");
		}
		PoliticaSenha.validar(novaSenhaPlana);
		this.senhaHash = PasswordHasher.hash(novaSenhaPlana);
	}

	public void bloquearRevenda() {
		this.bloqueadoRevenda = true;
	}

	public void desbloquearRevenda() {
		this.bloqueadoRevenda = false;
	}

	public void concederPapel(Papel p) {
		Validate.notNull(p, "p");
		this.papeis.add(p);
	}

	public void revogarPapel(Papel p) {
		Validate.notNull(p, "p");
		if (p == Papel.COMPRADOR) {
			throw new IllegalStateException("não é possível revogar papel COMPRADOR");
		}
		this.papeis.remove(p);
	}

	public void marcarComoExcluido() {
		this.nome = "[excluído]";
		this.email = "excluido@anonimo.local";
		this.fotoUrl = null;
	}

	public boolean temPapel(Papel p) {
		return papeis.contains(p);
	}

	public UsuarioId getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public String getFotoUrl() {
		return fotoUrl;
	}

	public String getCidade() {
		return cidade;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public boolean isBloqueadoRevenda() {
		return bloqueadoRevenda;
	}

	public Set<Papel> getPapeis() {
		return Collections.unmodifiableSet(papeis);
	}
}
