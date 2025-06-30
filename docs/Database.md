# Documentação do Banco de Dados: RPG Market

> Este documento descreve o esquema das coleções principais utilizadas no banco de dados MongoDB do projeto.
> 
> *Última atualização: 29 de Junho de 2025*

## Visão Geral

A base de dados do RPG Market é modelada em torno de cinco coleções principais que interagem para criar a experiência do mercado. As relações entre elas são mantidas principalmente através de referências (`DBRef`), garantindo a integridade dos dados enquanto se mantém a flexibilidade de um esquema NoSQL.

---

### Coleção: `users`

Armazena todas as informações dos usuários da plataforma, desde dados de login até seus atributos de RPG. É a entidade central de identidade.

| Campo | Tipo de Dado | Índice | Descrição |
| :--- | :--- | :--- | :--- |
| `_id` | `ObjectId` | **Único (PK)** | Identificador único do documento, gerado pelo MongoDB. |
| `username` | `String` | **Único** | Nome de usuário único utilizado para login e identificação pública. |
| `email` | `String` | **Único** | Endereço de e-mail único, usado para comunicação e login. |
| `password` | `String` | - | Senha do usuário, armazenada de forma criptografada (hash). |
| `characterClass`| `String` | - | Classe do personagem no universo RPG (ex: "Guerreiro", "Mago"). |
| `level` | `Integer` | - | Nível atual do personagem do usuário. |
| `experience` | `Integer` | - | Pontos de experiência acumulados pelo usuário. |
| `goldCoins` | `Decimal128` | - | Saldo de moedas de ouro, a moeda principal do mercado. |
| `role` | `String (Enum)`| - | Papel do usuário no sistema (`ROLE_AVENTUREIRO`, `ROLE_MESTRE`, `ROLE_ADMIN`). |
| `profileImageUrl`| `String` | - | URL da imagem de perfil hospedada. |

---

### Coleção: `products`

Contém todos os itens que estão à venda ou em leilão. É a coleção mais rica em detalhes, descrevendo cada aspecto de um item de RPG.

| Campo | Tipo de Dado | Índice | Descrição |
| :--- | :--- | :--- | :--- |
| `_id` | `ObjectId` | **Único (PK)** | Identificador único do produto. |
| `name` | `String` | - | Nome do item (ex: "Espada Longa de Fogo"). |
| `description` | `String` | - | Descrição textual do item, incluindo seus atributos e história. |
| `price` | `Decimal128` | - | Preço base para venda direta ou lance inicial em leilão. |
| `category` | `String (Enum)` | Indexado | Categoria do produto (`ARMAS`, `POCOES_ELIXIRES`, etc.). |
| `status` | `String (Enum)` | Indexado | Status atual no mercado (`AVAILABLE`, `SOLD`, `AUCTION_ACTIVE`, etc.). |
| `type` | `String (Enum)` | Indexado | Tipo de venda (`DIRECT_SALE` ou `AUCTION`). |
| `imageUrl` | `String` | - | URL da imagem de exibição do produto. |
| `seller` | `DBRef -> users` | Indexado | Referência ao `User` vendedor. |
| `bids` | `Array<DBRef -> bids>`| - | Lista de referências a todos os lances feitos neste produto. |
| `createdAt` | `DateTime` | - | Data e hora em que o produto foi listado. |
| `auctionEndDate`| `DateTime` | - | Data e hora de término do leilão (se aplicável). |
| `buyNowPrice` | `Decimal128` | - | Preço opcional para compra imediata em um leilão. |
| `minBidIncrement`| `Decimal128` | - | O incremento mínimo obrigatório para um novo lance. |
| `rarity` | `String (Enum)` | - | Raridade do item (`COMUM`, `RARO`, `LENDARIO`, etc.). |
| `levelRequired` | `Integer` | - | Nível mínimo que um `User` precisa ter para usar o item. |
| `magicProperties`| `Array<String (Enum)>`| - | Conjunto de propriedades mágicas (`FOGO`, `CURA`, etc.). |
| `quantity` | `Integer` | - | Quantidade de itens disponíveis no estoque (para itens empilháveis). |
| `weight` | `Decimal128` | - | O peso do item, que pode influenciar a capacidade de carga. |
| `durability` | `Integer` | - | Durabilidade atual do item, geralmente em uma escala de 0 a 100. |
| `history` | `String` | - | Campo de texto para a história, proveniência ou "lore" do item. |
| `experienceGained`| `Integer` | - | Quantidade de experiência concedida ao interagir com o item. |

---

### Coleção: `bids`

Registra cada lance individual feito por um usuário em um produto de leilão.

| Campo | Tipo de Dado | Índice | Descrição |
| :--- | :--- | :--- | :--- |
| `_id` | `ObjectId` | **Único (PK)** | Identificador único do lance. |
| `product` | `DBRef -> products`| Indexado | Referência ao `Product` que está sendo leiloado. |
| `bidder` | `DBRef -> users` | Indexado | Referência ao `User` que fez o lance. |
| `amount` | `Decimal128` | - | O valor do lance em moedas de ouro. |
| `bidTime` | `DateTime` | - | Data e hora exatas em que o lance foi feito. |
| `winning` | `Boolean` | - | `true` se este é, no momento, o lance mais alto do leilão. |

---

### Coleção: `transactions`

Armazena o histórico de todas as compras e vendas finalizadas, servindo como um livro-razão do mercado.

| Campo | Tipo de Dado | Índice | Descrição |
| :--- | :--- | :--- | :--- |
| `_id` | `ObjectId` | **Único (PK)** | Identificador único da transação. |
| `product` | `DBRef -> products`| Indexado | Referência ao `Product` que foi negociado. |
| `buyer` | `DBRef -> users` | Indexado | Referência ao `User` comprador. |
| `seller` | `DBRef -> users` | Indexado | Referência ao `User` vendedor. |
| `amount` | `Decimal128` | - | Valor final da transação em moedas de ouro. |
| `status` | `String (Enum)` | Indexado | Status da transação (`COMPLETED`, `CANCELED`, `SHIPPED`, etc.). |
| `deliveryAddress`| `DBRef -> delivery_addresses` | - | Referência ao endereço para o qual o item foi enviado. |
| `createdAt` | `DateTime` | - | Data e hora em que a transação foi iniciada. |
| `completedAt` | `DateTime` | - | Data e hora em que a transação foi concluída. |
| `trackingCode` | `String` | - | Código de rastreio para a entrega do item, se aplicável. |
| `notes` | `String` | - | Observações ou notas adicionais sobre a compra. |

---

### Coleção: `delivery_addresses`

Guarda os endereços de entrega cadastrados pelos usuários.

| Campo | Tipo de Dado | Índice | Descrição |
| :--- | :--- | :--- | :--- |
| `_id` | `ObjectId` | **Único (PK)** | Identificador único do endereço. |
| `userId` | `String` | Indexado | O `_id` do `User` a quem este endereço pertence. |
| `street` | `String` | - | Nome da rua. |
| `number`| `String` | - | Número do imóvel. |
| `complement` | `String` | - | Complemento (ex: "Apto 101", "Torre do Mago"). |
| `district` | `String` | - | Bairro ou distrito. |
| `city` | `String` | - | Cidade. |
| `state` | `String` | - | Estado ou província. |
| `postalCode`| `String` | - | Código Postal (CEP). |
| `isDefault`| `Boolean` | - | `true` se este é o endereço de entrega padrão do usuário. |
| `description` | `String` | - | Um rótulo para o endereço (ex: "Casa", "Trabalho"). |

---

## Relações Entre Coleções

As conexões entre os dados são feitas da seguinte forma:

* **`Product` <-> `User`**: Um `User` (`seller`) é referenciado por múltiplos `Product`.
* **`Product` <-> `Bid`**: Um `Product` (`bids`) contém um array de referências a múltiplos `Bid`.
* **`Bid` <-> `User`**: Um `Bid` (`bidder`) referencia o `User` que fez o lance.
* **`Transaction` <-> `User`**: Uma `Transaction` referencia tanto o `buyer` quanto o `seller`.
* **`Transaction` <-> `Product`**: Uma `Transaction` referencia o `Product` negociado.
* **`Transaction` <-> `DeliveryAddress`**: Uma `Transaction` pode referenciar um `DeliveryAddress`.

## Enumerações (Enums)

Os seguintes conjuntos de valores fixos são usados para garantir a consistência dos dados em todo o sistema:

* **Enums de Produto**: `ProductCategory`, `ProductStatus`, `ProductType`, `ItemRarity`, `MagicProperty`.
* **Enums de Usuário**: `UserRole`.
* **Enums de Transação**: `TransactionStatus`.