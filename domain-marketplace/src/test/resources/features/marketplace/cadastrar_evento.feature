Feature: Cadastrar evento

  Scenario: Nome em branco é inválido
    When tento criar um evento com nome em branco
    Then a criação é rejeitada

  Scenario: Data no passado é inválida
    When tento criar um evento com data no passado
    Then a criação é rejeitada

  Scenario: Capacidade zero é inválida
    When tento criar um evento com capacidade zero
    Then a criação é rejeitada

  Scenario: Evento ativo pode ser cancelado
    Given um evento ativo
    When cancelo o evento
    Then o status do evento é cancelado

  Scenario: Evento já iniciado não pode ser atualizado
    Given um evento já iniciado
    When tento atualizar os dados do evento
    Then a atualização do evento é rejeitada
