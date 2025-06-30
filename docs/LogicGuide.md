# Guia de Lógica de Negócio (Regras do Reino)

> Este documento centraliza as principais regras de negócio e fluxos de trabalho implementados na camada de `Service` do RPG Market.

## 1. `UserService` - Gerenciamento de Aventureiros

Este serviço dita as regras para o ciclo de vida e progressão dos usuários.

* **Registro de Novos Usuários:**
    * Um novo usuário não pode se registrar com um `username` ou `email` que já exista no banco de dados.
    * A senha fornecida é sempre criptografada com `PasswordEncoder` antes de ser salva.
    * Todo novo usuário recebe automaticamente o papel `ROLE_AVENTUREIRO`.
    * Valores iniciais são definidos: `level` 1, `experience` 0 e um saldo de `100.00` moedas de ouro.

* **Progressão de Nível e Experiência:**
    * A cada `100` pontos de experiência (`experience`) acumulados, o usuário sobe de nível.
    * Ao subir de nível, o usuário recebe um bônus de `100` moedas de ouro.
    * A experiência é concedida ao criar transações (compra/venda) e ao completá-las.

* **Alteração de Senha:**
    * O usuário deve fornecer a `currentPassword` (senha atual) correta para prosseguir.
    * A `newPassword` (nova senha) deve ser idêntica à `confirmPassword` (confirmação).
    * A nova senha deve ter no mínimo 6 caracteres.

## 2. `ProductService` e `BidService` - O Coração do Leilão

Estes serviços trabalham em conjunto para gerenciar o complexo fluxo de leilões.

* **Regras para Criar um Item:**
    * Um item do tipo `AUCTION` (Leilão) deve, obrigatoriamente, ter um preço inicial (`price`) maior que zero.
    * Se um leilão é criado sem uma data de término (`auctionEndDate`), o sistema define uma data padrão de **7 dias** a partir da criação.

* **Regras para Registrar um Lance (`placeBid`):**
    1.  **Validação de Status:** O sistema primeiro verifica se o item é de fato um `AUCTION` e se seu status é `AUCTION_ACTIVE`.
    2.  **Validação de Vendedor:** O vendedor do item (`seller`) não pode dar lances em seu próprio produto.
    3.  **Validação de Liderança:** Um usuário que já está com o lance mais alto não pode dar um novo lance sobre si mesmo.
    4.  **Validação de Valor:** O valor do novo lance (`amount`) deve ser maior que o preço atual do item somado ao incremento mínimo (`minBidIncrement`).
    5.  **Validação de Saldo:** O sistema verifica se o licitante possui moedas de ouro suficientes para cobrir o valor do lance.

* **Processo do Lance:**
    * Quando um novo lance válido é registrado, o(s) lance(s) vencedor(es) anteriores são marcados como não-vencedores (`winning = false`).
    * O novo lance é salvo com o status de vencedor (`winning = true`).
    * O preço atual do produto (`product.price`) é atualizado para o valor do novo lance.
    * **Importante:** O ouro **não é debitado** do licitante neste momento. Ele funciona como uma "reserva".

* **Finalização de Leilões (`checkEndedAuctions` e `processAuctionEnd`):**
    * A cada minuto (`@Scheduled(fixedRate = 60000)`), o sistema verifica por leilões cuja `auctionEndDate` já passou.
    * Ao finalizar um leilão, o sistema busca o lance vencedor (`winning = true`).
    * Se houver um vencedor, o sistema verifica **novamente** se ele ainda possui saldo.
        * **Se sim:** O ouro é transferido (debitado do comprador, creditado ao vendedor), o status do produto vira `SOLD` e uma transação é criada.
        * **Se não:** O leilão é encerrado sem venda e o status do produto vira `AUCTION_ENDED`.
    * Se não houver lances, o leilão é apenas encerrado (`AUCTION_ENDED`).

## 3. `CheckoutService` - O Fluxo de Compra

Este serviço orquestra o processo de compra, desde a revisão até a confirmação.

* **Preparação do Checkout (`prepareCheckout`):**
    * Valida se o produto está disponível para compra e se o comprador não é o próprio vendedor.
    * Calcula o valor total (`totalAmount`) baseado no tipo de compra (preço de venda direta ou preço "Comprar Agora" de um leilão).
    * Verifica se o comprador tem saldo suficiente (`hasSufficientFunds`).
    * Carrega o endereço de entrega padrão do usuário ou um endereço selecionado.
    * Retorna um objeto `CheckoutSummary` com todos os dados para a página de revisão.

* **Confirmação da Compra (`confirmPurchase`):**
    1.  Revalida todas as condições (saldo, disponibilidade do produto, etc.).
    2.  Debita o valor total das moedas de ouro do comprador (`userService.deductGold`).
    3.  **Não** credita o valor ao vendedor imediatamente (a liberação pode depender de outras etapas, como a confirmação de entrega).
    4.  Muda o status do produto para `SOLD`.
    5.  Cria um registro permanente na coleção `transactions` através do `TransactionService`.

## 4. `TransactionService` - O Livro-Razão

Gerencia o ciclo de vida de uma transação após ela ser criada.

* **Criação de Transação:**
    * Uma nova transação é sempre criada com o status inicial `PENDING`.
    * Ao ser criada, a transação concede pontos de experiência (XP) tanto para o comprador quanto para o vendedor.

* **Atualização de Status:**
    * O serviço impõe regras sobre quem pode alterar o status. Por exemplo:
        * Apenas o `vendedor` pode adicionar um código de rastreio e mudar o status para `SHIPPED`.
        * Apenas o `comprador` pode confirmar o recebimento (`confirmReceipt`), mudando o status para `COMPLETED`.
        * Apenas o `comprador` pode abrir uma `disputa`.

---