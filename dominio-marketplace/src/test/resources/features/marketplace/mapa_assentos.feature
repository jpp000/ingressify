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
