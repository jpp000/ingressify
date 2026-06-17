package cesar.rv.ingressify.dominio.padroes.decorador;

import java.math.BigDecimal;
import java.math.RoundingMode;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;

/**
 * Decorador VIP: adiciona 50% ao preço base e rótula o tipo como VIP.
 */
public class AssentoVipDecorador extends AssentoDecorador {

    private static final BigDecimal FATOR_VIP = new BigDecimal("1.50");

    public AssentoVipDecorador(AssentoComponente envolvido) {
        super(envolvido);
    }

    @Override
    public BigDecimal getPreco() {
        return envolvido.getPreco().multiply(FATOR_VIP).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescricao() {
        return envolvido.getDescricao() + " [Área VIP — Lounge exclusivo]";
    }

    @Override
    public TipoAssento getTipo() {
        return TipoAssento.VIP;
    }
}
