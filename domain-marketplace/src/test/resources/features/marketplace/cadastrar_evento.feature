Feature: Cadastrar evento

  Scenario: Nome em branco é rejeitado
    When tento criar um evento com nome em branco
    Then a criação é rejeitada

  Scenario: Data no passado é rejeitada
    When tento criar um evento com data no passado
    Then a criação é rejeitada

  Scenario: Capacidade zero é rejeitada
    When tento criar um evento com capacidade zero
    Then a criação é rejeitada

  Scenario: Evento criado com dados válidos tem status ativo
    When crio um evento com dados válidos
    Then o evento é persistido com status ativo

  Scenario: Evento ativo pode ser cancelado
    Given um evento ativo
    When cancelo o evento
    Then o status do evento é cancelado

  Scenario: Dados do evento podem ser atualizados antes de iniciar
    Given um evento ativo
    When atualizo o nome do evento
    Then o nome do evento é atualizado

  Scenario: Evento já iniciado não pode ser atualizado
    Given um evento já iniciado
    When tento atualizar os dados do evento
    Then a atualização do evento é rejeitada
