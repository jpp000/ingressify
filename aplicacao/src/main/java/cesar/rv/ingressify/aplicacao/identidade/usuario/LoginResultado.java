package cesar.rv.ingressify.aplicacao.identidade.usuario;

import java.util.Set;

public record LoginResultado(int id, String nome, String email, Set<String> papeis) {
}
