Feature: Histórico de transações

  Scenario: Transação de compra é registrada e consultada pelo usuário
    Given uma transação de compra de 100 reais registrada para o usuário 1
    When busco o histórico do usuário 1
    Then o histórico contém 1 transação do tipo compra com valor 100 reais

  Scenario: Transação de venda é registrada e consultada pelo usuário
    Given uma transação de venda de 90 reais registrada para o usuário 1
    When busco o histórico do usuário 1
    Then o histórico contém 1 transação do tipo venda com valor 90 reais

  Scenario: Múltiplas transações de um usuário são retornadas juntas
    Given uma transação de compra de 100 reais registrada para o usuário 1
    And uma transação de venda de 90 reais registrada para o usuário 1
    When busco o histórico do usuário 1
    Then o histórico contém 2 transações
