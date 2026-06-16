package cesar.rv.ingressify.infraestrutura.agendamento;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cesar.rv.ingressify.aplicacao.marketplace.grupoCompra.GrupoCompraServicoAplicacao;
import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.FilaEsperaServicoAplicacao;
import cesar.rv.ingressify.dominio.marketplace.grupoCompra.GrupoCompra;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;

@Component
public class AgendadorTarefas {

    private final MapaAssentosServico mapaServico;
    private final FilaEsperaServicoAplicacao filaServico;
    private final GrupoCompraServicoAplicacao grupoCompraServico;

    public AgendadorTarefas(MapaAssentosServico mapaServico,
            FilaEsperaServicoAplicacao filaServico,
            GrupoCompraServicoAplicacao grupoCompraServico) {
        this.mapaServico = mapaServico;
        this.filaServico = filaServico;
        this.grupoCompraServico = grupoCompraServico;
    }

    @Scheduled(fixedDelay = 60_000)
    public void liberarReservasEPromoverFila() {
        List<MapaAssentosId> todos = mapaServico.listarTodosIds();
        for (MapaAssentosId mapaId : todos) {
            mapaServico.liberarReservasExpiradas(mapaId);
            filaServico.promoverProximo(mapaId);
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void expirarGruposDeCompra() {
        List<GrupoCompra> expirados = grupoCompraServico.listarExpirados();
        for (GrupoCompra grupo : expirados) {
            grupoCompraServico.expirarSeNecessario(grupo.getId());
        }
    }
}
