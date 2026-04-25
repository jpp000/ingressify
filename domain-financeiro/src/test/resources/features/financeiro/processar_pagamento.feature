Feature: Processar pagamento

  Scenario: Pagamento pendente pode ser confirmado
    Given um pagamento pendente
    When confirmo o pagamento
    Then o status passa a confirmado

  Scenario: Pagamento pendente pode ser rejeitado
    Given um pagamento pendente
    When rejeito o pagamento
    Then o status passa a rejeitado

  Scenario: Pagamento já confirmado não pode ser confirmado novamente
    Given um pagamento já confirmado
    When tento confirmar o pagamento novamente
    Then a confirmação do pagamento é rejeitada
