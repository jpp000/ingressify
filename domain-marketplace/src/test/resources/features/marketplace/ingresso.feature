Feature: Gerenciar ingresso

  Scenario: Ingresso ativo pode ser transferido
    Given um ingresso ativo do usuário 1
    When transfiro o ingresso para o usuário 2
    Then o ingresso pertence ao usuário 2

  Scenario: Ingresso ativo pode ser marcado para revenda
    Given um ingresso ativo do usuário 1
    When marco o ingresso para revenda
    Then o status do ingresso é em revenda

  Scenario: Ingresso em revenda pode ter revenda concluída
    Given um ingresso em revenda
    When concluo a revenda do ingresso para o usuário 2
    Then o status do ingresso é revendido

  Scenario: Ingresso em revenda pode ser desmarcado da revenda
    Given um ingresso em revenda
    When desmarca o ingresso da revenda
    Then o status do ingresso volta a ativo

  Scenario: Ingresso não ativo não pode ser transferido
    Given um ingresso em revenda
    When tento transferir o ingresso em revenda para o usuário 2
    Then a operação no ingresso é rejeitada
