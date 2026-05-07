# 🏦 BazaBank: Ecossistema Bancário Digital

![BazaBank Logo](app-mobile-kotlin/app/src/main/res/drawable/logo_baza.png)

O **BazaBank** é uma plataforma bancária completa de alta performance, projetada com foco em resiliência, escalabilidade e experiência do utilizador offline-first. O sistema utiliza uma arquitetura de microserviços e eventos para garantir a integridade de transações críticas em tempo real.

---

## 🏗️ Arquitetura e Design Patterns

O projeto foi construído seguindo os mais rigorosos padrões da engenharia de software moderna:

* **Clean Architecture & Hexagonal (Ports & Adapters):** Isolamento total da lógica de negócio das tecnologias externas.
* **Event-Driven Architecture (EDA):** Processamento assíncrono de transferências utilizando **Apache Kafka**.
* **Transactional Outbox Pattern:** Garante a consistência entre a base de dados e o broker de mensagens.
* **Pessimistic Locking:** Proteção contra Race Conditions em operações de saldo simultâneas.
* **Offline-First (Mobile):** Cache local resiliente com **Room Database**.

---

## 🔥 Funcionalidades Sênior

### 🛡️ Resiliência (Circuit Breaker)
Implementação de **Resilience4j** no endpoint de extrato para proteger o backend contra sobrecargas e falhas em cascata no banco de dados.

### 📈 Performance & Paginação
API preparada para lidar com grandes volumes de dados através de paginação dinâmica no Spring Data JPA.

### 🔐 Segurança Enterprise
Autenticação baseada em **JWT (JSON Web Token)** com filtros de segurança customizados e encriptação de senhas via BCrypt.

### 📱 Experiência Mobile
Interface reativa desenvolvida inteiramente em **Jetpack Compose**, com sistema de Splash Screen oficial e gestão de estado via Flow.



## 🛠️ Tech Stack

**Backend:**
- Java 17 / Spring Boot 3
- PostgreSQL (Base de dados relacional)
- Apache Kafka (Mensageria)
- Resilience4j (Circuit Breaker)
- Docker & Kubernetes (Orquestração)

**Mobile:**
- Kotlin / Jetpack Compose
- Room Database (Cache local)
- Retrofit (Comunicação com API)
- KSP (Kotlin Symbol Processing)

---

## 🚀 Como Executar

### Backend (Docker Compose)
cd motor-core-java
docker-compose up -d


