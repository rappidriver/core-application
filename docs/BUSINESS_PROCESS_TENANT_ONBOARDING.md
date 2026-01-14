# Processo de Negócio: Onboarding de Tenant no RappiDrive

**Versão**: 1.0  
**Data**: 14 de janeiro de 2026  
**Status**: ✅ Documentado

---

## 1. Visão Geral

RappiDrive é uma plataforma **white-label** de mobilidade urbana que suporta múltiplos operadores (tenants) em paralelo. Cada tenant é uma empresa/marca independente com seus próprios:

- 🚗 Motoristas (drivers)
- 👤 Passageiros (passengers)
- 🚕 Veículos (vehicles)
- 🛣️ Corridas (trips)
- 💰 Pagamentos (payments)
- ⭐ Avaliações (ratings)
- 📲 Notificações (notifications)

**Objetivo**: Permite que empresas de ride-hailing (Uber, 99, Loggi Transporte, etc.) operem na plataforma com dados completamente isolados.

---

## 2. Modelo de Dados: Multi-Tenancy

### 2.1 Estrutura de Isolamento

```
┌───────────────────────────────────────────────────────┐
│                  RappiDrive Platform                  │
├───────────────────────────────────────────────────────┤
│                                                       │
│  ┌──────────────────┐  ┌──────────────────────────┐   │
│  │   Tenant A       │  │     Tenant B             │   │
│  │  (ex: Uber)      │  │  (ex: 99 Táxi)           │   │
│  ├──────────────────┤  ├──────────────────────────┤   │
│  │ Drivers: 5000    │  │ Drivers: 3000            │   │
│  │ Passengers: 50K  │  │ Passengers: 20K          │   │
│  │ Trips/dia: 8000  │  │ Trips/dia: 5000          │   │
│  └──────────────────┘  └──────────────────────────┘   │
│                                                       │
│  ┌──────────────────┐  ┌──────────────────────────┐   │
│  │   Tenant C       │  │     Tenant N             │   │
│  │  (ex: Loggi)     │  │  (ex: Outra marca)       │   │
│  ├──────────────────┤  ├──────────────────────────┤   │
│  │ Drivers: 2000    │  │ Drivers: ...             │   │
│  │ Passengers: 10K  │  │ Passengers: ...          │   │
│  │ Trips/dia: 3000  │  │ Trips/dia: ...           │   │
│  └──────────────────┘  └──────────────────────────┘   │
│                                                       │
└───────────────────────────────────────────────────────┘
```

### 2.2 Isolamento por TenantId

Toda entidade (Driver, Passenger, Trip, etc.) carrega um `TenantId`:

```sql
-- Exemplo: tabela de motoristas
CREATE TABLE drivers (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,  -- ← Chave de isolamento
    full_name VARCHAR(255),
    email VARCHAR(255),
    status VARCHAR(20),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

-- Queries SEMPRE incluem tenant_id
SELECT * FROM drivers 
WHERE tenant_id = ? AND status = 'ACTIVE'
```

**Garantia**: Um motorista de Tenant A NUNCA acessa dados de Tenant B, mesmo que tenha acesso ao banco.

---

## 3. Processo de Cadastro de Tenant

### 3.1 Fase 1: Criação da Tenant no Sistema

**Responsável**: Administrador do RappiDrive ou Dashboard Admin

**Entrada**:
```json
{
  "name": "Uber Brasil",
  "slug": "uber-br",
  "config": {
    "currencyCode": "BRL",
    "timezone": "America/Sao_Paulo",
    "minDriversPerZone": 5,
    "maxWaitTime": 15,
    "baseFareUSD": 2.50
  }
}
```

**Saída (sucesso)**:
```json
{
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ACTIVE",
  "createdAt": "2026-01-14T00:00:00Z"
}
```

**SQL executado (V1__Initial_schema.sql)**:
```sql
INSERT INTO tenants (id, name, slug, active, config, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'Uber Brasil',
  'uber-br',
  true,
  '{"currencyCode":"BRL","timezone":"America/Sao_Paulo","minDriversPerZone":5,"maxWaitTime":15,"baseFareUSD":2.50}',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
```

**Validações**:
- ❌ `name` não pode ser vazio
- ❌ `slug` deve ser único (índice `UNIQUE` em `tenants(slug)`)
- ❌ `slug` deve ser URL-safe (alfanumérico + hífen)

---

### 3.2 Fase 2: Configuração Inicial de Zonas Geográficas

**Responsável**: Equipe de Operações da Tenant

**O que configurar**:
- Regiões de operação (ex: São Paulo, Rio de Janeiro)
- Coordenadas de cobertura (latitude/longitude)
- Prioridade de zonas (zona premium vs. zona normal)
- Horários de operação

**Tabelas envolvidas**:
```sql
-- Futuro: adicionar tabela de zones
CREATE TABLE IF NOT EXISTS zones (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    center_lat DOUBLE PRECISION NOT NULL,
    center_lon DOUBLE PRECISION NOT NULL,
    radius_km DOUBLE PRECISION NOT NULL,
    status VARCHAR(20),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
```

---

### 3.3 Fase 3: Cadastro de Motoristas

**Fluxo de um motorista**:

```
1. Motorista acessa app/web
   ↓
2. Seleciona Tenant (ex: "Uber")
   ↓
3. Preenche dados: nome, CPF, CNH, telefone
   ↓
4. Envía para API POST /api/v1/drivers
   {
     "tenantId": "550e8400-...",
     "fullName": "João Silva",
     "email": "joao@email.com",
     "cpf": "12345678901",
     "phone": "+5511999999999",
     "driverLicenseNumber": "ABC123456",
     "driverLicenseCategory": "B",
     "driverLicenseIssueDate": "2020-01-01",
     "driverLicenseExpirationDate": "2030-01-01"
   }
   ↓
5. Sistema valida dados (CPF, CNH)
   ↓
6. Sistema cria entrada no banco:
   INSERT INTO drivers (..., tenant_id, ...)
   ↓
7. Motorista entra em modo INACTIVE (aguarda aprovação)
   ↓
8. Administrador aprova via endpoint:
   POST /api/v1/drivers/{id}/approve
   ↓
9. Status muda para ACTIVE (pronto para receber corridas)
```

**Modelo de dados (domain/entities/Driver.java)**:
```java
public class Driver {
    private final UUID id;
    private final TenantId tenantId;        // ← Isolamento
    private final String fullName;
    private final Email email;
    private final CPF cpf;                   // Validado
    private final Phone phone;
    private final DriverLicense license;    // CNH completa
    private DriverStatus status;            // INACTIVE → ACTIVE
    private Location currentLocation;       // Latitude/Longitude
    private LocalDateTime createdAt;
}
```

---

### 3.4 Fase 4: Cadastro de Passageiros

**Fluxo de um passageiro**:

```
1. Usuário acessa app/web
   ↓
2. Seleciona Tenant (ex: "Uber")
   ↓
3. Faz login com email/senha ou social
   ↓
4. Preenche perfil: nome, telefone, endereço padrão
   ↓
5. Sistema cria entrada no banco:
   INSERT INTO passengers (tenant_id, ...)
   ↓
6. Status: ACTIVE (pronto para solicitar corridas)
```

**Modelo de dados (domain/entities/Passenger.java)**:
```java
public class Passenger {
    private final UUID id;
    private final TenantId tenantId;        // ← Isolamento
    private final String fullName;
    private final Email email;
    private final Phone phone;
    private PassengerStatus status;        // INACTIVE / ACTIVE / BLOCKED
    private LocalDateTime createdAt;
}
```

---

### 3.5 Fase 5: Cadastro de Veículos

**Fluxo de um veículo**:

```
1. Motorista (já aprovado) entra no app
   ↓
2. Acessa "Meus Veículos"
   ↓
3. Clica "Adicionar Veículo"
   ↓
4. Preenche dados: placa, marca, modelo, ano, cor
   ↓
5. Envia para API POST /api/v1/vehicles
   {
     "tenantId": "550e8400-...",
     "driverId": "650e8400-...",
     "licensePlate": "ABC-1234",
     "brand": "Toyota",
     "model": "Corolla",
     "year": 2023,
     "color": "Preto"
   }
   ↓
6. Sistema valida placa (formato, duplicação)
   ↓
7. Sistema cria entrada no banco:
   INSERT INTO vehicles (tenant_id, driver_id, ...)
   ↓
8. Veículo ativado (pronto para receber corridas)
```

**Modelo de dados (domain/entities/Vehicle.java)**:
```java
public class Vehicle {
    private final UUID id;
    private final TenantId tenantId;        // ← Isolamento
    private final DriverId driverId;
    private final LicensePlate licensePlate; // ABC-1234 (validado)
    private final String brand;
    private final String model;
    private final VehicleYear year;
    private final String color;
    private VehicleStatus status;          // INACTIVE / ACTIVE
}
```

---

## 4. Estrutura de Dados da Tenant

### 4.1 Tabela Central: `tenants`

```sql
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,            -- Nome da empresa (ex: "Uber Brasil")
    slug VARCHAR(100) NOT NULL UNIQUE,     -- Identificador URL (ex: "uber-br")
    active BOOLEAN NOT NULL DEFAULT true,  -- Ativa ou desativada
    config JSONB,                          -- Configurações personalizadas
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para performance
CREATE INDEX idx_tenants_slug ON tenants(slug);        -- Lookup por slug
CREATE INDEX idx_tenants_active ON tenants(active);    -- Filtro por status
```

### 4.2 Campo `config` (JSONB)

```json
{
  "currencyCode": "BRL",
  "timezone": "America/Sao_Paulo",
  "minDriversPerZone": 5,
  "maxWaitTime": 15,
  "baseFareUSD": 2.50,
  "cancellationPolicyPassengerRequested": {
    "freeWindow": 300,           // 5 minutos
    "fee": 5.00                  // R$ 5,00
  },
  "cancellationPolicyPassengerAssigned": {
    "freeWindow": 120,           // 2 minutos
    "fee": 8.00                  // R$ 8,00
  },
  "stripeApiKey": "sk_live_...",
  "webhookUrl": "https://api.uber.com/webhooks/rappidrive",
  "supportEmail": "support@uber.com.br",
  "language": "pt-BR"
}
```

---

## 5. Isolamento de Dados (Segurança)

### 5.1 Garantias de Isolamento

**Regra 1: Toda query inclui `tenant_id`**

```java
// ❌ ERRADO - Acessa dados de todas as tenants
List<Driver> drivers = driverRepository.findAll();

// ✅ CORRETO - Filtra por tenant_id
List<Driver> drivers = driverRepository.findByTenantId(tenantId);
```

**Implementação em RepositoryPorts**:

```java
public interface DriverRepositoryPort {
    // ✅ Sempre com tenantId
    Optional<Driver> findById(UUID driverId, TenantId tenantId);
    List<Driver> findByStatus(DriverStatus status, TenantId tenantId);
    List<Driver> findNearby(Location location, double radiusKm, TenantId tenantId);
    
    // ❌ Nunca sem tenantId
    // List<Driver> findAll();
    // Optional<Driver> findById(UUID id);
}
```

**Implementação em Adapters (JPA)**:

```java
@Component
public class JpaDriverRepositoryAdapter implements DriverRepositoryPort {
    @Override
    public Optional<Driver> findById(UUID driverId, TenantId tenantId) {
        // Sempre filtra por tenant_id + id
        return jpaRepository.findByIdAndTenantId(driverId, tenantId)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Driver> findNearby(Location location, double radiusKm, TenantId tenantId) {
        // Query PostGIS com filtro tenant_id
        return jpaRepository.findNearbyWithinTenant(
            location.latitude(),
            location.longitude(),
            radiusKm,
            tenantId.getValue()
        ).stream()
         .map(mapper::toDomain)
         .collect(toList());
    }
}
```

### 5.2 Proteção em Camadas

```
┌─────────────────────────────────────────────────┐
│        Presentation Layer (REST Controller)     │
│  - Valida tenantId no JWT token                 │
│  - Passa tenantId para use case                 │
└──────────────┬──────────────────────────────────┘
               ↓
┌─────────────────────────────────────────────────┐
│        Application Layer (Use Cases)            │
│  - Valida ownership (motorista é da tenant?)    │
│  - Passa tenantId para repository               │
└──────────────┬──────────────────────────────────┘
               ↓
┌─────────────────────────────────────────────────┐
│        Infrastructure Layer (JPA Repository)    │
│  - Query SEMPRE inclui WHERE tenant_id = ?      │
│  - Índices em (tenant_id, status, ...)          │
└─────────────────────────────────────────────────┘
               ↓
        PostgreSQL Database
```

---

## 6. Ciclo de Vida de uma Corrida (Trip)

Uma corrida não existe sem tenant:

```java
public class Trip {
    private final TripId id;
    private final TenantId tenantId;        // ← OBRIGATÓRIO
    private final PassengerId passengerId;
    private final DriverId driverId;
    private final Location origin;
    private final Location destination;
    private TripStatus status;
    // ...
}
```

### 6.1 Estados de uma Trip

```
REQUESTED (passageiro solicita)
    ↓
DRIVER_ASSIGNED (motorista atribuído)
    ↓
IN_PROGRESS (motorista saiu do local)
    ↓
COMPLETED (corrida finalizada)

Ou: CANCELLED (em qualquer estado, conforme HIST-2026-017)
```

### 6.2 Isolamento em Operações

```sql
-- Criar trip
INSERT INTO trips (tenant_id, passenger_id, ...)
VALUES (?, ?, ...);

-- Atribuir motorista à trip
UPDATE trips
SET driver_id = ?, status = 'DRIVER_ASSIGNED'
WHERE id = ? AND tenant_id = ?;   -- ← tenant_id sempre

-- Cancelar trip (HIST-2026-017)
UPDATE trips
SET status = 'CANCELLED', cancelled_by = ?, cancellation_reason_enum = ?
WHERE id = ? AND tenant_id = ?;   -- ← tenant_id sempre
```

---

## 7. Auditoria e Logs

### 7.1 Rastreabilidade

Toda operação gera logs com `tenantId`:

```java
log.info("Trip created: tripId={}, tenantId={}, passengerId={}",
         trip.getId(), trip.getTenantId(), trip.getPassengerId());

log.info("Driver assigned: driverId={}, tenantId={}, status=DRIVER_ASSIGNED",
         driverId, tenantId);

log.info("Trip cancelled: tripId={}, tenantId={}, reason={}, fee={}",
         trip.getId(), tenantId, cancellationReason, fee);
```

### 7.2 Eventos de Domínio

Eventos publicados via Outbox com `tenantId`:

```java
public record TripCancelledEvent(
    String eventId,
    LocalDateTime occurredOn,
    TripId tripId,
    TenantId tenantId,        // ← Sempre incluir
    ActorType cancelledBy,
    CancellationReason reason,
    Money fee,
    LocalDateTime cancelledAt
) implements DomainEvent {}
```

---

## 8. Escalabilidade

### 8.1 Estratégia Multi-Tenant Escalável

```
┌─────────────────────────────────────────────────┐
│    Load Balancer                                │
└──────────┬──────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────┐
│    API Gateway (Kong/Nginx)                     │
│  - Extrai tenantId do header                    │
│  - Roteia para pod específico (opcional)        │
└──────────┬──────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────┐
│    Spring Boot Instances (3-5 réplicas)         │
│  - Cada um serve TODAS as tenants               │
│  - Filtro de segurança valida TenantId          │
│  - Virtual threads para concorrência            │
└──────────┬──────────────────────────────────────┘
           ↓
┌─────────────────────────────────────────────────┐
│    PostgreSQL 16 + PostGIS 3.4                  │
│  - Single database, múltiplas tenants           │
│  - Índices em (tenant_id, status, location)     │
│  - Row-level security (RLS) futuro              │
│  - Backup per-tenant possível                   │
└─────────────────────────────────────────────────┘
```

### 8.2 Índices Críticos para Performance

```sql
-- Isolamento rápido por tenant
CREATE INDEX idx_drivers_tenant ON drivers(tenant_id);
CREATE INDEX idx_passengers_tenant ON passengers(tenant_id);
CREATE INDEX idx_trips_tenant ON trips(tenant_id);
CREATE INDEX idx_vehicles_tenant ON vehicles(tenant_id);

-- Queries de negócio (status + tenant)
CREATE INDEX idx_drivers_status_tenant ON drivers(status, tenant_id);
CREATE INDEX idx_trips_status_tenant ON trips(status, tenant_id);

-- Geoespacial (PostGIS)
CREATE INDEX idx_drivers_location_gist ON drivers USING GIST(
    ST_SetSRID(ST_MakePoint(location_longitude, location_latitude), 4326)
) WHERE location_latitude IS NOT NULL;

-- Composite para queries comuns
CREATE INDEX idx_trips_tenant_status_created 
ON trips(tenant_id, status, created_at DESC);
```

---

## 9. Integração com Sistemas Externos

### 9.1 Webhooks por Tenant

```json
POST /webhooks/{tenantId}/trip-cancelled
Authorization: Bearer {tenantSecret}

{
  "eventId": "evt_...",
  "tripId": "trip_...",
  "tenantId": "550e8400-...",
  "cancelledBy": "PASSENGER",
  "reason": "WAIT_TOO_LONG",
  "fee": {
    "amount": 5.00,
    "currency": "BRL"
  },
  "cancelledAt": "2026-01-14T12:30:00Z"
}
```

### 9.2 Configuração por Tenant

```java
// Cada tenant pode ter sua configuração de pagamento
config = {
  "stripe": {
    "apiKey": "sk_test_...",
    "publishableKey": "pk_test_..."
  },
  "webhookSecret": "whsec_..."
}
```

---

## 10. Governança e Compliance

### 10.1 Separação de Responsabilidades

| Função | Responsabilidade |
|--------|------------------|
| **Admin RappiDrive** | Criar/deletar tenants, aprovar motoristas críticos |
| **Operações Tenant** | Configurar zonas, políticas de cancelamento |
| **Motorista** | Dirigir, aceitar/rejeitar corridas |
| **Passageiro** | Solicitar corridas, avaliar |

### 10.2 Auditoria de Dados

```sql
-- Tabela de auditoria (futuro)
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    entity_type VARCHAR(50),  -- 'TRIP', 'DRIVER', 'PAYMENT'
    entity_id UUID,
    action VARCHAR(50),       -- 'CREATE', 'UPDATE', 'DELETE'
    old_data JSONB,
    new_data JSONB,
    actor_id UUID,
    actor_type VARCHAR(20),   -- 'ADMIN', 'DRIVER', 'SYSTEM'
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 11. Casos de Uso Práticos

### 11.1 Cenário 1: Adição de Novo Motorista

```
Input:
- Tenant: Uber Brasil
- Motorista: João Silva (CPF, CNH, dados)

Fluxo:
1. POST /api/v1/drivers
   {
     "tenantId": "550e8400-...",
     "fullName": "João Silva",
     "cpf": "123.456.789-00",
     ...
   }

2. CreateDriverUseCase executa:
   - Valida CPF (domínio)
   - Cria Driver(id, tenantId, ..., status=INACTIVE)
   - Salva em driverRepository.save(driver, tenantId)  // ← tenantId sempre
   - Publica DriverCreatedEvent(tenantId, ...)
   - Retorna DriverResponse

3. SQL executado:
   INSERT INTO drivers (id, tenant_id, cpf, email, status, ...)
   VALUES ('...', '550e8400-...', '12345678900', '...', 'INACTIVE', ...)

Output:
- Status 201 Created
- Motorista criado com status INACTIVE
- Admin aprova depois via POST /drivers/{id}/approve
```

### 11.2 Cenário 2: Cancelamento de Corrida (HIST-2026-017)

```
Input:
- Trip: ABC-123 (Tenant: 99 Táxi)
- Passageiro: Maria Silva
- Razão: "Motorista demorando demais"

Fluxo:
1. POST /api/v1/trips/{tripId}/cancel
   {
     "reason": "PASSENGER_WAIT_TOO_LONG",
     "additionalNotes": "Motorista demorando demais"
   }

2. CancelTripUseCase executa:
   - Fetch Trip by tripId + tenantId (isolamento!)
   - Valida autorização: passageiro é da trip?
   - Calcula taxa: CancellationPolicyService.calculateFee(trip, PASSENGER)
     → Trip status REQUESTED + 6min transcorridos = R$ 5.00
   - Processa pagamento: PaymentGatewayPort.processPayment(...)
   - Chama Trip.cancel(PASSENGER, WAIT_TOO_LONG, now)
   - Publica TripCancelledEvent(tripId, tenantId, PASSENGER, WAIT_TOO_LONG, fee)
   - Salva em tripRepository.save(trip, tenantId)

3. SQL executado:
   UPDATE trips 
   SET status = 'CANCELLED', cancelled_by = 'PASSENGER', 
       cancellation_reason_enum = 'PASSENGER_WAIT_TOO_LONG',
       cancelled_at = CURRENT_TIMESTAMP
   WHERE id = 'ABC-123' AND tenant_id = '550e8400-...';

4. OutboxPublisher processa evento:
   INSERT INTO outbox_events (...)
   VALUES ('TripCancelledEvent', '{"tripId":"...","tenantId":"...","fee":5.00}', ...)

Output:
- Status 200 OK
- Trip cancelada
- Passageira recebe R$ 5.00 de taxa
- Webhook enviado para Tenant (se configurado)
```

---

## 12. Roadmap de Melhorias

### Curto Prazo (Q1 2026)
- [ ] Dashboard de administração por tenant
- [ ] Relatórios de performance (trips, motoristas, receita)
- [ ] Alertas de anomalias (fraude, driver inativo)

### Médio Prazo (Q2-Q3 2026)
- [ ] Row-Level Security (RLS) no PostgreSQL
- [ ] Backup automático per-tenant
- [ ] Multi-region replication
- [ ] Políticas de cancelamento customizáveis por tenant

### Longo Prazo (Q4 2026+)
- [ ] Separação de bancos (1 DB por tenant para máxima segurança)
- [ ] SLA customizável per-tenant
- [ ] Machine Learning para detecção de fraude por tenant
- [ ] Marketplace de integrações (pagamento, SMS, maps)

---

## 13. Checklist de Onboarding de Nova Tenant

- [ ] Criar tenant no banco (tabela `tenants`)
- [ ] Gerar chave de API para integrações
- [ ] Configurar zones de operação
- [ ] Configurar políticas de cancelamento (HIST-2026-017)
- [ ] Configurar credenciais de pagamento (Stripe, etc.)
- [ ] Enviar email com credenciais de admin
- [ ] Treinar equipe de operações
- [ ] Validar com motorista de teste
- [ ] Validar com passageiro de teste
- [ ] Ir live em produção

---

## Referências

- **HIST-2026-017**: Cancelamento de Corridas com Política de Tarifa
- **Domain Model**: `domain/entities/Driver`, `Passenger`, `Trip`, `Vehicle`
- **Repositories**: `application/ports/output/*RepositoryPort.java`
- **Migrations**: `db/migration/V1__Initial_schema.sql`

---

**Próxima Revisão**: 30 de março de 2026
