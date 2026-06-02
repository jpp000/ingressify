Feature: Reembolso de ingressos

  Scenario: Reembolso voluntário dentro do prazo é permitido
    Given a compra foi feita há 3 dias
    And o evento ocorre em 5 dias
    When valido a estratégia de reembolso voluntário
    Then a validação passa sem erro

  Scenario: Reembolso voluntário fora do prazo de 7 dias é rejeitado
    Given a compra foi feita há 8 dias
    And o evento ocorre em 5 dias
    When valido a estratégia de reembolso voluntário
    Then a validação é rejeitada com "7 dias"

  Scenario: Reembolso voluntário com evento em menos de 48 horas é rejeitado
    Given a compra foi feita há 1 dia
    And o evento ocorre em 24 horas
    When valido a estratégia de reembolso voluntário
    Then a validação é rejeitada com "48 horas"

  Scenario: Reembolso por cancelamento de evento é sempre aprovado
    Given a compra foi feita há 30 dias
    And o evento ocorre em 1 hora
    When valido a estratégia de reembolso por cancelamento
    Then a validação passa sem erro

  Scenario: Reembolso por cancelamento ignora prazo de compra antiga
    Given a compra foi feita há 365 dias
    And o evento ocorre em 1 dia
    When valido a estratégia de reembolso por cancelamento
    Then a validação passa sem erro
