# language: pt
Funcionalidade: Compra de ingressos em grupo

  Cenário: Grupo é criado com status ABERTO
    Dado um grupo de compra criado pelo líder com 2 participantes
    Então o status do grupo é ABERTO

  Cenário: Todos os participantes pagos confirmam o grupo
    Dado um grupo de compra com 2 participantes
    Quando todos os participantes confirmam pagamento
    Então o grupo pode ser confirmado com status CONFIRMADO

  Cenário: Grupo não pode ser confirmado com pagamentos pendentes
    Dado um grupo de compra com 2 participantes
    Quando apenas 1 dos participantes confirma pagamento
    Então a confirmação do grupo é rejeitada

  Cenário: Apenas o líder pode cancelar o grupo
    Dado um grupo de compra com status ABERTO
    Quando o líder cancela o grupo
    Então o status do grupo é CANCELADO

  Cenário: Participante que não é líder não pode cancelar o grupo
    Dado um grupo de compra com status ABERTO
    Quando um participante não-líder tenta cancelar o grupo
    Então o cancelamento é rejeitado com erro

  Cenário: Grupo com prazo expirado fica inativo
    Dado um grupo de compra com prazo vencido
    Então o prazo do grupo está expirado

  Cenário: Grupo confirmado não pode ser cancelado
    Dado um grupo de compra já confirmado
    Quando o líder tenta cancelar o grupo confirmado
    Então o cancelamento é rejeitado por status inválido

  Cenário: Participante marca pagamento pendente como pago
    Dado um participante com status PENDENTE
    Quando o participante confirma o pagamento individual
    Então o status do participante é PAGO
