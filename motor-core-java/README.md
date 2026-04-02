# 🏦 Motor de Transferências Financeiras (BazaBank)

Um microsserviço de transferências bancárias construído com foco em **alta disponibilidade, resiliência e consistência de dados**. Este projeto simula o núcleo de um motor financeiro, resolvendo problemas clássicos de concorrência (*Double-Spending*) e implementando uma arquitetura orientada a eventos.

## 🚀 Tecnologias Utilizadas

* **Java 22** & **Spring Boot 4.0.5**
* **PostgreSQL** (Banco de dados relacional para persistência transacional)
* **Apache Kafka** (Mensageria e arquitetura Event-Driven)
* **Docker & Docker Compose** (Containerização da infraestrutura)
* **Testcontainers & JUnit 5** (Testes de integração com bancos efêmeros)
* **Clean Architecture** (Separação clara entre Domínio, Aplicação e Infraestrutura)

## 🧠 Decisões de Arquitetura

### 1. Clean Architecture & Portas e Adaptadores
O projeto foi estruturado para manter o *Core Domain* (Regras de Negócio) totalmente isolado de *frameworks* externos. O `RealizarTransferenciaUseCase` não conhece o Kafka ou o Spring Data JPA, comunicando-se apenas através de interfaces (Ports). Isso garante que a tecnologia de infraestrutura possa ser substituída no futuro sem alterar uma única linha de regra de negócio.

### 2. Prevenção de *Double-Spending* (Pessimistic Locking)
Num ambiente bancário, múltiplas transações podem tentar aceder à mesma conta no exato mesmo milissegundo. Para garantir a consistência ACID, foi implementado um **Pessimistic Write Lock** na base de dados (`@Lock(LockModeType.PESSIMISTIC_WRITE)`). Isto obriga as *threads* concorrentes a formarem uma fila, garantindo que o saldo nunca fique negativo, mesmo sob testes de stress.

### 3. Arquitetura Orientada a Eventos (Kafka)
Para garantir o desacoplamento de serviços paralelos (como o envio de comprovativos por e-mail ou SMS), o motor publica um `TransferenciaRealizadaEvent` no Apache Kafka. 
Foi implementado um consumidor resiliente (`NotificacaoTransferenciaConsumer`) que processa estes eventos de forma assíncrona, utilizando serialização segura via `ObjectMapper` e tratamento de mensagens para evitar falhas em cascata.

## ⚙️ Como Executar o Projeto

1. **Subir a Infraestrutura (Banco e Mensageria):**
   ```bash
   docker-compose up -d
   ```
2. **Rodar a Aplicação Spring Boot:**
   ```bash
   ./mvnw spring-boot:run
   ```

A API estará disponível em `http://localhost:8080`. O banco de dados já inicializa com duas contas de teste (uma com saldo de R$ 5000.00 e outra zerada).

## 🧪 Testes de Integração
A qualidade do motor foi validada através de Testes de Integração automatizados utilizando **Testcontainers**. Durante a execução dos testes, contentores Docker descartáveis do PostgreSQL são levantados para validar o comportamento de múltiplas requisições simultâneas atacando a mesma conta (garantindo o funcionamento prático do Lock Pessimista).

Para rodar os testes:
```bash
./mvnw clean test
```
