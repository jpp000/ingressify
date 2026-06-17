Feature: Sorteio de Ingressos

  Scenario: Sorteio configurado pode ter inscrições abertas
    Given um sorteio configurado para o evento
    When o organizador abre as inscrições
    Then o status do sorteio é INSCRICOES_ABERTAS

  Scenario: Comprador pode se inscrever em sorteio com inscrições abertas
    Given um sorteio com inscrições abertas
    When um comprador se inscreve no sorteio
    Then a inscrição é registrada com status INSCRITO

  Scenario: Comprador não pode se inscrever duas vezes no mesmo sorteio
    Given um sorteio com inscrições abertas
    And um comprador já inscrito no sorteio
    When o mesmo comprador tenta se inscrever novamente
    Then a segunda inscrição é rejeitada

  Scenario: Comprador não pode se inscrever em sorteio não aberto
    Given um sorteio configurado para o evento
    When um comprador tenta se inscrever antes das inscrições abrirem
    Then a inscrição é rejeitada por status inválido

  Scenario: Organizador encerra inscrições e sorteio fica aguardando execução
    Given um sorteio com inscrições abertas
    When o organizador encerra as inscrições
    Then o status do sorteio é AGUARDANDO_SORTEIO

  Scenario: Sorteio é executado e contemplados são selecionados
    Given um sorteio aguardando execução com 3 inscrições e 1 vaga
    When o sorteio é executado
    Then o status do sorteio é SORTEADO
    And exatamente 1 inscrição está com status CONTEMPLADO

  Scenario: Contemplado confirma participação e sorteio é encerrado automaticamente
    Given um sorteio com 1 vaga sorteado e o contemplado definido
    When o contemplado confirma a participação
    Then a inscrição do contemplado é CONFIRMADO
    And o status do sorteio é ENCERRADO

  Scenario: Sorteio pode ser cancelado antes de ser executado
    Given um sorteio com inscrições abertas
    When o organizador cancela o sorteio
    Then o status do sorteio é CANCELADO
    And todas as inscrições são CANCELADO

  Scenario: Sorteio encerrado não pode ser cancelado
    Given um sorteio encerrado
    When o organizador tenta cancelar o sorteio encerrado
    Then o cancelamento é rejeitado
