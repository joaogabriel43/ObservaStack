# CLAUDE.md — Fonte de Verdade do ObservaStack

> **ADR-7**: Este documento é a fonte de verdade do projeto. Toda decisão arquitetural relevante deve ser registrada aqui. Antes de iniciar qualquer tarefa, leia este arquivo na íntegra.

---

## 1. Visão Geral do Projeto

**ObservaStack** é uma plataforma de observabilidade para aplicações Spring Boot. Ela coleta, armazena e exibe dados de traces distribuídos, métricas JVM via JFR (Java Flight Recorder) e permite a visualização de flame graphs no frontend.

### Stack Tecnológica

| Camada        | Tecnologia                                  |
|---------------|---------------------------------------------|
| Linguagem     | Java 21                                     |
| Framework     | Spring Boot 3.2.x                           |
| Banco de dados| PostgreSQL (com JSONB para atributos)        |
| Migrations    | Flyway                                      |
| Observabilidade | Spring Boot Actuator                      |
| Build         | Maven (mono-repo multi-módulo)              |
| Testes        | JUnit 5, Testcontainers, jqwik              |
| CI/CD         | GitHub Actions                              |
| Frontend (futuro) | Angular + d3-flame-graph                |

---

## 2. Estrutura de Módulos (ADR-1)

O projeto é um **mono-repo Maven com 3 módulos principais**:

```
ObservaStack/
├── pom.xml                            ← Parent POM
├── observastack-server/               ← Servidor de coleta e API (Spring Boot)
├── observastack-sdk/                  ← SDK Java puro (sem Spring)
│   └── observastack-sdk-spring-boot-starter/ ← Autoconfiguração Spring
└── observastack-demo-app/             ← App de demonstração (Spring Boot)
```

### Responsabilidades dos Módulos

| Módulo | Responsabilidade |
|--------|-----------------|
| `observastack-server` | Recebe traces/spans via HTTP, persiste no PostgreSQL, expõe API de consulta |
| `observastack-sdk` | Instrumentação Java pura: TraceContext (ThreadLocal), Sampler, propagação W3C |
| `observastack-sdk-spring-boot-starter` | Autoconfiguração Spring Boot para o SDK |
| `observastack-demo-app` | App de demonstração com endpoints reais para testar o SDK |

---

## 3. Decisões Arquiteturais (ADRs)

### ADR-1: Mono-repo com 3 Módulos Maven

**Decisão:** Utilizar um único repositório Git com Maven multi-módulo.

**Contexto:** Facilita o versionamento conjunto e a referência entre módulos durante o desenvolvimento inicial.

**Consequências:** O `observastack-sdk` deve ser publicado no Maven Central futuramente para que usuários externos possam consumi-lo sem clonar o mono-repo.

---

### ADR-2: Traces no PostgreSQL com Tabelas Normalizadas

**Decisão:** Armazenar traces e spans em tabelas normalizadas (`traces` e `spans`) com coluna `attributes JSONB`.

**Contexto:** JSONB permite flexibilidade nos atributos de spans sem schema rígido, enquanto as colunas tipadas (`trace_id`, `duration_ms`, `status`) permitem índices eficientes.

**Schema:**
```sql
-- traces: cabeçalho de um trace completo
traces (trace_id PK, service_name, started_at, ended_at, duration_ms, status)

-- spans: operações individuais dentro de um trace
spans (span_id PK, trace_id FK, parent_span_id, operation_name,
       started_at, ended_at, duration_ms, status, attributes JSONB)
```

**Índices obrigatórios:** `trace_id`, `duration_ms`, `status` em ambas as tabelas. GIN index em `spans.attributes`.

---

### ADR-3: JFR via HTTP Push Assíncrono

**Decisão:** O SDK coletará dados JFR (Java Flight Recorder) e os enviará ao server via HTTP push assíncrono, com buffer circular gerenciado pelo Resilience4j.

**Status:** Estrutura preparada no Sprint 1. **Implementação a partir do Sprint 2.**

**Consequências:** O SDK core não deve bloquear a thread da aplicação durante o envio de dados JFR.

---

### ADR-4: W3C TraceContext sem Spring (ThreadLocal)

**Decisão:** A propagação de contexto de trace no SDK utiliza `ThreadLocal` puro (sem dependência do Spring), seguindo o padrão W3C TraceContext.

**Contexto:** Permite que o SDK core seja utilizado em qualquer aplicação Java, independente de framework.

**Implementação:**
```java
// TraceContext armazenado em ThreadLocal
// Formato W3C: traceparent: 00-{traceId}-{spanId}-{flags}
```

---

### ADR-5: Flame Graph no Frontend com d3-flame-graph

**Decisão:** A visualização de flame graphs será implementada no frontend Angular utilizando a biblioteca `d3-flame-graph`.

**Status:** Fora do escopo do Sprint 1. Implementar a partir do Sprint de Frontend.

---

### ADR-6: Sampling no SDK Head-Based Determinístico

**Decisão:** O SDK implementará sampling no início de cada trace (head-based), com taxa configurável. A decisão de sample é determinística baseada no `traceId`.

**Contexto:** Head-based sampling garante que um trace seja amostrado ou descartado integralmente, sem traces parciais.

**Interface:**
```java
public interface Sampler {
    boolean shouldSample(String traceId, double samplingRate);
}
```

---

### ADR-7: CLAUDE.md como Fonte de Verdade

**Decisão:** Este arquivo é a referência principal para todos os agentes e desenvolvedores. Deve ser atualizado a cada sprint com novas decisões, padrões e erros conhecidos.

**Regra:** Toda ADR nova começa com status `[PROPOSTA]` e só passa a `[APROVADA]` após revisão.

---

## 4. Padrões do Projeto

### 4.1 Clean Architecture (apenas `observastack-server`)

```
io.observastack.server/
├── domain/           ← Entidades, Value Objects, Ports (interfaces de repositório)
│   ├── model/        ← Trace, Span, TraceId, SpanId
│   └── port/         ← TraceRepository (interface)
├── application/      ← Use Cases, Services de aplicação
│   └── usecase/      ← ReceiveTraceUseCase, QueryTraceUseCase (esqueleto)
├── infrastructure/   ← Adapters: JPA, HTTP clients, configs
│   ├── persistence/  ← JPA Entities, Repositories Spring Data
│   └── config/       ← Spring @Configuration classes
└── presentation/     ← Controllers REST, DTOs de entrada/saída
    ├── controller/
    └── dto/
```

**Regras de dependência:** `domain` não conhece nenhuma outra camada. `application` conhece apenas `domain`. `infrastructure` e `presentation` conhecem `application` e `domain`.

### 4.2 Flyway

- Todas as migrations ficam em `src/main/resources/db/migration/`
- Nomenclatura obrigatória: `V{número}__{descrição_snake_case}.sql`
- **Nunca** altere uma migration já executada em produção; crie uma nova
- Migrations devem ser idempotentes onde possível (`CREATE TABLE IF NOT EXISTS`)

### 4.3 TDD (Test-Driven Development)

- Todo novo use case deve ter testes unitários **antes** da implementação
- Integration tests usam **Testcontainers** com PostgreSQL real
- Property-based tests usam **jqwik** (`@Property`, `@ForAll`)
- Cobertura mínima de 80% nas camadas `domain` e `application`

### 4.4 Commits Semânticos (Conventional Commits)

| Prefixo | Uso |
|---------|-----|
| `feat:` | Nova funcionalidade |
| `fix:` | Correção de bug |
| `chore:` | Manutenção, dependências, CI |
| `docs:` | Documentação |
| `test:` | Adição ou correção de testes |
| `refactor:` | Refatoração sem mudança de comportamento |
| `perf:` | Melhoria de performance |
| `style:` | Formatação, sem mudança de lógica |

Exemplo: `feat(server): add Flyway migration V1 for traces table`

### 4.5 Qualidade de Código

- **p6spy**: Ativo no `observastack-server` para logging de queries SQL e detecção de N+1
- **SpotBugs**: Executa no CI/CD (`mvn spotbugs:check`)
- **Actuator**: Habilitado em `server` e `demo-app`; endpoints `/actuator/health` e `/actuator/info` sempre expostos

---

## 5. Configuração de Ambiente

### Pré-requisitos

- Java 21 (recomendado: Eclipse Temurin)
- Maven 3.9+
- Docker (para Testcontainers)
- PostgreSQL 16+ (produção/desenvolvimento local)

### Build Completo

```bash
# Na raiz do projeto
mvn clean install

# Apenas testes
mvn test

# Pular testes de integração
mvn clean install -DskipITs
```

### Executar o Demo App

```bash
cd observastack-demo-app
mvn spring-boot:run
# Acesse: http://localhost:8081/actuator/health
```

---

## 6. CI/CD Pipeline

O workflow `.github/workflows/ci.yml` executa em todo push e PR para `main`:

| Job | Ferramenta | Objetivo |
|-----|-----------|----------|
| `build-and-test` | Maven + Testcontainers | Compilar e testar todos os módulos |
| `spotbugs` | SpotBugs Maven Plugin | SAST — análise estática Java |
| `gitleaks` | gitleaks/gitleaks-action | Detecção de secrets no código |
| `trivy` | aquasecurity/trivy-action | Scan de vulnerabilidades (SARIF) |
| `owasp-dep-check` | OWASP Dependency-Check | Auditoria de dependências (CVEs) |

---

## 7. Erros Conhecidos e Workarounds

> Esta seção será atualizada conforme bugs e limitações são encontrados.

*(Nenhum erro conhecido no Sprint 1 inicial)*

---

## 8. Histórico de Sprints

| Sprint | Objetivo | Status |
|--------|----------|--------|
| Sprint 1 | Fundação do mono-repo, modelos de dados, CI/CD | ✅ Concluído |

---

*Última atualização: Sprint 1 — Setup Inicial*
