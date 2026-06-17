# language: pt
Funcionalidade: Denúncia de eventos pelos compradores

  Cenário: Usuário registra denúncia com status PENDENTE
    Dado um evento ativo disponível no sistema
    Quando um usuário denuncia o evento com motivo FRAUDE
    Então a denúncia é registrada com status PENDENTE

  Cenário: Usuário não pode denunciar o mesmo evento duas vezes
    Dado um evento com uma denúncia já registrada pelo usuário
    Quando o mesmo usuário tenta denunciar o mesmo evento novamente
    Então a segunda denúncia é rejeitada com erro de duplicidade

  Cenário: Admin inicia a análise de uma denúncia pendente
    Dado uma denúncia com status PENDENTE
    Quando o admin inicia a análise da denúncia
    Então o status da denúncia é EM_ANALISE

  Cenário: Admin aprova uma denúncia em análise
    Dado uma denúncia com status EM_ANALISE
    Quando o admin aprova a denúncia
    Então o status da denúncia é APROVADA

  Cenário: Admin rejeita uma denúncia em análise
    Dado uma denúncia com status EM_ANALISE
    Quando o admin rejeita a denúncia
    Então o status da denúncia é REJEITADA

  Cenário: Admin pode aprovar denúncia diretamente sem passar por EM_ANALISE
    Dado uma denúncia com status PENDENTE
    Quando o admin aprova a denúncia diretamente
    Então o status da denúncia é APROVADA

  Cenário: Denúncia já decidida não pode ter novo andamento
    Dado uma denúncia com status APROVADA
    Quando o admin tenta iniciar análise da denúncia já decidida
    Então a operação é rejeitada com erro de status inválido
