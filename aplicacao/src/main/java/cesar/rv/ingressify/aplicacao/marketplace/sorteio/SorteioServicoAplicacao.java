package cesar.rv.ingressify.aplicacao.marketplace.sorteio;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.ObservadorSorteioContemplado;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.PublicadorSorteio;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.ContextoSorteioProcessamento;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.ExecutarSorteioProcessamento;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.InscricaoSorteioRepositorio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.Sorteio;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioId;
import cesar.rv.ingressify.dominio.marketplace.sorteio.SorteioServico;
import cesar.rv.ingressify.dominio.marketplace.sorteio.StatusInscricao;
import cesar.rv.ingressify.dominio.marketplace.tipoIngresso.TipoIngressoId;
import cesar.rv.ingressify.dominio.padroes.estrategia.EstrategiaSorteio;

public class SorteioServicoAplicacao {

    private final SorteioServico sorteioServico;
    private final InscricaoSorteioRepositorio inscricaoRepositorio;
    private final EstrategiaSorteio estrategiaPadrao;

    public SorteioServicoAplicacao(SorteioServico sorteioServico,
            InscricaoSorteioRepositorio inscricaoRepositorio,
            EstrategiaSorteio estrategiaPadrao) {
        Validate.notNull(sorteioServico, "sorteioServico");
        Validate.notNull(inscricaoRepositorio, "inscricaoRepositorio");
        Validate.notNull(estrategiaPadrao, "estrategiaPadrao");
        this.sorteioServico = sorteioServico;
        this.inscricaoRepositorio = inscricaoRepositorio;
        this.estrategiaPadrao = estrategiaPadrao;
    }

    public SorteioId criar(EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId organizadorId,
            int quantidadeIngressos, int quantidadeListaEspera,
            LocalDateTime prazoInscricao, int prazoConfirmacaoHoras) {
        Sorteio sorteio = new Sorteio(eventoId, tipoIngressoId, organizadorId,
                quantidadeIngressos, quantidadeListaEspera, prazoInscricao, prazoConfirmacaoHoras);
        sorteioServico.salvar(sorteio);
        return sorteio.getId();
    }

    public void abrirInscricoes(SorteioId sorteioId, UsuarioId organizadorId) {
        sorteioServico.abrirInscricoes(sorteioId, organizadorId);
    }

    public void encerrarInscricoes(SorteioId sorteioId, UsuarioId organizadorId) {
        sorteioServico.encerrarInscricoes(sorteioId, organizadorId);
    }

    public InscricaoSorteioId inscrever(SorteioId sorteioId, UsuarioId participanteId) {
        InscricaoSorteio inscricao = sorteioServico.inscrever(sorteioId, participanteId);
        return inscricao.getId();
    }

    public void executarSorteio(SorteioId sorteioId) {
        Sorteio sorteio = sorteioServico.obter(sorteioId);
        List<InscricaoSorteio> inscricoes = sorteioServico.listarInscricoes(sorteioId);

        PublicadorSorteio publicador = new PublicadorSorteio();
        publicador.registrar(new ObservadorSorteioContemplado());

        ExecutarSorteioProcessamento processamento = new ExecutarSorteioProcessamento(
                estrategiaPadrao, inscricaoRepositorio, publicador);

        ContextoSorteioProcessamento ctx = new ContextoSorteioProcessamento(sorteio, inscricoes);
        processamento.executar(ctx);

        sorteioServico.salvar(sorteio);
    }

    public void confirmarParticipacao(SorteioId sorteioId, UsuarioId participanteId) {
        Sorteio sorteio = sorteioServico.obter(sorteioId);

        InscricaoSorteio inscricao = inscricaoRepositorio
                .buscarPorSorteioEParticipante(sorteioId, participanteId)
                .orElseThrow(() -> new IllegalArgumentException("inscrição não encontrada para este participante"));

        if (inscricao.getStatus() == StatusInscricao.LISTA_ESPERA) {
            long confirmados = inscricaoRepositorio.pesquisarPorSorteio(sorteioId).stream()
                    .filter(i -> i.getStatus() == StatusInscricao.CONFIRMADO)
                    .count();
            if (confirmados >= sorteio.getQuantidadeIngressos()) {
                throw new IllegalStateException("todas as vagas já foram preenchidas");
            }
        }

        inscricao.confirmar();
        sorteioServico.salvarInscricao(inscricao);

        long totalConfirmados = inscricaoRepositorio.pesquisarPorSorteio(sorteioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.CONFIRMADO)
                .count();
        boolean todasVagasPreenchidas = totalConfirmados >= sorteio.getQuantidadeIngressos();
        if (todasVagasPreenchidas) {
            sorteioServico.encerrar(sorteioId);
        }
    }

    public void cancelar(SorteioId sorteioId, UsuarioId organizadorId) {
        sorteioServico.cancelar(sorteioId, organizadorId);
    }

    public Sorteio obter(SorteioId sorteioId) {
        return sorteioServico.obter(sorteioId);
    }

    public List<Sorteio> listarPorEvento(EventoId eventoId) {
        return sorteioServico.listarPorEvento(eventoId);
    }

    public List<InscricaoSorteio> listarInscricoes(SorteioId sorteioId) {
        return sorteioServico.listarInscricoes(sorteioId);
    }
}
