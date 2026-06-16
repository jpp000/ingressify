package cesar.rv.ingressify.infraestrutura.agendamento;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.FilaEsperaServicoAplicacao;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosServico;

@Component
public class AgendadorTarefas {

    private final MapaAssentosServico mapaServico;
    private final FilaEsperaServicoAplicacao filaServico;

    public AgendadorTarefas(MapaAssentosServico mapaServico,
            FilaEsperaServicoAplicacao filaServico) {
        this.mapaServico = mapaServico;
        this.filaServico = filaServico;
    }

    @Scheduled(fixedDelay = 60_000)
    public void liberarReservasEPromoverFila() {
        List<MapaAssentosId> todos = mapaServico.listarTodosIds();
        for (MapaAssentosId mapaId : todos) {
            mapaServico.liberarReservasExpiradas(mapaId);
            filaServico.promoverProximo(mapaId);
        }
    }
}
