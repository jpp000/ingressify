# language: pt
Funcionalidade: Gestão de reembolso pelo administrador

  Cenário: Solicitação criada fica com status PENDENTE
    Dado uma solicitação de reembolso voluntário criada pelo usuário
    Então o status da solicitação é PENDENTE

  Cenário: Admin move a solicitação para EM_ANALISE
    Dado uma solicitação de reembolso com status PENDENTE
    Quando o admin inicia a análise da solicitação
    Então o status da solicitação é EM_ANALISE

  Cenário: Admin aprova solicitação em análise e registra data de decisão
    Dado uma solicitação de reembolso com status EM_ANALISE
    Quando o admin aprova a solicitação
    Então o status da solicitação é APROVADA
    E a data de decisão é preenchida

  Cenário: Admin recusa solicitação em análise
    Dado uma solicitação de reembolso com status EM_ANALISE
    Quando o admin recusa a solicitação
    Então o status da solicitação é RECUSADA

  Cenário: Admin pode aprovar solicitação diretamente de PENDENTE
    Dado uma solicitação de reembolso com status PENDENTE
    Quando o admin aprova diretamente a solicitação pendente
    Então o status da solicitação é APROVADA

  Cenário: Admin pode recusar solicitação diretamente de PENDENTE
    Dado uma solicitação de reembolso com status PENDENTE
    Quando o admin recusa diretamente a solicitação pendente
    Então o status da solicitação é RECUSADA

  Cenário: Usuário cancela sua própria solicitação pendente
    Dado uma solicitação de reembolso com status PENDENTE
    Quando o usuário cancela a própria solicitação
    Então o status da solicitação é CANCELADA

  Cenário: Solicitação aprovada não pode ser alterada
    Dado uma solicitação de reembolso com status APROVADA
    Quando o admin tenta recusar a solicitação já aprovada
    Então a operação é rejeitada com erro de status
