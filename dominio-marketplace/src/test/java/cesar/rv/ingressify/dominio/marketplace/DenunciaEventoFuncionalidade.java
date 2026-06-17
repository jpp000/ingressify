package cesar.rv.ingressify.dominio.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoId;
import cesar.rv.ingressify.dominio.marketplace.denuncia.DenunciaEventoRepositorio;
import cesar.rv.ingressify.dominio.marketplace.denuncia.MotivoDenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.denuncia.StatusDenunciaEvento;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class DenunciaEventoFuncionalidade {

    private static final EventoId EVENTO = new EventoId(1);
    private static final UsuarioId USUARIO = new UsuarioId(10);

    private final DenunciaRepositorioMemoria repositorio = new DenunciaRepositorioMemoria();

    private DenunciaEvento denunciaAtual;
    private Throwable excecao;

    private DenunciaEvento criarDenuncia(EventoId eventoId, UsuarioId userId, StatusDenunciaEvento status) {
        if (status == StatusDenunciaEvento.PENDENTE) {
            DenunciaEvento d = new DenunciaEvento(eventoId, userId, MotivoDenunciaEvento.FRAUDE, null,
                    LocalDateTime.now());
            repositorio.salvar(d);
            return d;
        }
        DenunciaEvento d = new DenunciaEvento(new DenunciaEventoId(repositorio.proximoId++),
                eventoId, userId, MotivoDenunciaEvento.FRAUDE, null, status,
                LocalDateTime.now().minusMinutes(30), LocalDateTime.now().minusMinutes(5));
        repositorio.salvar(d);
        return d;
    }

    @Dado("um evento ativo disponível no sistema")
    public void eventoAtivo() {
        // apenas prepara o estado: nenhuma denúncia ainda
    }

    @Dado("um evento com uma denúncia já registrada pelo usuário")
    public void eventoComDenunciaExistente() {
        denunciaAtual = criarDenuncia(EVENTO, USUARIO, StatusDenunciaEvento.PENDENTE);
    }

    @Dado("uma denúncia com status PENDENTE")
    public void denunciaPendente() {
        denunciaAtual = criarDenuncia(EVENTO, USUARIO, StatusDenunciaEvento.PENDENTE);
    }

    @Dado("uma denúncia com status EM_ANALISE")
    public void denunciaEmAnalise() {
        denunciaAtual = criarDenuncia(EVENTO, USUARIO, StatusDenunciaEvento.EM_ANALISE);
    }

    @Dado("uma denúncia com status APROVADA")
    public void denunciaAprovada() {
        denunciaAtual = criarDenuncia(EVENTO, USUARIO, StatusDenunciaEvento.APROVADA);
    }

    @Quando("um usuário denuncia o evento com motivo FRAUDE")
    public void usuarioDenuncia() {
        try {
            boolean duplicada = repositorio.existePorEventoEDenunciante(EVENTO, USUARIO);
            if (duplicada) throw new IllegalStateException("você já denunciou este evento");
            denunciaAtual = new DenunciaEvento(EVENTO, USUARIO, MotivoDenunciaEvento.FRAUDE,
                    "Possível fraude", LocalDateTime.now());
            repositorio.salvar(denunciaAtual);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o mesmo usuário tenta denunciar o mesmo evento novamente")
    public void usuarioDenunciaNovamente() {
        try {
            boolean duplicada = repositorio.existePorEventoEDenunciante(EVENTO, USUARIO);
            if (duplicada) throw new IllegalStateException("você já denunciou este evento");
            DenunciaEvento nova = new DenunciaEvento(EVENTO, USUARIO, MotivoDenunciaEvento.FRAUDE,
                    "Segunda denúncia", LocalDateTime.now());
            repositorio.salvar(nova);
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Quando("o admin inicia a análise da denúncia")
    public void adminIniciaAnalise() {
        denunciaAtual.iniciarAnalise(LocalDateTime.now());
        repositorio.salvar(denunciaAtual);
    }

    @Quando("o admin aprova a denúncia")
    public void adminAprova() {
        denunciaAtual.aprovar(LocalDateTime.now());
        repositorio.salvar(denunciaAtual);
    }

    @Quando("o admin rejeita a denúncia")
    public void adminRejeita() {
        denunciaAtual.rejeitar(LocalDateTime.now());
        repositorio.salvar(denunciaAtual);
    }

    @Quando("o admin aprova a denúncia diretamente")
    public void adminAprovadiretamente() {
        denunciaAtual.aprovar(LocalDateTime.now());
        repositorio.salvar(denunciaAtual);
    }

    @Quando("o admin tenta iniciar análise da denúncia já decidida")
    public void adminTentaIniciarAnaliseDecidida() {
        try {
            denunciaAtual.iniciarAnalise(LocalDateTime.now());
        } catch (Exception e) {
            excecao = e;
        }
    }

    @Então("a denúncia é registrada com status PENDENTE")
    public void denunciaRegistradaPendente() {
        assertNotNull(denunciaAtual, "denúncia deveria ter sido criada");
        assertEquals(StatusDenunciaEvento.PENDENTE, denunciaAtual.getStatus());
    }

    @Então("a segunda denúncia é rejeitada com erro de duplicidade")
    public void segundaDenunciaRejeitada() {
        assertNotNull(excecao, "era esperada uma exceção mas nenhuma foi lançada");
        assertTrue(excecao.getMessage().contains("denunciou"),
                "mensagem inesperada: " + excecao.getMessage());
    }

    @Então("o status da denúncia é EM_ANALISE")
    public void statusEmAnalise() {
        assertEquals(StatusDenunciaEvento.EM_ANALISE, denunciaAtual.getStatus());
    }

    @Então("o status da denúncia é APROVADA")
    public void statusAprovada() {
        assertEquals(StatusDenunciaEvento.APROVADA, denunciaAtual.getStatus());
    }

    @Então("o status da denúncia é REJEITADA")
    public void statusRejeitada() {
        assertEquals(StatusDenunciaEvento.REJEITADA, denunciaAtual.getStatus());
    }

    @Então("a operação é rejeitada com erro de status inválido")
    public void operacaoRejeitadaStatusInvalido() {
        assertNotNull(excecao, "era esperada uma exceção mas nenhuma foi lançada");
        assertTrue(excecao.getMessage().contains("decidida") || excecao.getMessage().contains("pendente"),
                "mensagem inesperada: " + excecao.getMessage());
    }

    // ── Repositório em memória ─────────────────────────────────────────────────

    static class DenunciaRepositorioMemoria implements DenunciaEventoRepositorio {
        private final Map<DenunciaEventoId, DenunciaEvento> dados = new HashMap<>();
        int proximoId = 1;

        @Override
        public void salvar(DenunciaEvento denuncia) {
            if (denuncia.getId() == null) denuncia.atribuirId(new DenunciaEventoId(proximoId++));
            dados.put(denuncia.getId(), denuncia);
        }

        @Override
        public DenunciaEvento obter(DenunciaEventoId id) {
            DenunciaEvento d = dados.get(id);
            if (d == null) throw new IllegalArgumentException("Denúncia não encontrada: " + id);
            return d;
        }

        @Override
        public List<DenunciaEvento> pesquisarTodas() {
            return new ArrayList<>(dados.values());
        }

        @Override
        public List<DenunciaEvento> pesquisarPorEvento(EventoId eventoId) {
            return dados.values().stream().filter(d -> d.getEventoId().equals(eventoId)).toList();
        }

        @Override
        public boolean existePorEventoEDenunciante(EventoId eventoId, UsuarioId denuncianteId) {
            return dados.values().stream().anyMatch(d ->
                    d.getEventoId().equals(eventoId) && d.getDenuncianteId().equals(denuncianteId));
        }
    }
}
