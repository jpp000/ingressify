package cesar.rv.ingressify.dominio.padroes.decorador;

import java.math.BigDecimal;

import cesar.rv.ingressify.dominio.marketplace.mapaAssentos.TipoAssento;

/**
 * Padrão Decorator — componente base para assentos decoráveis.
 * Permite adicionar comportamentos (preço extra, benefícios) sem herança.
 */
public interface AssentoComponente {

    String getCodigo();

    BigDecimal getPreco();

    String getDescricao();

    TipoAssento getTipo();
}
