package cesar.rv.ingressify.dominio.marketplace.mapaAssentos;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;

/**
 * Padrão Proxy: controla o acesso ao processo de reserva de assentos.
 * Verifica disponibilidade e a regra anti-ilha antes de delegar ao domínio.
 */
public class AssentoReservaProxy {

    private static final int MINUTOS_RESERVA = 5;
    private static final int MAX_ASSENTOS_POR_USUARIO = 6;

    private final MapaAssentosRepositorio repositorio;

    public AssentoReservaProxy(MapaAssentosRepositorio repositorio) {
        Validate.notNull(repositorio, "repositorio");
        this.repositorio = repositorio;
    }

    public void reservar(List<AssentoId> assentoIds, UsuarioId usuarioId) {
        Validate.notEmpty(assentoIds, "assentoIds");
        Validate.notNull(usuarioId, "usuarioId");

        List<Assento> assentos = assentoIds.stream()
                .map(repositorio::obterAssento)
                .toList();

        // Pré-condição 1: todos os assentos devem estar disponíveis
        for (Assento a : assentos) {
            if (a.getStatus() != StatusAssento.DISPONIVEL) {
                throw new IllegalStateException(
                        "assento " + a.getCodigo() + " não está disponível para reserva");
            }
        }

        List<Assento> todosDoEvento = repositorio.listarAssentosPorEvento(
                assentos.get(0).getEventoId());

        // Pré-condição 2: limite de assentos por usuário por evento
        verificarLimiteUsuario(assentos, todosDoEvento, usuarioId);

        // Pré-condição 3: anti-ilha — garante que nenhum assento disponível ficará
        // isolado (ambos os vizinhos ocupados/reservados) após esta seleção
        verificarAntiIlha(assentos, todosDoEvento);

        // Delega ao domínio real
        for (Assento a : assentos) {
            a.reservar(usuarioId, MINUTOS_RESERVA);
            repositorio.salvarAssento(a);
        }
    }

    private void verificarLimiteUsuario(List<Assento> selecionados, List<Assento> todosDoEvento,
            UsuarioId usuarioId) {
        long jaAtivos = todosDoEvento.stream()
                .filter(a -> usuarioId.equals(a.getReservadoPor()))
                .filter(a -> a.getStatus() == StatusAssento.VENDIDO
                        || (a.getStatus() == StatusAssento.RESERVADO && !a.reservaExpirada()))
                .count();
        if (jaAtivos + selecionados.size() > MAX_ASSENTOS_POR_USUARIO) {
            throw new IllegalStateException(
                    "limite de " + MAX_ASSENTOS_POR_USUARIO + " assentos por usuário por evento excedido"
                    + " (você já tem " + jaAtivos + ")");
        }
    }

    /**
     * Regra anti-ilha: rejeita seleção que deixaria algum assento disponível
     * completamente isolado entre dois assentos ocupados/reservados na mesma fileira.
     * Assentos nas extremidades não são considerados isolados (só têm um vizinho).
     */
    private void verificarAntiIlha(List<Assento> selecionados, List<Assento> todosDoEvento) {
        Set<String> codigosSelecionados = selecionados.stream()
                .map(Assento::getCodigo)
                .collect(Collectors.toSet());

        // Conjunto de assentos que estarão "bloqueados" após a seleção
        Set<String> bloqueadosApos = todosDoEvento.stream()
                .filter(a -> a.getStatus() != StatusAssento.DISPONIVEL
                        || codigosSelecionados.contains(a.getCodigo()))
                .map(Assento::getCodigo)
                .collect(Collectors.toSet());

        // Verifica se algum assento disponível remanescente ficaria isolado
        for (Assento candidato : todosDoEvento) {
            if (codigosSelecionados.contains(candidato.getCodigo())) continue;
            if (candidato.getStatus() != StatusAssento.DISPONIVEL) continue;

            String vizinhoDireito = incrementarCodigo(candidato.getCodigo());
            String vizinhoEsquerdo = decrementarCodigo(candidato.getCodigo());

            // Assento de extremidade (sem vizinho em um dos lados) nunca fica isolado
            if (vizinhoEsquerdo.isEmpty() || vizinhoDireito.isEmpty()) continue;

            boolean esquerdoBloqueado = bloqueadosApos.contains(vizinhoEsquerdo);
            boolean direitoBloqueado = bloqueadosApos.contains(vizinhoDireito);

            if (esquerdoBloqueado && direitoBloqueado) {
                throw new IllegalStateException(
                        "a seleção isolaria o assento " + candidato.getCodigo());
            }
        }
    }

    private String incrementarCodigo(String codigo) {
        if (codigo == null || codigo.isEmpty()) return "";
        char letra = codigo.charAt(0);
        String numero = codigo.substring(1);
        try {
            return letra + String.valueOf(Integer.parseInt(numero) + 1);
        } catch (NumberFormatException e) {
            return codigo;
        }
    }

    private String decrementarCodigo(String codigo) {
        if (codigo == null || codigo.isEmpty()) return "";
        char letra = codigo.charAt(0);
        String numero = codigo.substring(1);
        try {
            int n = Integer.parseInt(numero);
            return n > 1 ? letra + String.valueOf(n - 1) : "";
        } catch (NumberFormatException e) {
            return codigo;
        }
    }
}
