package cesar.rv.ingressify.aplicacao.marketplace.reembolso;

import java.time.LocalDateTime;

import cesar.rv.ingressify.dominio.financeiro.Dinheiro;
import cesar.rv.ingressify.dominio.identidade.UsuarioId;
import cesar.rv.ingressify.dominio.marketplace.ingresso.IngressoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.MotivoReembolso;
import cesar.rv.ingressify.dominio.marketplace.reembolso.SolicitacaoReembolsoId;
import cesar.rv.ingressify.dominio.marketplace.reembolso.StatusSolicitacaoReembolso;

public interface ReembolsoResumo {

	SolicitacaoReembolsoId getId();

	IngressoId getIngressoId();

	MotivoReembolso getMotivo();

	StatusSolicitacaoReembolso getStatus();

	Dinheiro getValor();

	LocalDateTime getCriadaEm();

	LocalDateTime getDecididaEm();
}
