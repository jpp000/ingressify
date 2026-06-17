# language: pt
Funcionalidade: Cupom de desconto em compras de ingressos

  Cenário: Cupom percentual reduz o valor da compra proporcionalmente
    Dado um cupom percentual de 20% para o evento
    Quando aplico o cupom a uma compra de R$200,00
    Então o valor com desconto é R$160,00

  Cenário: Cupom de valor fixo abate um montante definido
    Dado um cupom de valor fixo de R$50,00 para o evento
    Quando aplico o cupom a uma compra de R$200,00
    Então o valor com desconto é R$150,00

  Cenário: Cupom expirado é rejeitado
    Dado um cupom percentual de 10% já expirado
    Quando tento aplicar o cupom expirado
    Então o cupom é rejeitado com mensagem "cupom expirado"

  Cenário: Cupom que ainda não está vigente é rejeitado
    Dado um cupom percentual de 10% ainda não vigente
    Quando tento aplicar o cupom ainda não vigente
    Então o cupom é rejeitado com mensagem "cupom ainda não está vigente"

  Cenário: Cupom com limite de usos esgotado é rejeitado
    Dado um cupom com limite de 1 uso já consumido
    Quando tento aplicar o cupom esgotado
    Então o cupom é rejeitado com mensagem "limite de usos do cupom atingido"

  Cenário: Cupom de outro evento não é válido para a compra
    Dado um cupom criado para o evento 99
    Quando aplico o cupom a uma compra do evento 1
    Então o cupom é rejeitado com mensagem "cupom não é válido para este evento"

  Cenário: Valor de compra abaixo do mínimo exigido pelo cupom é rejeitado
    Dado um cupom com valor mínimo de R$300,00
    Quando tento aplicar o cupom a uma compra de R$100,00
    Então o cupom é rejeitado com mensagem "valor mínimo para uso do cupom não atingido"

  Cenário: Consumir o cupom incrementa o contador de usos
    Dado um cupom percentual de 10% para o evento
    Quando consumo o cupom uma vez em uma compra de R$200,00
    Então o cupom registra 1 uso
