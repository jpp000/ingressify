package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioServico;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusInscricao;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusSorteio;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class SorteioFuncionalidade {

    private static final UsuarioId ORGANIZADOR = new UsuarioId(1);
    private static final UsuarioId COMPRADOR_A = new UsuarioId(10);
    private static final UsuarioId COMPRADOR_B = new UsuarioId(11);
    private static final UsuarioId COMPRADOR_C = new UsuarioId(12);
    private static final EventoId EVENTO = new EventoId(100);
    private static final TipoIngressoId TIPO = new TipoIngressoId(1);

    private final SorteioRepositorioMemoria sorteioRepo = new SorteioRepositorioMemoria();
    private final InscricaoSorteioRepositorioMemoria inscricaoRepo = new InscricaoSorteioRepositorioMemoria();
    private final SorteioServico servico = new SorteioServico(sorteioRepo, inscricaoRepo);

    private SorteioId sorteioId;
    private Throwable excecao;

    @Dado("um sorteio configurado para o evento")
    public void sorteioConfigurado() {
        Sorteio sorteio = new Sorteio(EVENTO, TIPO, ORGANIZADOR, 1, 0,
                LocalDateTime.now().plusDays(7), 48);
        servico.salvar(sorteio);
        sorteioId = sorteio.getId();
    }

    @Quando("o organizador abre as inscrições")
    public void abrirInscricoes() {
        servico.abrirInscricoes(sorteioId, ORGANIZADOR);
    }

    @Então("o status do sorteio é INSCRICOES_ABERTAS")
    public void statusInscricoesAbertas() {
        assertEquals(StatusSorteio.INSCRICOES_ABERTAS, servico.obter(sorteioId).getStatus());
    }

    @Dado("um sorteio com inscrições abertas")
    public void sorteioComInscricoesAbertas() {
        sorteioConfigurado();
        abrirInscricoes();
    }

    @Quando("um comprador se inscreve no sorteio")
    public void compradorSeInscreve() {
        servico.inscrever(sorteioId, COMPRADOR_A);
    }

    @Então("a inscrição é registrada com status INSCRITO")
    public void inscricaoRegistrada() {
        List<InscricaoSorteio> inscricoes = servico.listarInscricoes(sorteioId);
        assertEquals(1, inscricoes.size());
        assertEquals(StatusInscricao.INSCRITO, inscricoes.get(0).getStatus());
    }

    @Dado("um comprador já inscrito no sorteio")
    public void compradorJaInscrito() {
        servico.inscrever(sorteioId, COMPRADOR_A);
    }

    @Quando("o mesmo comprador tenta se inscrever novamente")
    public void mesmCompradorInscreveNovamente() {
        try {
            servico.inscrever(sorteioId, COMPRADOR_A);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a segunda inscrição é rejeitada")
    public void segundaInscricaoRejeitada() {
        assertNotNull(excecao);
    }

    @Quando("um comprador tenta se inscrever antes das inscrições abrirem")
    public void compradorTentaInscreverAntes() {
        try {
            servico.inscrever(sorteioId, COMPRADOR_A);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a inscrição é rejeitada por status inválido")
    public void inscricaoRejeitadaStatusInvalido() {
        assertNotNull(excecao);
    }

    @Quando("o organizador encerra as inscrições")
    public void organizadorEncerraInscricoes() {
        servico.encerrarInscricoes(sorteioId, ORGANIZADOR);
    }

    @Então("o status do sorteio é AGUARDANDO_SORTEIO")
    public void statusAguardandoSorteio() {
        assertEquals(StatusSorteio.AGUARDANDO_SORTEIO, servico.obter(sorteioId).getStatus());
    }

    @Dado("um sorteio aguardando execução com 3 inscrições e 1 vaga")
    public void sorteioAguardandoComInscricoes() {
        Sorteio sorteio = new Sorteio(EVENTO, TIPO, ORGANIZADOR, 1, 1,
                LocalDateTime.now().plusDays(7), 48);
        servico.salvar(sorteio);
        sorteioId = sorteio.getId();
        servico.abrirInscricoes(sorteioId, ORGANIZADOR);
        servico.inscrever(sorteioId, COMPRADOR_A);
        servico.inscrever(sorteioId, COMPRADOR_B);
        servico.inscrever(sorteioId, COMPRADOR_C);
        servico.encerrarInscricoes(sorteioId, ORGANIZADOR);
    }

    @Quando("o sorteio é executado")
    public void sorteioExecutado() {
        Sorteio sorteio = servico.obter(sorteioId);
        List<InscricaoSorteio> elegiveis = servico.listarInscricoes(sorteioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.INSCRITO).toList();
        int nContemplados = Math.min(sorteio.getQuantidadeIngressos(), elegiveis.size());
        for (int i = 0; i < nContemplados; i++) {
            elegiveis.get(i).contemplar(i + 1);
            servico.salvarInscricao(elegiveis.get(i));
        }
        int nEspera = sorteio.getQuantidadeListaEspera();
        for (int i = nContemplados; i < Math.min(nContemplados + nEspera, elegiveis.size()); i++) {
            elegiveis.get(i).marcarListaEspera(i - nContemplados + 1);
            servico.salvarInscricao(elegiveis.get(i));
        }
        sorteio.executarSorteio();
        servico.salvar(sorteio);
    }

    @Então("o status do sorteio é SORTEADO")
    public void statusSorteado() {
        assertEquals(StatusSorteio.SORTEADO, servico.obter(sorteioId).getStatus());
    }

    @Então("exatamente 1 inscrição está com status CONTEMPLADO")
    public void umInscritoContemplado() {
        long count = servico.listarInscricoes(sorteioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.CONTEMPLADO).count();
        assertEquals(1, count);
    }

    @Dado("um sorteio com 1 vaga sorteado e o contemplado definido")
    public void sorteioSorteadoComContemplado() {
        sorteioAguardandoComInscricoes();
        sorteioExecutado();
    }

    @Quando("o contemplado confirma a participação")
    public void contemladoConfirma() {
        InscricaoSorteio contemplado = servico.listarInscricoes(sorteioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.CONTEMPLADO)
                .findFirst().orElseThrow();
        contemplado.confirmar();
        servico.salvarInscricao(contemplado);
        long confirmados = servico.listarInscricoes(sorteioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.CONFIRMADO).count();
        Sorteio sorteio = servico.obter(sorteioId);
        if (confirmados >= sorteio.getQuantidadeIngressos()) {
            servico.encerrar(sorteioId);
        }
    }

    @Então("a inscrição do contemplado é CONFIRMADO")
    public void inscricaoConfirmada() {
        long count = servico.listarInscricoes(sorteioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.CONFIRMADO).count();
        assertEquals(1, count);
    }

    @Então("o status do sorteio é ENCERRADO")
    public void statusEncerrado() {
        assertEquals(StatusSorteio.ENCERRADO, servico.obter(sorteioId).getStatus());
    }

    @Quando("o organizador cancela o sorteio")
    public void organizadorCancela() {
        servico.cancelar(sorteioId, ORGANIZADOR);
    }

    @Então("o status do sorteio é CANCELADO")
    public void statusCancelado() {
        assertEquals(StatusSorteio.CANCELADO, servico.obter(sorteioId).getStatus());
    }

    @Então("todas as inscrições são CANCELADO")
    public void todasInscricoesCanceladas() {
        servico.listarInscricoes(sorteioId).forEach(i ->
                assertEquals(StatusInscricao.CANCELADO, i.getStatus()));
    }

    @Dado("um sorteio encerrado")
    public void sorteioEncerrado() {
        sorteioAguardandoComInscricoes();
        sorteioExecutado();
        contemladoConfirma();
    }

    @Quando("o organizador tenta cancelar o sorteio encerrado")
    public void tentarCancelarEncerrado() {
        try {
            servico.cancelar(sorteioId, ORGANIZADOR);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("o cancelamento é rejeitado")
    public void cancelamentoRejeitado() {
        assertNotNull(excecao);
    }

    // ── Repositórios em memória (apenas para testes) ─────────────────────────

    static class SorteioRepositorioMemoria implements SorteioRepositorio {
        private final Map<SorteioId, Sorteio> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(Sorteio s) {
            if (s.getId() == null) s.atribuirId(new SorteioId(proximoId++));
            dados.put(s.getId(), s);
        }

        @Override
        public Sorteio obter(SorteioId id) {
            Sorteio s = dados.get(id);
            if (s == null) throw new IllegalArgumentException("Sorteio não encontrado: " + id);
            return s;
        }

        @Override
        public List<Sorteio> pesquisarPorEvento(EventoId eventoId) {
            return dados.values().stream()
                    .filter(s -> s.getEventoId().equals(eventoId)).toList();
        }
    }

    static class InscricaoSorteioRepositorioMemoria implements InscricaoSorteioRepositorio {
        private final Map<InscricaoSorteioId, InscricaoSorteio> dados = new HashMap<>();
        private int proximoId = 1;

        @Override
        public void salvar(InscricaoSorteio i) {
            if (i.getId() == null) i.atribuirId(new InscricaoSorteioId(proximoId++));
            dados.put(i.getId(), i);
        }

        @Override
        public InscricaoSorteio obter(InscricaoSorteioId id) {
            InscricaoSorteio i = dados.get(id);
            if (i == null) throw new IllegalArgumentException("Inscrição não encontrada: " + id);
            return i;
        }

        @Override
        public List<InscricaoSorteio> pesquisarPorSorteio(SorteioId sorteioId) {
            return dados.values().stream()
                    .filter(i -> i.getSorteioId().equals(sorteioId)).toList();
        }

        @Override
        public Optional<InscricaoSorteio> buscarPorSorteioEParticipante(SorteioId sorteioId,
                UsuarioId participanteId) {
            return dados.values().stream()
                    .filter(i -> i.getSorteioId().equals(sorteioId)
                            && i.getParticipanteId().equals(participanteId))
                    .findFirst();
        }

        @Override
        public boolean existeInscricaoAtiva(SorteioId sorteioId, UsuarioId participanteId) {
            return dados.values().stream().anyMatch(i ->
                    i.getSorteioId().equals(sorteioId)
                    && i.getParticipanteId().equals(participanteId)
                    && i.getStatus() != StatusInscricao.CANCELADO
                    && i.getStatus() != StatusInscricao.EXPIRADO);
        }
    }
}
