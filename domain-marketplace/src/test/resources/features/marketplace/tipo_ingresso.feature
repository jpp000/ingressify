Feature: Gerenciar tipo de ingresso

  Scenario: Tipo de ingresso com quantidade disponível pode ser reservado
    Given um tipo de ingresso com 10 ingressos disponíveis
    When reservo 3 ingressos do tipo
    Then a quantidade disponível do tipo é 7

  Scenario: Tipo de ingresso sem ingressos disponíveis rejeita reserva
    Given um tipo de ingresso sem ingressos disponíveis
    When tento reservar 1 ingresso do tipo indisponível
    Then a reserva do ingresso é rejeitada

  Scenario: Quantidade devolvida aumenta o estoque
    Given um tipo de ingresso com 5 ingressos disponíveis de um total de 10
    When devolvo 2 ingressos ao tipo
    Then a quantidade disponível do tipo é 7
