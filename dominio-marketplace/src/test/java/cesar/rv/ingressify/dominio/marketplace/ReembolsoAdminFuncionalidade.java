package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.MotivoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.reembolso.StatusSolicitacaoReembolso;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class ReembolsoAdminFuncionalidade {

    private static final UsuarioId USUARIO = new UsuarioId(5);
    private static final IngressoId INGRESSO = new IngressoId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private final ReembolsoRepositorioMemoria repositorio = new ReembolsoRepositorioMemoria();

    private SolicitacaoReembolso solicitacao;
    private Throwable excecao;

    private SolicitacaoReembolso criarSolicitacaoCom(StatusSolicitacaoReembolso status) {
        if (status == StatusSolicitacaoReembolso.PENDENTE) {
            SolicitacaoReembolso s = new SolicitacaoReembolso(INGRESSO, USUARIO, MotivoReembolso.VOLUNTARIO,
                    new Dinheiro(BigDecimal.valueOf(150)), LocalDateTime.now());
            repositorio.salvar(s);
            return s;
        }
        SolicitacaoReembolso s = new SolicitacaoReembolso(
                new SolicitacaoReembolsoId(999),
                INGRESSO, USUARIO, MotivoReembolso.VOLUNTARIO,
                status, new Dinheiro(BigDecimal.valueOf(150)),
                LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now().minusMinutes(5));
        repositorio.salvar(s);
        return s;
    }

    @Dado("uma solicitação de reembolso voluntário criada pelo usuário")
    public void solicitacaoCriada() {
        solicitacao = criarSolicitacaoCom(StatusSolicitacaoReembolso.PENDENTE);
    }

    @Dado("uma solicitação de reembolso com status PENDENTE")
    public void solicitacaoPendente() {
        solicitacao = criarSolicitacaoCom(StatusSolicitacaoReembolso.PENDENTE);
    }

    @Dado("uma solicitação de reembolso com status EM_ANALISE")
    public void solicitacaoEmAnalise() {
        solicitacao = criarSolicitacaoCom(StatusSolicitacaoReembolso.EM_ANALISE);
    }

    @Dado("uma solicitação de reembolso com status APROVADA")
    public void solicitacaoAprovada() {
        solicitacao = criarSolicitacaoCom(StatusSolicitacaoReembolso.APROVADA);
    }

    @Quando("o admin inicia a análise da solicitação")
    public void adminIniciaAnalise() {
        solicitacao.iniciarAnalise(LocalDateTime.now());
        repositorio.salvar(solicitacao);
    }

    @Quando("o admin aprova a solicitação")
    public void adminAprova() {
        solicitacao.aprovar(LocalDateTime.now());
        repositorio.salvar(solicitacao);
    }

    @Quando("o admin recusa a solicitação")
    public void adminRecusa() {
        solicitacao.recusar(LocalDateTime.now());
        repositorio.salvar(solicitacao);
    }

    @Quando("o admin aprova diretamente a solicitação pendente")
    public void adminAprovadiretamente() {
        solicitacao.aprovar(LocalDateTime.now());
        repositorio.salvar(solicitacao);
    }

    @Quando("o admin recusa diretamente a solicitação pendente")
    public void adminRecusaDiretamente() {
        solicitacao.recusar(LocalDateTime.now());
        repositorio.salvar(solicitacao);
    }

    @Quando("o usuário cancela a própria solicitação")
    public void usuarioCancela() {
        solicitacao.cancelar(LocalDateTime.now());
        repositorio.salvar(solicitacao);
    }

    @Quando("o admin tenta recusar a solicitação já aprovada")
    public void adminTentaRecusarAprovada() {
        try {
            solicitacao.recusar(LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("o status da solicitação é PENDENTE")
    public void statusPendente() {
        assertEquals(StatusSolicitacaoReembolso.PENDENTE, solicitacao.getStatus());
    }

    @Então("o status da solicitação é EM_ANALISE")
    public void statusEmAnalise() {
        assertEquals(StatusSolicitacaoReembolso.EM_ANALISE, solicitacao.getStatus());
    }

    @Então("o status da solicitação é APROVADA")
    public void statusAprovada() {
        assertEquals(StatusSolicitacaoReembolso.APROVADA, solicitacao.getStatus());
    }

    @E("a data de decisão é preenchida")
    public void dataDecisaoPreenchida() {
        assertNotNull(solicitacao.getDecididaEm(), "decididaEm deveria estar preenchido");
    }

    @Então("o status da solicitação é RECUSADA")
    public void statusRecusada() {
        assertEquals(StatusSolicitacaoReembolso.RECUSADA, solicitacao.getStatus());
    }

    @Então("o status da solicitação é CANCELADA")
    public void statusCancelada() {
        assertEquals(StatusSolicitacaoReembolso.CANCELADA, solicitacao.getStatus());
    }

    @Então("a operação é rejeitada com erro de status")
    public void operacaoRejeitadaStatus() {
        assertNotNull(excecao, "era esperada uma exceção mas nenhuma foi lançada");
    }

    // ── Repositório em memória ─────────────────────────────────────────────────

    static class ReembolsoRepositorioMemoria implements SolicitacaoReembolsoRepositorio {
        private final Map<SolicitacaoReembolsoId, SolicitacaoReembolso> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(SolicitacaoReembolso s) {
            if (s.getId() == null) s.atribuirId(new SolicitacaoReembolsoId(proximoId++));
            dados.put(s.getId(), s);
        }

        @Override
        public SolicitacaoReembolso obter(SolicitacaoReembolsoId id) {
            SolicitacaoReembolso s = dados.get(id);
            if (s == null) throw new IllegalArgumentException("Solicitação não encontrada: " + id);
            return s;
        }

        @Override
        public Optional<SolicitacaoReembolso> pesquisarAtivaPorIngresso(IngressoId ingressoId) {
            return dados.values().stream()
                    .filter(s -> s.getIngressoId().equals(ingressoId)
                            && (s.getStatus() == StatusSolicitacaoReembolso.PENDENTE
                                    || s.getStatus() == StatusSolicitacaoReembolso.EM_ANALISE))
                    .findFirst();
        }

        @Override
        public List<SolicitacaoReembolso> pesquisarPorSolicitante(UsuarioId solicitanteId) {
            return dados.values().stream()
                    .filter(s -> s.getSolicitanteId().equals(solicitanteId)).toList();
        }

        @Override
        public List<SolicitacaoReembolso> listarTodas() {
            return new ArrayList<>(dados.values());
        }
    }
}
