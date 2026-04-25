Feature: Histórico de transações

  Scenario: Transação de compra é registrada com dados válidos
    Given uma transação de compra de 100 reais
    Then a transação possui tipo compra e valor 100 reais

  Scenario: Transação de venda é registrada com dados válidos
    Given uma transação de venda de 90 reais
    Then a transação possui tipo venda e valor 90 reais

  Scenario: Transação de ajuste de saldo é registrada com dados válidos
    Given uma transação de ajuste de saldo de 200 reais
    Then a transação possui tipo ajuste de saldo e valor 200 reais
