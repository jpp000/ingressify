Feature: Avaliação de eventos

  Scenario: Avaliar evento com nota válida é aceito
    Given uma avaliação com nota 4 e comentário "Ótimo evento"
    When salvo a avaliação
    Then a avaliação é persistida com nota 4

  Scenario: Nota fora do intervalo 1-5 é rejeitada
    When tento criar uma avaliação com nota 0
    Then a criação é rejeitada por nota inválida

  Scenario: Nota 6 também é inválida
    When tento criar uma avaliação com nota 6
    Then a criação é rejeitada por nota inválida

  Scenario: Comentário é opcional na avaliação
    Given uma avaliação com nota 3 e sem comentário
    When salvo a avaliação
    Then a avaliação é persistida com nota 3

  Scenario: Editar avaliação existente atualiza nota e comentário
    Given uma avaliação com nota 3 e comentário "Ok"
    When edito a avaliação para nota 5 e comentário "Excelente"
    Then a avaliação fica com nota 5 e comentário "Excelente"

  Scenario: Organizador responde avaliação
    Given uma avaliação com nota 4 e comentário "Bom"
    When salvo a avaliação
    And o organizador responde com "Obrigado pelo feedback"
    Then a avaliação contém resposta do organizador "Obrigado pelo feedback"

  Scenario: Média de avaliações é calculada corretamente
    Given avaliações com notas 3, 4 e 5 para o mesmo evento
    When calculo a média do evento
    Then a média é 4.0
