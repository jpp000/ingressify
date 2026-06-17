Feature: Gerenciar saldo

  Scenario: Saldo pode ser creditado
    Given um saldo de 100 reais
    When credito 50 reais ao saldo
    Then o saldo passa a 150 reais

  Scenario: Saldo pode ser debitado quando há fundos suficientes
    Given um saldo de 100 reais
    When debito 30 reais do saldo
    Then o saldo passa a 70 reais

  Scenario: Débito é rejeitado quando saldo é insuficiente
    Given um saldo de 20 reais
    When tento debitar 50 reais do saldo
    Then o débito é rejeitado
