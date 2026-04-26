Feature: Anúncio de revenda

  Scenario: Anúncio disponível pode ser reservado
    Given um anúncio de revenda disponível
    When reservo o anúncio para um comprador
    Then o status do anúncio é reservado

  Scenario: Anúncio reservado pode ser concluído
    Given um anúncio de revenda reservado
    When concluo o anúncio de revenda
    Then o status do anúncio é vendido

  Scenario: Anúncio reservado pode ter reserva cancelada
    Given um anúncio de revenda reservado
    When cancelo a reserva do anúncio
    Then o status do anúncio volta a disponível

  Scenario: Anúncio vendido não pode ser cancelado
    Given um anúncio de revenda vendido
    When tento cancelar o anúncio vendido
    Then o cancelamento do anúncio é rejeitado

  Scenario: Preço de anúncio disponível pode ser alterado
    Given um anúncio de revenda disponível
    When altero o preço do anúncio para 150 reais
    Then o preço do anúncio é 150 reais

  Scenario: Preço de anúncio reservado não pode ser alterado
    Given um anúncio de revenda reservado
    When tento alterar o preço do anúncio reservado
    Then a alteração de preço é rejeitada

  Scenario: Anúncio não pode ser criado para ingresso de outro usuário
    Given um ingresso pertencente ao usuário 1
    When tento criar um anúncio para esse ingresso como usuário 2
    Then a criação do anúncio é rejeitada

  Scenario: Ingresso que já está em revenda não pode ter novo anúncio
    Given um ingresso com anúncio já ativo
    When tento criar mais um anúncio para o mesmo ingresso
    Then a criação do segundo anúncio é rejeitada
