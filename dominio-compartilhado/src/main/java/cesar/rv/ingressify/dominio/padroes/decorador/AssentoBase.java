package cesar.rv.ingressify.dominio.padroes.decorador;

import java.math.BigDecimal;

import org.apache.commons.lang3.Validate;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;

/**
 * Componente concreto do Decorator: representa o assento base sem benefícios adicionais.
 */
public class AssentoBase implements AssentoComponente {

    private final String codigo;
    private final BigDecimal preco;
    private final TipoAssento tipo;

    public AssentoBase(String codigo, BigDecimal preco, TipoAssento tipo) {
        Validate.notBlank(codigo, "codigo");
        Validate.notNull(preco, "preco");
        Validate.notNull(tipo, "tipo");
        this.codigo = codigo;
        this.preco = preco;
        this.tipo = tipo;
    }

    @Override
    public String getCodigo() { return codigo; }

    @Override
    public BigDecimal getPreco() { return preco; }

    @Override
    public String getDescricao() { return "Assento " + codigo; }

    @Override
    public TipoAssento getTipo() { return tipo; }
}
