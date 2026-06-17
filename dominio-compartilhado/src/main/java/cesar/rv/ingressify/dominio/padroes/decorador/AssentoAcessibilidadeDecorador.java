package cesar.rv.ingressify.dominio.padroes.decorador;

import java.math.BigDecimal;
import java.math.RoundingMode;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;

/**
 * Decorador de Acessibilidade: aplica 50% de desconto e marca como ACESSIBILIDADE.
 */
public class AssentoAcessibilidadeDecorador extends AssentoDecorador {

    private static final BigDecimal FATOR_DESCONTO = new BigDecimal("0.50");

    public AssentoAcessibilidadeDecorador(AssentoComponente envolvido) {
        super(envolvido);
    }

    @Override
    public BigDecimal getPreco() {
        return envolvido.getPreco().multiply(FATOR_DESCONTO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescricao() {
        return envolvido.getDescricao() + " [Acessível — PCD]";
    }

    @Override
    public TipoAssento getTipo() {
        return TipoAssento.ACESSIBILIDADE;
    }
}
