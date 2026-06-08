package cesar.rv.ingressify.dominio.padroes.decorador;

import java.math.BigDecimal;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;

/**
 * Decorador abstrato: delega todas as chamadas ao componente envolvido.
 * Subclasses sobrescrevem apenas o que diferem.
 */
public abstract class AssentoDecorador implements AssentoComponente {

    protected final AssentoComponente envolvido;

    protected AssentoDecorador(AssentoComponente envolvido) {
        Validate.notNull(envolvido, "envolvido");
        this.envolvido = envolvido;
    }

    @Override
    public String getCodigo() { return envolvido.getCodigo(); }

    @Override
    public BigDecimal getPreco() { return envolvido.getPreco(); }

    @Override
    public String getDescricao() { return envolvido.getDescricao(); }

    @Override
    public TipoAssento getTipo() { return envolvido.getTipo(); }
}
