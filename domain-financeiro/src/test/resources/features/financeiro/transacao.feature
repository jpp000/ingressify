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

  Scenario: Iterador percorre todas as transações do histórico
    Given uma transação de compra de 100 reais registrada para o usuário 1
    And uma transação de venda de 90 reais registrada para o usuário 1
    When itero o histórico do usuário 1 com o iterador
    Then o iterador percorre 2 transações no total

  Scenario: Iterador verifica presença e ausência de próximo elemento
    Given uma transação de compra de 100 reais registrada para o usuário 1
    When itero o histórico do usuário 1 com o iterador
    Then o iterador tem próximo elemento antes de iterar
    And após consumir todos os elementos o iterador não tem próximo

  Scenario: Iterador em histórico vazio não tem próximo elemento
    When itero o histórico do usuário 1 com o iterador
    Then o iterador não tem próximo elemento
