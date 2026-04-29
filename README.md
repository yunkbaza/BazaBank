# 🏦 BazaBank - Motor de Transferências Core



![Java](https://img.shields.io/badge/Java-21-orange.svg)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)

![Kafka](https://img.shields.io/badge/Kafka-Event%20Driven-black.svg)

![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-blue.svg)



O **BazaBank** é um motor de transferências financeiras de alta disponibilidade. Foi desenhado utilizando os princípios da **Arquitetura Hexagonal (Ports and Adapters)**, garantindo um forte isolamento entre a lógica de negócio central (Domain) e os detalhes de infraestrutura (Base de dados, Mensageria, APIs REST).## 🚀 Arquitetura e Tecnologias* **Linguagem/Framework:** Java 21 + Spring Boot 3.4.0* **Base de Dados:** PostgreSQL (Persistência) & Redis (Cache e Controlo de Sessões/Idempotência)* **Mensageria Assíncrona:** Apache Kafka para processamento orientado a eventos (Event-Driven).* **Observabilidade:** Métricas exportadas via Micrometer para o Prometheus e visualização em Dashboards Grafana.* **Orquestração:** Totalmente conteinerizado e gerido via manifests Kubernetes (`/k8s`).## 🧠 Architecture Decision Records (ADRs)



Como especialistas, tomamos decisões baseadas em compromissos técnicos (trade-offs). Abaixo estão os princípios de design aplicados neste motor:1. **Shift-Left Testing (Testcontainers):** Em vez de utilizarmos base de dados em memória (como o H2) para testes, utilizamos `Testcontainers` para levantar instâncias reais de PostgreSQL e Kafka em contentores Docker durante a fase de CI (GitHub Actions). Isto garante fidelidade absoluta com o ambiente de produção.2. **Proteção de Saldo (Pessimistic Locking):** *(Em implementação)* Para evitar *Race Conditions* quando múltiplas transações tentam debitar o mesmo saldo concorrentemente, o motor utiliza Locks a nível de base de dados para garantir o modelo ACID em escala financeira.3. **Segurança (JWT):** A camada web está protegida pelo Spring Security, validando Tokens JWT emitidos externamente, delegando a autorização para uma camada segura.4. **Resiliência e Tolerância a Falhas:** *(Em implementação)* Garantia de entrega com Transactional Outbox Pattern e gestão de eventos falhos via Dead Letter Queues (DLQ).## 🛠️ Como Executar Localmente### Pré-requisitos* Docker e Docker Compose instalados.* Java 21+ instalado.### 1. Iniciar a Infraestrutura (Bases de dados, Kafka, Observabilidade)

O projeto contém um `docker-compose.yml` com toda a infraestrutura adjacente.```bash

docker-compose up -d

2. Executar a Aplicação

Bash



./mvnw spring-boot:run

A API estará disponível em http://localhost:8080

📊 Observabilidade em Produção

Se executar a stack em Kubernetes (via os manifests /k8s-*), os dados do Prometheus são injetados automaticamente no Grafana. Aceda ao Grafana (Porta 3000) e importe o nosso Dashboard para monitorizar o uso de Heap, GC e conexões de base de dados (HikariCP) em tempo real.



---



Com estes dois passos concluídos, a sua base de engenharia de software "brilha" e mostra maturidade no ciclo de vida do código. 



Quando estiver pronto para aplicar o código no Java, por onde quer começar? Sugiro atacarmos a **Idempotência no Redis** ou o **Pessimistic Locking no Banco de Dados** para blindarmos as transferências contra concorrência!