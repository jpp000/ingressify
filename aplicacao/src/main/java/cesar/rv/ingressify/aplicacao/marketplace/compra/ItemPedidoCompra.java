package cesar.rv.ingressify.aplicacao.marketplace.compra;

/** Item de um pedido de compra: um tipo de ingresso, quantidade e opção de meia-entrada. */
public record ItemPedidoCompra(int tipoIngressoId, int quantidade, boolean meiaEntrada, String documento) {
}
