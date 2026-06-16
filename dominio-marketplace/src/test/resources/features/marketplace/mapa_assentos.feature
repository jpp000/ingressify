Feature: Mapa de Assentos

  Scenario: Organizador cria mapa de assentos para um evento
    Given um evento sem mapa de assentos configurado
    When o organizador cria um mapa com 3 fileiras e 4 assentos por fileira
    Then o mapa é criado com 12 assentos

  Scenario: Assentos VIP são gerados na primeira fileira central
    Given um mapa de assentos com 3 fileiras e 6 colunas criado
    When o organizador lista os assentos do mapa
    Then assentos na fileira A colunas 3 e 4 são do tipo VIP

  Scenario: Assentos de acessibilidade ficam na última fileira das extremidades
    Given um mapa de assentos com 3 fileiras e 6 colunas criado
    When o organizador lista os assentos do mapa
    Then assentos na última fileira colunas 1 e 6 são do tipo ACESSIBILIDADE

  Scenario: Comprador reserva assentos disponíveis
    Given um mapa de assentos criado com assentos disponíveis
    When um comprador reserva 2 assentos
    Then os 2 assentos ficam com status RESERVADO

  Scenario: Comprador não pode reservar assento já reservado
    Given um mapa de assentos com um assento já reservado
    When outro comprador tenta reservar o mesmo assento
    Then a reserva é rejeitada por indisponibilidade

  Scenario: Proxy bloqueia reserva que isolaria assento único
    Given um mapa com 1 fileira e 4 assentos onde A4 está vendido
    When um comprador tenta reservar A1 e A3 ao mesmo tempo
    Then a reserva é rejeitada por regra anti-ilha

  Scenario: Venda é confirmada após reserva
    Given um mapa de assentos com um assento reservado pelo comprador
    When o comprador confirma a compra do assento reservado
    Then o assento fica com status VENDIDO

  Scenario: Reservas expiradas são liberadas automaticamente
    Given um mapa de assentos com uma reserva expirada
    When o sistema libera reservas expiradas
    Then o assento volta ao status DISPONIVEL

  Scenario: Não é possível criar dois mapas para o mesmo evento
    Given um evento com mapa de assentos já configurado
    When o organizador tenta criar um segundo mapa para o mesmo evento
    Then a criação é rejeitada por duplicidade

  Scenario: Limite de 6 assentos por usuário por evento é aplicado
    Given um mapa com 2 fileiras e 6 assentos por fileira com 5 assentos já comprados pelo comprador
    When o comprador tenta reservar 2 assentos adicionais
    Then a reserva é rejeitada por limite de assentos por usuário

  Scenario: Confirmação de venda é rejeitada se reserva expirou
    Given um mapa com um assento com reserva expirada pertencente ao comprador
    When o comprador tenta confirmar a venda do assento expirado
    Then a confirmação é rejeitada por reserva expirada

  Scenario: Evento sem capacidade numerada rejeita criação de mapa via serviço de aplicação
    Given um evento sem capacidade numerada configurada
    When o organizador tenta criar um mapa de assentos para este evento
    Then a criação do mapa é rejeitada por falta de capacidade numerada

  Scenario: Mapa maior que a capacidade numerada do evento é rejeitado
    Given um evento com capacidade total 100 e capacidade numerada 20
    When o organizador tenta criar um mapa com 5 fileiras e 5 assentos por fileira
    Then a criação do mapa é rejeitada por exceder capacidade numerada
