Feature: Cadastrar evento
  Scenario: Nome em branco é inválido
    When tento criar um evento com nome em branco
    Then a criação é rejeitada
