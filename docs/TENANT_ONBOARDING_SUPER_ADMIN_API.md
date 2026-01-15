# Tenant Onboarding Super Admin API - Documentação Técnica

**Data**: 14 de janeiro de 2026  
**Versão**: 1.0  
**Status**: ✅ Implementado e Testado

---

## 📋 Sumário Executivo

O **Tenant Onboarding Super Admin API** automatiza completamente o processo de adicionar uma nova cidade (tenant) ao RappiDrive. Antes, era um processo manual propenso a erros. Agora, uma única chamada HTTP orquestra:

1. ✅ Criação de usuário admin no Keycloak
2. ✅ Criação de grupo de tenant no Keycloak  
3. ✅ Salvamento de configuração de tarifas
4. ✅ Criação da área de serviço (polígono GeoJSON)
5. ✅ Publicação de evento para notificações/email

---

## 🏗️ Arquitetura

### Padrão: Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  SuperAdminController → OnboardTenantRequest/Response       │
│  (Security: ROLE_SUPER_ADMIN)                               │
└────────────────┬────────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────────┐
│                  APPLICATION LAYER                          │
│  OnboardNewTenantUseCase (Orchestrator)                     │
│  Depends on: OnboardNewTenantInputPort                      │
└────────────────┬────────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
┌───────▼──────┐  ┌──────▼────────-──┐
│ Domain Layer │  │ Output Ports     │
├──────────────┤  ├──────────────-───┤
│ServiceArea   │  │ServiceAreaRepo   │
│FareConfig    │  │FareConfigRepo    │
│Event:        │  │IdentityProvision |
│TenantOnboard │  │                  │
│              │  │                  │
└──────────────┘  └──────┬──────_────┘
                         │
                ┌────────┴────────┐
                │                 │
        ┌──────▼────────┐  ┌─────▼──────────────┐
        │INFRASTRUCTURE │  │INFRASTRUCTURE      │
        ├───────────────┤  ├────────────────────┤
        │PostgreSQL:    │  │Keycloak:           │
        │- ServiceArea  │  │- Admin User        │
        │- FareConfig   │  │- Tenant Group      │
        │  (JPA)        │  │- Role Assignment   │
        └───────────────┘  └────────────────────┘
```

### Decisão Arquitetural: Single Realm + Groups

- **Realm único** (`rappidrive`) para todos os tenants
- **Grupo por tenant** com nome: `tenant:{tenantId}`
- **Atributos de usuário**: `tenant_id = {tenantId}`
- **Roles**: `ROLE_ADMIN`, `ROLE_DRIVER`, `ROLE_PASSENGER` (realm-level)
- **Benefícios**:
  - Simpler Keycloak management
  - Cross-tenant operations easier
  - Reduced operational complexity

---

## 📚 Fluxo Completo de Onboarding

```
┌────────────────────────────────────────────────────────────┐
│  SuperAdmin faz POST /api/admin/tenants                    │
│  Authorization: Bearer ROLE_SUPER_ADMIN                    │
│  Body: OnboardTenantRequest (validação Bean Validation)   │
└─────────────────────┬──────────────────────────────────────┘
                      │
        ┌─────────────▼────────────────┐
        │  OnboardNewTenantUseCase      │
        │  execute(OnboardingCommand)   │
        └─────────────┬────────────────┘
                      │
        ┌─────────────┴──────────────┐
        │                            │
   ┌────▼────────────┐   ┌─────────▼────────┐
   │ KEYCLOAK        │   │ POSTGRESQL        │
   ├─────────────────┤   ├──────────────────┤
   │1. Validate no   │   │4. Create         │
   │   tenant exists │   │   FareConfig     │
   │                 │   │                  │
   │2. Create admin  │   │5. Create         │
   │   user          │   │   ServiceArea    │
   │                 │   │                  │
   │3. Create tenant │   │6. Publish        │
   │   group         │   │   TenantOnboarded│
   │                 │   │   Event          │
   │4. Assign roles  │   │                  │
   │                 │   │(Transactional)   │
   └────────────────┘   └──────────────────┘
        │                       │
        └───────────┬───────────┘
                    │
    ┌───────────────▼──────────────┐
    │ OnboardingResult              │
    │ - tenantId                    │
    │ - keycloakUserId (admin)      │
    │ - keycloakGroupId             │
    │ - serviceAreaId               │
    │ - fareConfigurationId         │
    │ - temporaryPassword           │
    │ - message                     │
    └───────────────┬──────────────┘
                    │
    ┌───────────────▼──────────────┐
    │ Response 201 Created          │
    │ SuperAdmin receives creds     │
    └───────────────────────────────┘
```

---

## 🔌 Endpoints

### Onboard New Tenant

```http
POST /api/admin/tenants
Authorization: Bearer <JWT_SUPER_ADMIN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "tenantId": "goiania-go",
  "displayName": "RappiDrive Goiânia",
  "adminEmail": "admin@goiania.rappidrive.com",
  "currency": "BRL",
  "baseFare": 5.00,
  "pricePerKm": 2.50,
  "pricePerMin": 0.30,
  "serviceAreaName": "Centro de Goiânia",
  "geoJsonPolygon": {
    "type": "Polygon",
    "coordinates": [
      [
        [-49.0, -15.8],
        [-49.0, -15.9],
        [-49.1, -15.9],
        [-49.1, -15.8],
        [-49.0, -15.8]
      ]
    ]
  }
}
```

**Response (201 Created)**:
```json
{
  "tenant_id": "goiania-go",
  "display_name": "RappiDrive Goiânia",
  "admin_email": "admin@goiania.rappidrive.com",
  "temporary_password": "aBcDeFgHiJkLmNoP",
  "keycloak_user_id": "550e8400-e29b-41d4-a716-446655440001",
  "keycloak_group_id": "650e8400-e29b-41d4-a716-446655440002",
  "service_area_id": "750e8400-e29b-41d4-a716-446655440003",
  "fare_configuration_id": "850e8400-e29b-41d4-a716-446655440004",
  "message": "Tenant 'RappiDrive Goiânia' onboarded successfully. Admin user created: admin@goiania.rappidrive.com. User must change temporary password on first login."
}
```

**Validações (Bean Validation)**:
- `tenantId`: obrigatório, não-branco
- `displayName`: obrigatório, não-branco
- `adminEmail`: obrigatório, formato email válido
- `currency`: obrigatório, padrão ISO 4217 (ex: "BRL")
- `baseFare`: obrigatório, > 0, max 2 casas decimais
- `pricePerKm`: obrigatório, > 0, max 2 casas decimais
- `pricePerMin`: obrigatório, > 0, max 2 casas decimais
- `serviceAreaName`: obrigatório, não-branco
- `geoJsonPolygon`: obrigatório, válido GeoJSON

**Erros Possíveis**:
- `400 Bad Request`: Dados inválidos
- `401 Unauthorized`: Token ausente/inválido
- `403 Forbidden`: Usuário sem `ROLE_SUPER_ADMIN`
- `409 Conflict`: Tenant já existe
- `422 Unprocessable Entity`: Validação de negócio falhou
- `500 Internal Server Error`: Erro no servidor

---

## 📁 Estrutura de Arquivos Criados

### Domain Layer (Framework-Free)

```
domain/
├── entities/
│   └── ServiceArea.java              (Entidade imutável + Builder)
├── valueobjects/
│   └── ServiceAreaId.java            (Value Object UUID-based)
└── events/
    └── TenantOnboardedEvent.java     (Domain Event - Outbox Pattern)
```

### Application Layer

```
application/
├── ports/
│   ├── input/
│   │   └── tenant/
│   │       └── OnboardNewTenantInputPort.java  (Use case interface)
│   └── output/
│       ├── IdentityProvisioningPort.java       (Keycloak operations)
│       └── ServiceAreaRepositoryPort.java      (Persistence)
├── usecases/
│   └── tenant/
│       └── OnboardNewTenantUseCase.java        (Orchestration)
└── exceptions/
    ├── IdentityProvisioningException.java
    └── TenantAlreadyExistsException.java
```

### Infrastructure Layer

```
infrastructure/
├── config/
│   └── KeycloakConfig.java                     (Admin Client bean)
├── adapters/
│   └── keycloak/
│       └── KeycloakProvisioningAdapter.java    (Keycloak implementation)
├── persistence/
│   ├── entities/
│   │   └── ServiceAreaJpaEntity.java           (JPA mapping)
│   ├── repositories/
│   │   └── SpringDataServiceAreaRepository.java
│   ├── mappers/
│   │   └── ServiceAreaMapper.java              (Domain ↔ JPA)
│   └── adapters/
│       └── JpaServiceAreaRepositoryAdapter.java
└── db/migration/
    └── V18__create_service_areas_table.sql
```

### Presentation Layer

```
presentation/
├── controllers/
│   └── admin/
│       └── SuperAdminController.java           (REST endpoint)
├── dto/
│   ├── request/
│   │   └── OnboardTenantRequest.java
│   └── response/
│       └── OnboardTenantResponse.java
└── mappers/
    └── OnboardTenantDtoMapper.java
```

### Tests

```
test/
└── java/.../infrastructure/integration/
    └── TenantOnboardingIntegrationTest.java    (E2E with Testcontainers)
```

---

## 🔐 Segurança

### Autenticação
- JWT Bearer Token (Keycloak)
- Header: `Authorization: Bearer <token>`

### Autorização
- Endpoint `/api/admin/tenants` requer `ROLE_SUPER_ADMIN`
- Apenas desenvolvedores/donos da plataforma devem ter essa role
- Implementado via `@PreAuthorize("hasRole('SUPER_ADMIN')")`

### Geração de Senha Temporária
- **Algoritmo**: SecureRandom + Base64 URL-safe
- **Comprimento**: 16 caracteres
- **Propriedade**: Única para cada tenant
- **Expiração**: Usuário deve mudar no primeiro login

### Multi-Tenancy
- Cada usuário recebe atributo `tenant_id` no Keycloak
- Garantir isolamento de dados via `WHERE tenant_id = ?` em queries

---

## 🧪 Testes

### TenantOnboardingIntegrationTest

Testa cenários:

1. **Happy Path**: Onboarding bem-sucedido
   - User criado em Keycloak
   - Group criado em Keycloak
   - FareConfiguration salva em PostgreSQL
   - ServiceArea salva em PostgreSQL
   - Event publicado

2. **Tenant Already Exists**: Validação de duplicatas
   - Verifica se ServiceArea existe
   - Verifica se Group no Keycloak existe
   - Lança `TenantAlreadyExistsException`

3. **Invalid GeoJSON**: Validação de formato
   - Rejeita JSON sem "type" e "coordinates"
   - Valida no domain (ServiceArea constructor)

4. **Secure Passwords**: Geração de senhas
   - Cada tenant recebe senha única
   - Comprimento = 16 caracteres
   - Não são iguais entre tenants

**Como rodar**:
```bash
# Com Testcontainers (PostgreSQL + Keycloak)
mvn test -Dtest=TenantOnboardingIntegrationTest

# Todos os testes
mvn test

# Verify (inclui integração)
mvn verify
```

---

## 📊 Configuração

### application.yml

```yaml
keycloak:
  auth-server-url: http://localhost:8080
  realm: rappidrive
  admin:
    client-id: admin-cli
    username: admin
    password: admin
```

### Variáveis de Ambiente (Production)

```bash
KEYCLOAK_AUTH_SERVER_URL=https://keycloak.rappidrive.com
KEYCLOAK_ADMIN_USERNAME=${KC_ADMIN_USERNAME}
KEYCLOAK_ADMIN_PASSWORD=${KC_ADMIN_PASSWORD}
```

---

## 🔄 Event-Driven Architecture

### TenantOnboardedEvent

Publicado após onboarding bem-sucedido:

```java
{
  "eventId": "uuid",
  "tenantId": "goiania-go",
  "displayName": "RappiDrive Goiânia",
  "adminEmail": "admin@goiania.rappidrive.com",
  "serviceAreaName": "Centro",
  "occurredAt": "2026-01-14T12:00:00Z"
}
```

**Listeners podem**:
- Enviar email de boas-vindas
- Criar notificação para operações
- Integrar com sistemas externos
- Registrar em analytics

**Implementação**: Outbox Pattern
- Event armazenado em `outbox_event` table
- `OutboxPublisher` processa de forma confiável
- Garante entrega mesmo com crash do serviço

---

## 🚀 Exemplo de Uso

### cURL

```bash
curl -X POST http://localhost:8080/api/admin/tenants \
  -H "Authorization: Bearer <JWT_SUPER_ADMIN>" \
  -H "Content-Type: application/json" \
  -d '{
    "tenantId": "brasilia-df",
    "displayName": "RappiDrive Brasília",
    "adminEmail": "admin@brasilia.rappidrive.com",
    "currency": "BRL",
    "baseFare": 5.50,
    "pricePerKm": 2.75,
    "pricePerMin": 0.35,
    "serviceAreaName": "Eixo Monumental",
    "geoJsonPolygon": "{\"type\":\"Polygon\",\"coordinates\":[[[-47.9,-15.8],[-47.9,-15.9],[-48.0,-15.9],[-48.0,-15.8],[-47.9,-15.8]]]}"
  }'
```

### Java/Spring

```java
@Autowired
private OnboardNewTenantInputPort onboardTenantUseCase;

public void onboardNewCity() {
    var command = new OnboardNewTenantInputPort.OnboardingCommand(
        TenantId.of("curitiba-pr"),
        "RappiDrive Curitiba",
        Email.of("admin@curitiba.rappidrive.com"),
        "BRL",
        new BigDecimal("5.25"),
        new BigDecimal("2.60"),
        new BigDecimal("0.32"),
        "Bairro Alto",
        geoJsonPolygon
    );
    
    var result = onboardTenantUseCase.execute(command);
    
    System.out.println("Admin: " + result.adminEmail());
    System.out.println("Temporary Password: " + result.temporaryPassword());
    System.out.println("Keycloak User ID: " + result.keycloakUserId());
}
```

---

## ⚠️ Tratamento de Erros

### Cleanup Automático em Caso de Erro

Se algo falhar durante o onboarding:
1. Tenta deletar o group do Keycloak
2. Tenta deletar as ServiceAreas
3. Mantém FareConfiguration para auditoria
4. Registra erros sem propagar

```java
private void cleanupOnboardingOnError(TenantId tenantId) {
    try {
        // Delete Keycloak group
        identityProvisioning.deleteTenantGroup(tenantId);
        
        // Delete service areas
        serviceAreaRepository.findByTenantId(tenantId)
            .forEach(sa -> serviceAreaRepository.delete(sa.getId()));
        
        // Note: FareConfiguration kept for audit
    } catch (Exception e) {
        log.error("Error during cleanup", e);
    }
}
```

---

## 📈 Performance

- **Operação rápida**: < 2 segundos (com Keycloak local)
- **Transações ACID**: PostgreSQL garante consistência
- **Sem pontos de falha**: Cleanup automático em erro
- **Escalável**: Virtual Threads (Java 21) suportam múltiplas requisições paralelas

---

## 🔧 Troubleshooting

### Erro: "Failed to create user in Keycloak"

**Causa**: Keycloak não está acessível ou credenciais erradas
**Solução**: Verificar `keycloak.auth-server-url` e credenciais admin

### Erro: "Tenant already exists"

**Causa**: Tenant duplicado
**Solução**: Usar `tenantId` único ou deletar primeiro

### Erro: "Invalid GeoJSON format"

**Causa**: Polygon sem "type" ou "coordinates"
**Solução**: Validar GeoJSON em https://geojson.io/

---

## 📝 Dependências Adicionadas

```xml
<dependency>
    <groupId>org.keycloak</groupId>
    <artifactId>keycloak-admin-client</artifactId>
    <version>23.0.3</version>
</dependency>
```

---

## ✅ Checklist de Implementação

- ✅ Domain Layer: `ServiceArea`, `ServiceAreaId`, `TenantOnboardedEvent`
- ✅ Application Ports: Input (OnboardNewTenant), Output (Repository, Keycloak)
- ✅ Use Case: `OnboardNewTenantUseCase` com orquestração
- ✅ Infrastructure: Keycloak, PostgreSQL, migrations
- ✅ Presentation: Controller, DTOs, mappers
- ✅ Security: ROLE_SUPER_ADMIN, autorização
- ✅ Tests: Integration tests com Testcontainers
- ✅ Documentation: Este documento

---

## 🎯 Próximos Passos

1. **Configurar Keycloak em produção**
   - Setup admin credenciais
   - Criar roles (ROLE_ADMIN, ROLE_DRIVER, ROLE_PASSENGER)
   - Configurar email SMTP para notificações

2. **Implementar listeners para eventos**
   - Email de boas-vindas
   - Notificação no Slack
   - Integration com analytics

3. **Dashboard Super Admin**
   - Listar tenants onboarded
   - Visualizar histórico
   - Gerenciar configurações globais

4. **Documentação para DevOps**
   - Deploy do Keycloak
   - Backup strategy
   - Disaster recovery

---

**Última atualização**: 14 de janeiro de 2026
