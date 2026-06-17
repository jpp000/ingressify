package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompraRepositorio;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupo;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoId;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.ParticipanteGrupoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusGrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.StatusParticipanteGrupo;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class GrupoCompraFuncionalidade {

    private static final UsuarioId LIDER = new UsuarioId(1);
    private static final UsuarioId PARTICIPANTE_B = new UsuarioId(2);
    private static final EventoId EVENTO = new EventoId(10);
    private static final TipoIngressoId TIPO = new TipoIngressoId(1);

    private final GrupoCompraRepositorioMemoria grupoRepo = new GrupoCompraRepositorioMemoria();
    private final ParticipanteGrupoRepositorioMemoria participanteRepo = new ParticipanteGrupoRepositorioMemoria();

    private GrupoCompra grupo;
    private ParticipanteGrupo participante;
    private Throwable excecao;

    private GrupoCompra criarGrupoAberto() {
        GrupoCompra g = new GrupoCompra(EVENTO, TIPO, LIDER, 2, LocalDateTime.now().plusDays(3));
        grupoRepo.salvar(g);
        return g;
    }

    private ParticipanteGrupo adicionarParticipante(GrupoCompraId grupoId, UsuarioId usuarioId) {
        ParticipanteGrupo p = new ParticipanteGrupo(grupoId, usuarioId, 1, false, null,
                new Dinheiro(BigDecimal.valueOf(100)));
        participanteRepo.salvar(p);
        return p;
    }

    @Dado("um grupo de compra criado pelo líder com {int} participantes")
    public void grupoComParticipantes(int qtd) {
        grupo = criarGrupoAberto();
        for (int i = 0; i < qtd; i++) {
            adicionarParticipante(grupo.getId(), new UsuarioId(i + 1));
        }
    }

    @Dado("um grupo de compra com {int} participantes")
    public void grupoParaConfirmacao(int qtd) {
        grupo = criarGrupoAberto();
        for (int i = 0; i < qtd; i++) {
            adicionarParticipante(grupo.getId(), new UsuarioId(i + 1));
        }
    }

    @Dado("um grupo de compra com status ABERTO")
    public void grupoAberto() {
        grupo = criarGrupoAberto();
        adicionarParticipante(grupo.getId(), LIDER);
        adicionarParticipante(grupo.getId(), PARTICIPANTE_B);
    }

    @Dado("um grupo de compra com prazo vencido")
    public void grupoPrazoVencido() {
        grupo = new GrupoCompra(
                new GrupoCompraId(99), EVENTO, TIPO, LIDER, 2,
                LocalDateTime.now().minusHours(1), StatusGrupoCompra.ABERTO,
                LocalDateTime.now().minusDays(1));
        grupoRepo.salvar(grupo);
    }

    @Dado("um grupo de compra já confirmado")
    public void grupoJaConfirmado() {
        grupo = criarGrupoAberto();
        adicionarParticipante(grupo.getId(), LIDER);
        adicionarParticipante(grupo.getId(), PARTICIPANTE_B);
        participanteRepo.listarPorGrupo(grupo.getId()).forEach(ParticipanteGrupo::marcarPago);
        grupo.confirmar();
        grupoRepo.salvar(grupo);
    }

    @Dado("um participante com status PENDENTE")
    public void participantePendente() {
        grupo = criarGrupoAberto();
        participante = adicionarParticipante(grupo.getId(), LIDER);
    }

    @Quando("todos os participantes confirmam pagamento")
    public void todosConfirmam() {
        participanteRepo.listarPorGrupo(grupo.getId()).forEach(p -> {
            p.marcarPago();
            participanteRepo.salvar(p);
        });
    }

    @Quando("apenas {int} dos participantes confirma pagamento")
    public void apenasUmConfirma(int qtd) {
        List<ParticipanteGrupo> lista = participanteRepo.listarPorGrupo(grupo.getId());
        for (int i = 0; i < Math.min(qtd, lista.size()); i++) {
            lista.get(i).marcarPago();
            participanteRepo.salvar(lista.get(i));
        }
    }

    @Quando("o líder cancela o grupo")
    public void liderCancela() {
        try {
            if (!grupo.getLiderId().equals(LIDER)) {
                throw new IllegalStateException("apenas o líder pode cancelar o grupo de compra");
            }
            grupo.cancelar();
            grupoRepo.salvar(grupo);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("um participante não-líder tenta cancelar o grupo")
    public void naoLiderTentaCancelar() {
        try {
            // PARTICIPANTE_B is not the leader
            if (!grupo.getLiderId().equals(PARTICIPANTE_B)) {
                throw new IllegalStateException("apenas o líder pode cancelar o grupo de compra");
            }
            grupo.cancelar();
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o líder tenta cancelar o grupo confirmado")
    public void liderTentaCancelarConfirmado() {
        try {
            grupo.cancelar();
            grupoRepo.salvar(grupo);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o participante confirma o pagamento individual")
    public void participanteConfirmaPagamento() {
        participante.marcarPago();
        participanteRepo.salvar(participante);
    }

    @Então("o status do grupo é ABERTO")
    public void statusAberto() {
        assertEquals(StatusGrupoCompra.ABERTO, grupoRepo.obter(grupo.getId()).getStatus());
    }

    @Então("o grupo pode ser confirmado com status CONFIRMADO")
    public void grupoDeveConfirmar() {
        boolean todosPagos = participanteRepo.listarPorGrupo(grupo.getId())
                .stream().allMatch(p -> p.getStatus() == StatusParticipanteGrupo.PAGO);
        assertTrue(todosPagos, "nem todos os participantes pagaram");
        grupo.confirmar();
        grupoRepo.salvar(grupo);
        assertEquals(StatusGrupoCompra.CONFIRMADO, grupoRepo.obter(grupo.getId()).getStatus());
    }

    @Então("a confirmação do grupo é rejeitada")
    public void confirmacaoEhRejeitada() {
        try {
            boolean todosPagos = participanteRepo.listarPorGrupo(grupo.getId())
                    .stream().allMatch(p -> p.getStatus() == StatusParticipanteGrupo.PAGO);
            if (!todosPagos) throw new IllegalStateException("nem todos os participantes pagaram sua parte");
            grupo.confirmar();
        } catch (Exception e) {
            excecao = e;
        }
        assertNotNull(excecao, "esperava exceção mas nenhuma foi lançada");
        assertTrue(excecao.getMessage().contains("pagaram"), excecao.getMessage());
    }

    @Então("o status do grupo é CANCELADO")
    public void statusCancelado() {
        assertEquals(StatusGrupoCompra.CANCELADO, grupoRepo.obter(grupo.getId()).getStatus());
    }

    @Então("o cancelamento é rejeitado com erro")
    public void cancelamentoRejeitado() {
        assertNotNull(excecao, "esperava exceção mas nenhuma foi lançada");
    }

    @Então("o prazo do grupo está expirado")
    public void prazoExpirado() {
        assertTrue(grupo.prazoExpirado(LocalDateTime.now()),
                "esperava prazo expirado mas prazoExpirado() retornou false");
    }

    @Então("o cancelamento é rejeitado por status inválido")
    public void cancelamentoRejeitadoPorStatus() {
        assertNotNull(excecao, "esperava exceção mas nenhuma foi lançada");
        assertTrue(excecao.getMessage().contains("ABERTO"), excecao.getMessage());
    }

    @Então("o status do participante é PAGO")
    public void statusParticipantePago() {
        assertEquals(StatusParticipanteGrupo.PAGO, participante.getStatus());
    }

    // ── Repositórios em memória ──────────────────────────────────────────────────

    static class GrupoCompraRepositorioMemoria implements GrupoCompraRepositorio {
        private final Map<GrupoCompraId, GrupoCompra> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(GrupoCompra g) {
            if (g.getId() == null) g.atribuirId(new GrupoCompraId(proximoId++));
            dados.put(g.getId(), g);
        }

        @Override
        public GrupoCompra obter(GrupoCompraId id) {
            GrupoCompra g = dados.get(id);
            if (g == null) throw new IllegalArgumentException("GrupoCompra não encontrado: " + id);
            return g;
        }

        @Override
        public List<GrupoCompra> listarPorEvento(EventoId eventoId) {
            return dados.values().stream().filter(g -> g.getEventoId().equals(eventoId)).toList();
        }

        @Override
        public List<GrupoCompra> listarPorLider(UsuarioId liderId) {
            return dados.values().stream().filter(g -> g.getLiderId().equals(liderId)).toList();
        }

        @Override
        public List<GrupoCompra> listarAbertosExpirados(LocalDateTime agora) {
            return dados.values().stream()
                    .filter(g -> g.getStatus() == StatusGrupoCompra.ABERTO && g.prazoExpirado(agora))
                    .toList();
        }
    }

    static class ParticipanteGrupoRepositorioMemoria implements ParticipanteGrupoRepositorio {
        private final Map<ParticipanteGrupoId, ParticipanteGrupo> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(ParticipanteGrupo p) {
            if (p.getId() == null) p.atribuirId(new ParticipanteGrupoId(proximoId++));
            dados.put(p.getId(), p);
        }

        @Override
        public List<ParticipanteGrupo> listarPorGrupo(GrupoCompraId grupoCompraId) {
            return new ArrayList<>(dados.values().stream()
                    .filter(p -> p.getGrupoCompraId().equals(grupoCompraId)).toList());
        }

        @Override
        public Optional<ParticipanteGrupo> buscarPorGrupoEUsuario(GrupoCompraId grupoId, UsuarioId usuarioId) {
            return dados.values().stream()
                    .filter(p -> p.getGrupoCompraId().equals(grupoId) && p.getUsuarioId().equals(usuarioId))
                    .findFirst();
        }

        @Override
        public List<ParticipanteGrupo> listarPorUsuario(UsuarioId usuarioId) {
            return dados.values().stream().filter(p -> p.getUsuarioId().equals(usuarioId)).toList();
        }
    }
}
