package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.util.List;
import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

public interface FilaEsperaRepositorio {

    void salvar(FilaEspera entrada);

    void remover(MapaAssentosId mapaId, UsuarioId usuarioId);

    Optional<FilaEspera> buscar(MapaAssentosId mapaId, UsuarioId usuarioId);

    List<FilaEspera> listarPorMapa(MapaAssentosId mapaId);

    Optional<FilaEspera> obterProximo(MapaAssentosId mapaId);

    int contarPorMapa(MapaAssentosId mapaId);
}
