package cesar.rv.ingressify.aplicacao.marketplace.sorteio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.ObservadorSorteioContemplado;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.observador.PublicadorSorteio;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.ContextoSorteioProcessamento;
import cesar.rv.ingressify.aplicacao.marketplace.padroes.template.ExecutarSorteioProcessamento;
import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.financeiro.transacao.Transacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TipoTransacao;
import cesar.rv.ingressify.dominio.financeiro.transacao.TransacaoServico;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.identidade.usuario.Papel;
import cesar.rv.ingressify.dominio.identidade.usuario.UsuarioServico;
import cesar.rv.ingressify.dominio.marketplace.evento.EventoId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.Ingresso;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoServico;
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
    private final IngressoServico ingressoServico;
    private final TransacaoServico transacaoServico;
    private final UsuarioServico usuarioServico;

    public SorteioServicoAplicacao(SorteioServico sorteioServico,
            InscricaoSorteioRepositorio inscricaoRepositorio,
            EstrategiaSorteio estrategiaPadrao,
            IngressoServico ingressoServico,
            TransacaoServico transacaoServico,
            UsuarioServico usuarioServico) {
        Validate.notNull(sorteioServico, "sorteioServico");
        Validate.notNull(inscricaoRepositorio, "inscricaoRepositorio");
        Validate.notNull(estrategiaPadrao, "estrategiaPadrao");
        Validate.notNull(ingressoServico, "ingressoServico");
        Validate.notNull(transacaoServico, "transacaoServico");
        Validate.notNull(usuarioServico, "usuarioServico");
        this.sorteioServico = sorteioServico;
        this.inscricaoRepositorio = inscricaoRepositorio;
        this.estrategiaPadrao = estrategiaPadrao;
        this.ingressoServico = ingressoServico;
        this.transacaoServico = transacaoServico;
        this.usuarioServico = usuarioServico;
    }

    public SorteioId criar(EventoId eventoId, TipoIngressoId tipoIngressoId, UsuarioId organizadorId,
            int quantidadeIngressos, int quantidadeListaEspera,
            LocalDateTime prazoInscricao, int prazoConfirmacaoHoras) {
        Sorteio sorteio = new Sorteio(eventoId, tipoIngressoId, organizadorId,
                quantidadeIngressos, quantidadeListaEspera, prazoInscricao, prazoConfirmacaoHoras);
        sorteioServico.salvar(sorteio);
        return sorteio.getId();
    }

    public void abrirInscricoes(SorteioId sorteioId, UsuarioId solicitanteId) {
        sorteioServico.abrirInscricoes(sorteioId, resolverGestor(sorteioId, solicitanteId));
    }

    public void encerrarInscricoes(SorteioId sorteioId, UsuarioId solicitanteId) {
        sorteioServico.encerrarInscricoes(sorteioId, resolverGestor(sorteioId, solicitanteId));
    }

    // O admin pode gerenciar qualquer sorteio. Como a validação de domínio só
    // reconhece o organizador dono, tratamos o admin como o próprio organizador.
    private UsuarioId resolverGestor(SorteioId sorteioId, UsuarioId solicitanteId) {
        if (usuarioServico.obter(solicitanteId).temPapel(Papel.ADMIN)) {
            return sorteioServico.obter(sorteioId).getOrganizadorId();
        }
        return solicitanteId;
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

        // Sorteio é gratuito: ao confirmar, o ingresso é emitido na carteira do
        // participante e registramos uma transação SORTEIO (valor zero) para rastreabilidade.
        Ingresso ingresso = new Ingresso(sorteio.getTipoIngressoId(), sorteio.getEventoId(), participanteId);
        ingressoServico.salvar(ingresso);

        transacaoServico.registrar(new Transacao(
                participanteId, TipoTransacao.SORTEIO,
                Dinheiro.ZERO, LocalDateTime.now(),
                UUID.randomUUID()));

        long totalConfirmados = inscricaoRepositorio.pesquisarPorSorteio(sorteioId).stream()
                .filter(i -> i.getStatus() == StatusInscricao.CONFIRMADO)
                .count();
        boolean todasVagasPreenchidas = totalConfirmados >= sorteio.getQuantidadeIngressos();
        if (todasVagasPreenchidas) {
            sorteioServico.encerrar(sorteioId);
        }
    }

    public void cancelar(SorteioId sorteioId, UsuarioId solicitanteId) {
        sorteioServico.cancelar(sorteioId, resolverGestor(sorteioId, solicitanteId));
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
