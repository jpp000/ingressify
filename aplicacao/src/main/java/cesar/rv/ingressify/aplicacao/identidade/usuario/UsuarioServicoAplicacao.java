package cesar.rv.ingressify.aplicacao.identidade.usuario;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.saldo.SaldoRepositorio;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.PasswordHasher;
import cesar.rv.ingressify.dominio.identidade.usuario.Usuario;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioServico;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.ingresso.StatusIngresso;

public class UsuarioServicoAplicacao {

	private final UsuarioServico usuarioServico;
	private final IngressoRepositorio ingressoRepositorio;
	private final SaldoRepositorio saldoRepositorio;

	public UsuarioServicoAplicacao(UsuarioServico usuarioServico, IngressoRepositorio ingressoRepositorio,
			SaldoRepositorio saldoRepositorio) {
		Validate.notNull(usuarioServico, "usuarioServico");
		Validate.notNull(ingressoRepositorio, "ingressoRepositorio");
		Validate.notNull(saldoRepositorio, "saldoRepositorio");
		this.usuarioServico = usuarioServico;
		this.ingressoRepositorio = ingressoRepositorio;
		this.saldoRepositorio = saldoRepositorio;
	}

	public UsuarioId cadastrar(String nome, String email, String senhaPlana) {
		return usuarioServico.cadastrar(nome, email, senhaPlana);
	}

	public LoginResultado login(String email, String senha) {
		Validate.notBlank(email, "email");
		Validate.notBlank(senha, "senha");
		Usuario u;
		try {
			u = usuarioServico.obterPorEmail(email);
		} catch (Exception e) {
			throw new IllegalStateException("credenciais inválidas");
		}
		if (!PasswordHasher.verificar(senha, u.getSenhaHash())) {
			throw new IllegalStateException("credenciais inválidas");
		}
		Set<String> papeis = u.getPapeis().stream().map(Papel::name).collect(Collectors.toSet());
		return new LoginResultado(u.getId().getId(), u.getNome(), u.getEmail(), papeis);
	}

	public LoginResultado cadastrarComPapel(String nome, String email, String senha, String papelStr) {
		Validate.notBlank(nome, "nome");
		Validate.notBlank(email, "email");
		Validate.notBlank(senha, "senha");
		Validate.notBlank(papelStr, "papel");
		Papel papel;
		try {
			papel = Papel.valueOf(papelStr.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Papel inválido: deve ser COMPRADOR ou ORGANIZADOR");
		}
		if (papel != Papel.COMPRADOR && papel != Papel.ORGANIZADOR) {
			throw new IllegalArgumentException("Papel deve ser COMPRADOR ou ORGANIZADOR");
		}
		UsuarioId id = usuarioServico.cadastrar(nome, email, senha);
		if (papel == Papel.ORGANIZADOR) {
			usuarioServico.concederPapel(id, Papel.ORGANIZADOR);
		}
		Usuario u = usuarioServico.obter(id);
		Set<String> papeis = u.getPapeis().stream().map(Papel::name).collect(Collectors.toSet());
		return new LoginResultado(id.getId(), u.getNome(), u.getEmail(), papeis);
	}

	public void atualizarPerfil(UsuarioId usuarioId, String nome, String fotoUrl, String cidade,
			java.time.LocalDate dataNascimento) {
		usuarioServico.atualizarPerfil(usuarioId, nome, fotoUrl, cidade, dataNascimento);
	}

	public void alterarEmail(UsuarioId usuarioId, String novoEmail, String senhaAtual) {
		usuarioServico.alterarEmail(usuarioId, novoEmail, senhaAtual);
	}

	public void alterarSenha(UsuarioId usuarioId, String atual, String nova) {
		usuarioServico.alterarSenha(usuarioId, atual, nova);
	}

	public void excluir(UsuarioId usuarioId, String senhaAtual) {
		var u = usuarioServico.obter(usuarioId);
		if (!PasswordHasher.verificar(senhaAtual, u.getSenhaHash())) {
			throw new IllegalStateException("senha incorreta");
		}
		Dinheiro saldo = saldoRepositorio.obter(usuarioId).getValor();
		if (!Dinheiro.ZERO.equals(saldo)) {
			throw new IllegalStateException("conta com saldo não pode ser excluída");
		}
		boolean temAtivo = ingressoRepositorio.pesquisarPorProprietario(usuarioId).stream()
				.anyMatch(i -> i.getStatus() == StatusIngresso.ATIVO || i.getStatus() == StatusIngresso.EM_REVENDA);
		if (temAtivo) {
			throw new IllegalStateException("conta com ingressos ativos não pode ser excluída");
		}
		u.marcarComoExcluido();
		usuarioServico.salvar(u);
	}

	public UsuarioResumo obterPerfil(UsuarioId usuarioId) {
		return resumoDe(usuarioServico.obter(usuarioId));
	}

	public void bloquearRevenda(UsuarioId usuarioId) {
		usuarioServico.bloquearRevenda(usuarioId);
	}

	public void desbloquearRevenda(UsuarioId usuarioId) {
		usuarioServico.desbloquearRevenda(usuarioId);
	}

	public void concederPapel(UsuarioId usuarioId, Papel papel) {
		usuarioServico.concederPapel(usuarioId, papel);
	}

	public void revogarPapel(UsuarioId usuarioId, Papel papel) {
		usuarioServico.revogarPapel(usuarioId, papel);
	}

	private static UsuarioResumo resumoDe(Usuario u) {
		return new UsuarioResumo() {
			@Override
			public UsuarioId getId() {
				return u.getId();
			}

			@Override
			public String getNome() {
				return u.getNome();
			}

			@Override
			public String getEmail() {
				return u.getEmail();
			}

			@Override
			public String getCidade() {
				return u.getCidade();
			}

			@Override
			public boolean isBloqueadoRevenda() {
				return u.isBloqueadoRevenda();
			}
		};
	}
}
