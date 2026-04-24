Feature: Processar pagamento
  Scenario: Pagamento pendente pode ser confirmado
    Given um pagamento pendente
    When confirmo o pagamento
    Then o status passa a confirmado
