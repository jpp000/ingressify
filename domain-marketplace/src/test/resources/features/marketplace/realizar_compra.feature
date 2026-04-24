Feature: Realizar compra
  Scenario: Quantidade deve ser positiva
    When inicio compra com quantidade zero
    Then a operação é inválida
