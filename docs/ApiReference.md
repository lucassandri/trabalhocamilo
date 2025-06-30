# Referência da API: RPG Market

> Este documento detalha todos os endpoints da aplicação RPG Market. Ele foi gerado a partir de uma análise detalhada dos `Controllers` e `DTOs` do projeto.

## Visão Geral

* **Autenticação:** A maioria das rotas é protegida pelo Spring Security e requer uma sessão de usuário autenticada (login via formulário).
* **Permissões:** O acesso é controlado por papéis, principalmente `ROLE_AVENTUREIRO` (usuário padrão) e `ROLE_MESTRE` (administrador).
* **Tipos de Endpoint:**
    * **`[PÁGINA]`**: Retorna uma página HTML completa, renderizada no servidor.
    * **`[API/JSON]`**: Retorna dados no formato `application/json`, consumido por JavaScript (AJAX).
    * **`[ARQUIVO]`**: Retorna um arquivo binário, como uma imagem.
    * **`[REDIRECIONAMENTO]`**: Não retorna conteúdo, apenas redireciona o navegador para outra URL.

---

### 1. HomeController

Controlador simples que gerencia a rota raiz do site.

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /` | `[REDIRECIONAMENTO]`| Público | Redireciona o usuário da página inicial (`/`) para a página principal do mercado (`/mercado`). |

---

### 2. AuthController

Gerencia o fluxo de autenticação.

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /login` | `[PÁGINA]` | Público | Exibe a página de login. Aceita os query params `?error` (para falha no login) e `?logout` (para sucesso no logout) para exibir mensagens ao usuário. |
| `POST /login` | - | Público | Endpoint interno do Spring Security para processar o formulário de login. |

---

### 3. UserController

Gerencia todas as rotas relacionadas a um usuário autenticado. Prefixo: `/aventureiro`

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /registrar` | `[PÁGINA]` | Público | Exibe o formulário de registro de um novo aventureiro. |
| `POST /registrar` | `[REDIRECIONAMENTO]`| Público | Processa o registro. Espera um formulário `multipart/form-data` com os campos do modelo `User` e um arquivo opcional `profileImage`. |
| `GET /perfil` | `[PÁGINA]` | Aventureiro | Exibe a página de perfil completa do usuário logado, com estatísticas, itens ativos e vendidos. |
| `GET /inventario` | `[PÁGINA]` | Aventureiro | Mostra o inventário de itens que o usuário logado possui e está vendendo. |
| `GET /compras` | `[PÁGINA]` | Aventureiro | Exibe o histórico de transações de compra do usuário. |
| `GET /vendas` | `[PÁGINA]` | Aventureiro | Exibe o histórico de transações de venda do usuário. |
| `GET /senha` | `[PÁGINA]` | Aventureiro | Exibe o formulário para alteração de senha. |
| `POST /senha` | `[REDIRECIONAMENTO]`| Aventureiro | Processa a alteração de senha a partir de um formulário com os campos do `PasswordChangeRequest`. |
| `GET /editar-perfil` | `[PÁGINA]` | Aventureiro | Exibe o formulário para editar os dados do perfil (email, classe, imagem). |
| `POST /editar-perfil` | `[REDIRECIONAMENTO]`| Aventureiro | Processa a atualização do perfil. Recebe dados de `User` e um `MultipartFile` (`profileImage`). |
| `GET /enderecos` | `[PÁGINA]` | Aventureiro | Lista todos os endereços de entrega do usuário. |
| `GET /enderecos/novo`| `[PÁGINA]` | Aventureiro | Exibe o formulário para adicionar um novo endereço. |
| `POST /enderecos/novo`| `[REDIRECIONAMENTO]`| Aventureiro | Cria um novo endereço a partir de um formulário com os campos de `DeliveryAddress`. |
| `GET /enderecos/{id}/editar` | `[PÁGINA]` | Dono do Endereço | Exibe o formulário para editar um endereço existente. |
| `POST /enderecos/{id}/editar` | `[REDIRECIONAMENTO]`| Dono do Endereço | Processa a atualização de um endereço. |
| `POST /enderecos/{id}/deletar` | `[REDIRECIONAMENTO]`| Dono do Endereço | Remove um endereço de entrega específico. |
| `POST /enderecos/{id}/padrao` | `[REDIRECIONAMENTO]`| Dono do Endereço | Define um endereço de entrega como o padrão do usuário. |

---

### 4. MarketController

Controla a navegação e visualização do mercado público. Prefixo: `/mercado`

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /` | `[PÁGINA]` | Público | Página principal do mercado, exibindo seções de vendas diretas e leilões, filtrados pelas permissões de classe do personagem. |
| `GET /categoria/{category}` | `[PÁGINA]` | Público | Lista todos os produtos paginados de uma `ProductCategory` específica. |
| `GET /buscar` | `[PÁGINA]` | Público | Retorna a página de resultados de busca. Espera um query param opcional `keyword`. |
| `GET /masmorra-dos-leiloes`| `[PÁGINA]` | Público | Página dedicada a listar todos os leilões, com filtros avançados via query params (`category`, `rarity`, `minPrice`, `maxPrice`, `endingSoon`, `sort`). |
| `GET /vendas-diretas` | `[PÁGINA]` | Público | Página dedicada a listar todos os itens de venda direta, com filtros (`category`, `rarity`, `minPrice`, `maxPrice`). |
| `GET /ranking-dos-nobres`| `[PÁGINA]` | Público | Exibe um ranking público com os melhores vendedores e compradores. |

---

### 5. ProductController

Gerencia ações específicas de um item (visualizar, criar, editar, etc.). Prefixo: `/item`

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /{id}` | `[PÁGINA]` | Público | Exibe a página de detalhes de um produto específico. |
| `GET /novo` | `[PÁGINA]` | Aventureiro | Exibe o formulário para criar um novo item. |
| `POST /novo` | `[REDIRECIONAMENTO]`| Aventureiro | Processa a criação de um novo item a partir de um formulário `multipart/form-data` com os campos de `Product` e um arquivo `image`. |
| `GET /{id}/editar` | `[PÁGINA]` | Dono do Item | Exibe o formulário para editar um item que pertence ao usuário logado. |
| `POST /{id}/editar` | `[REDIRECIONAMENTO]`| Dono do Item | Processa a atualização de um item a partir de um formulário `multipart/form-data`. |
| `POST /{id}/excluir` | `[REDIRECIONAMENTO]`| Dono do Item | Remove um item do mercado. |
| `POST /{id}/comprar` | `[REDIRECIONAMENTO]`| Aventureiro | Inicia o fluxo de compra, redirecionando para a rota apropriada do `CheckoutController`. |
| `POST /{id}/lance` | `[REDIRECIONAMENTO]`| Aventureiro | Registra um lance em um item a partir de um formulário. |

---

### 6. BidController

Gerencia o fluxo de lances em leilões. Prefixo: `/bid` e `/lance`

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `POST /prepare` | `[API/JSON]`| Aventureiro | **(AJAX)** Valida um lance e retorna um JSON para preencher um modal de confirmação. |
| `POST /confirm` | `[REDIRECIONAMENTO]`| Aventureiro | Confirma e registra o lance no sistema após o usuário interagir com o modal. |
| `POST /dar` | `[REDIRECIONAMENTO]`| Aventureiro | Rota de compatibilidade simplificada para registrar um lance. |

---

### 7. CheckoutController

Controla todo o fluxo de finalização de compra e lances. Prefixo: `/checkout`

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /comprar/{productId}`| `[PÁGINA]` | Aventureiro | Exibe a página de revisão de compra para um item de **venda direta**. |
| `GET /comprar-agora/{productId}`| `[PÁGINA]` | Aventureiro | Exibe a página de revisão de compra para a opção "Comprar Agora" de um **leilão**. |
| `GET /lance/{productId}` | `[PÁGINA]` | Aventureiro | Exibe a página de revisão de lance para um **leilão**. |
| `POST /lance/{productId}`| `[REDIRECIONAMENTO]`| Aventureiro | Processa um lance em um leilão (rota alternativa). |
| `POST /confirmar` | `[REDIRECIONAMENTO]`| Aventureiro | Endpoint central que processa a compra final ou o lance a partir dos dados do `CheckoutRequest`. |
| `GET /sucesso/{transactionId}`| `[PÁGINA]` | Comprador | Exibe a página de sucesso após uma compra, mostrando os detalhes da transação. |
| `GET /enderecos` | `[API/JSON]`| Aventureiro | **(AJAX)** Retorna a lista de `DeliveryAddress` do usuário logado em formato JSON. |
| `GET /bid` | `[REDIRECIONAMENTO]`| Aventureiro | Rota de compatibilidade que redireciona para o fluxo de lance (`/checkout/lance/{productId}`). |

---

### 8. TransactionController

Gerencia o ciclo de vida de uma transação após a compra. Prefixo: `/transacao`

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /{id}` | `[PÁGINA]` | Comprador/Vendedor | Exibe os detalhes de uma transação da qual o usuário faz parte. |
| `POST /{id}/atualizar` | `[REDIRECIONAMENTO]`| Comprador/Vendedor | Atualiza o status de uma transação (ex: para `SHIPPED`). |
| `POST /{id}/rastreio` | `[REDIRECIONAMENTO]`| Vendedor | Adiciona um código de rastreio a uma transação. |
| `POST /{id}/confirmar-recebimento` | `[REDIRECIONAMENTO]`| Comprador | Permite ao comprador confirmar o recebimento do item. |
| `POST /{id}/abrir-disputa` | `[REDIRECIONAMENTO]`| Comprador | Abre uma disputa para a transação. |

---

### 9. AnalyticsController

Painel administrativo com dados e relatórios do sistema. Prefixo: `/mestre`

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /dashboard` | `[PÁGINA]` | `ROLE_MESTRE` | Painel principal de analytics. Aceita o query param `periodo` para filtrar por dias. |
| `GET /ranking-nobres`| `[PÁGINA]` | `ROLE_MESTRE` | Mostra rankings detalhados de usuários (compradores, vendedores, mais ricos). |
| `GET /relatorio-atividades` | `[PÁGINA]` | `ROLE_MESTRE` | Exibe um feed com as atividades mais recentes na plataforma. |

---

### 10. Endpoints de Utilitários

| Endpoint | Tipo | Permissão | Descrição |
| :--- | :--- | :--- | :--- |
| `GET /images/{fileName}`| `[ARQUIVO]` | Público | (`FileController`) Serve arquivos de imagem (produtos, perfis, etc.). |
| `GET /health` | `[API/JSON]`| Público | (`HealthController`) Endpoint de verificação de saúde. Retorna `{"status": "UP"}`. |
| `GET /error/*` | `[PÁGINA]` | Público | (`ErrorController`) Rotas que servem as páginas de erro padrão (403, 404, 500). |

> **Nota:** O `DebugController` contém várias rotas sob `/debug` que são destinadas apenas para desenvolvimento e foram omitidas desta documentação.