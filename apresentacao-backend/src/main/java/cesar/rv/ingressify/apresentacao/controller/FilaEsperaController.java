package cesar.rv.ingressify.apresentacao.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cesar.rv.ingressify.aplicacao.marketplace.mapaAssentos.FilaEsperaServicoAplicacao;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.MapaAssentosId;

@RestController
@RequestMapping("/mapas-assentos/{mapaId}/fila")
public class FilaEsperaController {

    private final FilaEsperaServicoAplicacao filaServico;

    public FilaEsperaController(FilaEsperaServicoAplicacao filaServico) {
        this.filaServico = filaServico;
    }

    @PostMapping("/entrar")
    public ResponseEntity<?> entrar(
            @PathVariable int mapaId,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            int posicao = filaServico.entrar(new MapaAssentosId(mapaId), new UsuarioId(usuarioId));
            return ResponseEntity.ok(Map.of("posicao", posicao,
                    "mensagem", "Você está na posição " + posicao + " da fila de espera"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        }
    }

    @DeleteMapping("/sair")
    public ResponseEntity<?> sair(
            @PathVariable int mapaId,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        try {
            filaServico.sair(new MapaAssentosId(mapaId), new UsuarioId(usuarioId));
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("motivo", e.getMessage()));
        }
    }

    @GetMapping("/posicao")
    public ResponseEntity<?> consultarPosicao(
            @PathVariable int mapaId,
            @RequestHeader("X-Usuario-Id") int usuarioId) {
        Optional<Integer> posicao = filaServico.consultarPosicao(
                new MapaAssentosId(mapaId), new UsuarioId(usuarioId));
        int tamanho = filaServico.tamanhoFila(new MapaAssentosId(mapaId));
        if (posicao.isEmpty()) {
            return ResponseEntity.ok(Map.of("naFila", false, "tamanhoFila", tamanho));
        }
        return ResponseEntity.ok(Map.of("naFila", true, "posicao", posicao.get(), "tamanhoFila", tamanho));
    }
}
