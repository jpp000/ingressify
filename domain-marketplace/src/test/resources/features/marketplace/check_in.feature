Feature: Check-in de ingressos

  Scenario: Escanear QR válido registra check-in e marca ingresso como utilizado
    Given um ingresso ativo para check-in
    When o operador realiza o check-in do ingresso
    Then o check-in é registrado com sucesso
    And o ingresso fica com status UTILIZADO

  Scenario: Escanear QR de ingresso já utilizado retorna erro
    Given um ingresso com status UTILIZADO
    When o operador tenta check-in no ingresso já utilizado
    Then o check-in é rejeitado

  Scenario: Escanear QR de ingresso cancelado retorna erro
    Given um ingresso com status CANCELADO
    When o operador tenta check-in no ingresso cancelado
    Then o check-in é rejeitado

  Scenario: Escanear QR de ingresso em revenda retorna erro
    Given um ingresso com status EM_REVENDA
    When o operador tenta check-in no ingresso em revenda
    Then o check-in é rejeitado

  Scenario: Ingresso bloqueado para reembolso não pode fazer check-in
    Given um ingresso ativo bloqueado para reembolso
    When o operador tenta check-in no ingresso bloqueado
    Then o check-in é rejeitado

  Scenario: Auditoria registra operador no check-in
    Given um ingresso ativo para check-in
    And o operador tem id 42
    When o operador realiza o check-in do ingresso
    Then o registro de check-in contém o operador 42

  Scenario: Check-in marca ingresso como utilizado e bloqueia transferência
    Given um ingresso ativo para check-in
    When o operador realiza o check-in do ingresso
    Then o ingresso fica com status UTILIZADO
    And a transferência do ingresso utilizado é rejeitada

  Scenario: Bloquear ingresso para reembolso impede transferência
    Given um ingresso ativo para check-in
    When bloqueio o ingresso para reembolso
    Then a transferência do ingresso bloqueado é rejeitada
