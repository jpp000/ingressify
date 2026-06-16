package cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.Assento;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.AssentoId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.FilaEspera;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.FilaEsperaRepositorio;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentos;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.StatusAssento;

public class FilaEsperaServicoAplicacao {

    private static final int ASSENTOS_AUTO_RESERVADOS = 1;

    private final FilaEsperaRepositorio filaRepositorio;
    private final MapaAssentosServico mapaServico;

    public FilaEsperaServicoAplicacao(FilaEsperaRepositorio filaRepositorio,
            MapaAssentosServico mapaServico) {
        Validate.notNull(filaRepositorio, "filaRepositorio");
        Validate.notNull(mapaServico, "mapaServico");
        this.filaRepositorio = filaRepositorio;
        this.mapaServico = mapaServico;
    }

    public int entrar(MapaAssentosId mapaId, UsuarioId usuarioId) {
        Validate.notNull(mapaId, "mapaId");
        Validate.notNull(usuarioId, "usuarioId");

        if (filaRepositorio.buscar(mapaId, usuarioId).isPresent()) {
            throw new IllegalStateException("usuário já está na fila de espera deste mapa");
        }

        MapaAssentos mapa = mapaServico.obterPorId(mapaId);
        List<Assento> assentos = mapaServico.listarAssentos(mapaId);
        boolean temDisponivel = assentos.stream()
                .anyMatch(a -> a.getStatus() == StatusAssento.DISPONIVEL);
        if (temDisponivel) {
            throw new IllegalStateException("ainda há assentos disponíveis — selecione diretamente");
        }

        int proxPosicao = filaRepositorio.contarPorMapa(mapaId) + 1;
        FilaEspera entrada = new FilaEspera(mapaId, mapa.getEventoId(), usuarioId, proxPosicao);
        filaRepositorio.salvar(entrada);
        return proxPosicao;
    }

    public void sair(MapaAssentosId mapaId, UsuarioId usuarioId) {
        Validate.notNull(mapaId, "mapaId");
        Validate.notNull(usuarioId, "usuarioId");

        if (filaRepositorio.buscar(mapaId, usuarioId).isEmpty()) {
            throw new IllegalStateException("usuário não está na fila de espera");
        }
        filaRepositorio.remover(mapaId, usuarioId);
    }

    public Optional<Integer> consultarPosicao(MapaAssentosId mapaId, UsuarioId usuarioId) {
        return filaRepositorio.buscar(mapaId, usuarioId)
                .map(FilaEspera::getPosicao);
    }

    public int tamanhoFila(MapaAssentosId mapaId) {
        return filaRepositorio.contarPorMapa(mapaId);
    }

    /**
     * Chamado pelo agendador após liberar reservas expiradas.
     * Reserva automaticamente um assento para o próximo da fila.
     */
    public void promoverProximo(MapaAssentosId mapaId) {
        Optional<FilaEspera> proximo = filaRepositorio.obterProximo(mapaId);
        if (proximo.isEmpty()) return;

        List<Assento> disponiveis = mapaServico.listarAssentos(mapaId).stream()
                .filter(a -> a.getStatus() == StatusAssento.DISPONIVEL)
                .limit(ASSENTOS_AUTO_RESERVADOS)
                .toList();

        if (disponiveis.isEmpty()) return;

        UsuarioId usuarioId = proximo.get().getUsuarioId();
        List<AssentoId> ids = disponiveis.stream().map(Assento::getId).toList();
        mapaServico.reservarAssentos(ids, usuarioId);
        filaRepositorio.remover(mapaId, usuarioId);
    }
}
