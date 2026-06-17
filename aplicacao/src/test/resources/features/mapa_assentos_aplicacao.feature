Feature: Mapa de Assentos — Regras de Aplicação

  Scenario: Evento sem capacidade numerada rejeita criação de mapa via serviço de aplicação
    Given um evento sem capacidade numerada configurada
    When o organizador tenta criar um mapa de assentos para este evento
    Then a criação do mapa é rejeitada por falta de capacidade numerada

  Scenario: Mapa maior que a capacidade numerada do evento é rejeitado
    Given um evento com capacidade total 100 e capacidade numerada 20
    When o organizador tenta criar um mapa com 5 fileiras e 5 assentos por fileira
    Then a criação do mapa é rejeitada por exceder capacidade numerada
