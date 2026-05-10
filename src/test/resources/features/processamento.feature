# language: pt
Funcionalidade: Processamento de solicitação padronizada
  Como backend de Gestão de Reclamações
  Quero processar solicitações padronizadas
  Para criar reclamações oficiais com classificação e SLA

  Cenário: Classificar reclamação com indício de fraude
    Dado uma solicitação com texto "não reconheço uma compra no meu cartão"
    Quando a solicitação for processada
    Então a categoria principal deve ser FRAUDE
    E a justificativa deve conter regra relacionada a compra não reconhecida

  Cenário: Criar reclamação oficial a partir de solicitação padronizada válida
    Dado que existe uma solicitação padronizada válida
    Quando a solicitação for processada
    Então uma reclamação oficial deve ser criada
    E deve possuir protocolo
    E deve possuir classificação
    E deve possuir SLA

  Cenário: Não duplicar reclamação para mesma solicitação
    Dado que uma solicitação padronizada já foi processada
    Quando a mesma solicitação for processada novamente
    Então nenhuma nova reclamação deve ser criada

  Cenário: Classificação ambígua exige revisão
    Dado uma solicitação com texto ambíguo entre cobrança e cartão
    Quando a solicitação for processada
    Então a classificação deve indicar confiança baixa ou média
    E deve conter categorias candidatas

  Cenário: Emitir alerta para reclamação próxima do SLA
    Dado uma reclamação com alertAt vencido
    Quando o job de SLA for executado
    Então um alerta deve ser emitido
    E a reclamação deve registrar alerta emitido
