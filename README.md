# 🏦 BazaBank: Ecossistema Bancário Digital

<img width="995" height="677" alt="Baza Bank" src="https://github.com/user-attachments/assets/bcc01b43-f2f9-4240-8e62-57cd1324a0f0" />

## 🏗️ Estrutura do Repositório

* **`motor-core-java/`**: Microsserviço de backend central responsável pelas regras de negócio. Implementado em **Java + Spring Boot**, utilizando *Clean Architecture* e portas/adaptadores (Arquitetura Hexagonal).
* **`app-mobile-kotlin/`**: Aplicação cliente nativa para Android, desenvolvida em **Kotlin** com **Jetpack Compose**.
* **`.github/workflows/`**: Configurações de CI/CD via GitHub Actions.

---

## 🚀 Tecnologias e Padrões de Arquitetura

### ⚙️ Backend (`motor-core-java`)

O núcleo do sistema foi desenhado para ser à prova de falhas e evitar problemas comuns em sistemas financeiros:

* **Outbox Pattern:** Para garantir a consistência eventual e não perder eventos em caso de falha de rede. O sistema grava a intenção de notificação (`OutboxEntity`) na mesma transação atómica do PostgreSQL onde a transferência ocorre. Um *worker* dedicado (`OutboxRelayWorker`) lê esta tabela e publica no **Apache Kafka**.
* **Filtro de Idempotência:** Implementado via **Redis** (`IdempotenciaFilter.java`). Evita que a mesma transferência seja processada duas vezes devido a *retries* de rede acidentais (ex: clique duplo na app ou falha de timeout do cliente).
* **Gestão de Concorrência:** Tratamento rigoroso de *race conditions* no acesso aos saldos (`TransferenciaConcurrencyTest.java`).
* **Segurança:** Autenticação *Stateless* com JWT (`TokenService` e `SecurityFilter`), protegendo os *endpoints* da API.
* **Base de Dados:** **PostgreSQL** como fonte de verdade para Contas e Transações.

### 📱 Mobile (`app-mobile-kotlin`)

* **Linguagem:** Kotlin.
* **Interface (UI):** Jetpack Compose, garantindo um design responsivo, moderno e declarativo (`ui/theme`).
* **Comunicação:** Integração direta com a API do motor bancário (`BazaBankApi.kt`).
* **Gestão de Estado:** Utilização de ViewModels (`TransferenciaViewModel.kt`).

---

## 🛠️ Como Executar o Projeto Localmente

### Pré-requisitos

* Docker e Docker Compose
* Java 17 ou superior
* Maven
* Android Studio (para a aplicação móvel)

### 1. Iniciar a Infraestrutura (Backend)

Para levantar o PostgreSQL, Redis, Kafka e o Zookeeper, navegue até à pasta do backend e execute o Docker Compose:

cd motor-core-java
docker-compose up -d

*Isto irá disponibilizar as bases de dados e a mensageria nos portos padrão.*

### 2. Iniciar a Aplicação Spring Boot

Na mesma pasta (`motor-core-java`), compile e inicie a aplicação:

./mvnw clean install
./mvnw spring-boot:run

A API ficará disponível em `http://localhost:8080`. O ficheiro `data.sql` encarregar-se-á de criar dados iniciais (contas de teste) se aplicável.

### 3. Iniciar a Aplicação Android

1. Abra o **Android Studio**.
2. Faça *Open Project* e selecione a pasta `app-mobile-kotlin`.
3. Aguarde a sincronização do Gradle (definido no `build.gradle.kts`).
4. Selecione um emulador (ou dispositivo físico) e clique em **Run**.

---

## 📡 Endpoints Principais da API

O backend expõe uma REST API protegida. Os controladores principais incluem:

* **`AuthController`**: Geração de tokens JWT para autenticação.
* **`ContaController`**: Gestão e consulta de dados bancários (`ContaResponse`).
* **`TransferenciaController`**: Receção de pedidos de transferência (`TransferenciaRequest`). Protegido pelo `IdempotenciaFilter` para garantir unicidade da chave de transação.
* **`ExtratoController`**: Listagem do histórico de movimentos (`TransacaoResponse`).

*Nota: As falhas de negócio (ex: saldo insuficiente, conta inválida) são geridas globalmente pelo `GlobalExceptionHandler` e `TratadorDeErros`, devolvendo respostas HTTP padronizadas.*

---

## ☁️ Implementação (Deployment) em Kubernetes

O projeto está totalmente preparado para ser orquestrado em **Kubernetes**. Na raiz do `motor-core-java`, encontrará os manifestos necessários:

* **Aplicações:** `k8s-deployment.yml`, `k8s-service.yml`, `k8s-ingress.yml`
* **Infra:** `k8s-postgres.yml`, `k8s-redis.yml`, `k8s-kafka.yml`
* **Observabilidade:** `k8s-prometheus.yml`, `k8s-grafana.yml` (Para métricas e monitorização em tempo real).

Para aplicar a infraestrutura no seu cluster:

kubectl apply -f motor-core-java/k8s-*.yml

---

## 🧪 Testes

A arquitetura garante alta fiabilidade. Para executar os testes integrados e de concorrência:

cd motor-core-java
./mvnw test

**Testes de destaque:**

* `MotorTransferenciaIntegrationTest.java`: Valida o fluxo ponta-a-ponta (criação, transferência, outbox).
* `TransferenciaConcurrencyTest.java`: Garante que dezenas de transferências simultâneas na mesma conta não causam inconsistência no saldo.

---

## 🤝 Contribuições

Contribuições são bem-vindas! Sinta-se à vontade para abrir uma *Issue* ou enviar um *Pull Request*.
