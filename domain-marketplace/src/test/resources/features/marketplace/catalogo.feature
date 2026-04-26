Feature: Catálogo de eventos

  Scenario: Catálogo lista todos os eventos ativos com data futura
    Given 3 eventos ativos cadastrados
    When visualizo o catálogo de eventos
    Then o catálogo exibe 3 eventos

  Scenario: Eventos cancelados não aparecem no catálogo
    Given um evento ativo e um evento cancelado cadastrados
    When visualizo o catálogo de eventos
    Then o catálogo exibe 1 evento

  Scenario: Eventos já iniciados não aparecem no catálogo
    Given um evento ativo e um evento já iniciado cadastrados
    When visualizo o catálogo de eventos
    Then o catálogo exibe 1 evento

  Scenario: Catálogo vazio quando não há eventos ativos
    Given nenhum evento cadastrado
    When visualizo o catálogo de eventos
    Then o catálogo está vazio
