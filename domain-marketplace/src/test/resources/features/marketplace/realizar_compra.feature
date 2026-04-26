Feature: Realizar compra

  Scenario: Quantidade deve ser positiva
    When inicio compra com quantidade zero
    Then a operação é inválida

  Scenario: Compra pendente é criada com dados válidos
    Given um tipo de ingresso e evento válidos
    When inicio uma compra com quantidade válida
    Then a compra pendente é criada com sucesso

  Scenario: Compra pendente rejeita quantidade negativa
    When inicio compra com quantidade negativa
    Then a operação é inválida
