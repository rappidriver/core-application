# RappiDrive Mobile API Documentation

**Versão da API**: v1  
**Data**: 14 de janeiro de 2026  
**Ambiente Base URL**: `https://api.rappidrive.com`  
**Staging URL**: `https://staging-api.rappidrive.com`  
**Documentação Swagger**: `/swagger-ui.html`

---

## Índice

1. [Introdução](#1-introdução)
2. [Autenticação e Segurança](#2-autenticação-e-segurança)
3. [Headers Obrigatórios](#3-headers-obrigatórios)
4. [Estrutura de Resposta](#4-estrutura-de-resposta)
5. [Códigos de Status HTTP](#5-códigos-de-status-http)
6. [Endpoints - Motorista (Driver)](#6-endpoints---motorista-driver)
7. [Endpoints - Passageiro (Passenger)](#7-endpoints---passageiro-passenger)
8. [Endpoints - Corrida (Trip)](#8-endpoints---corrida-trip)
9. [Endpoints - Veículo (Vehicle)](#9-endpoints---veículo-vehicle)
10. [Endpoints - Pagamento (Payment)](#10-endpoints---pagamento-payment)
11. [Endpoints - Avaliação (Rating)](#11-endpoints---avaliação-rating)
12. [Endpoints - Notificação (Notification)](#12-endpoints---notificação-notification)
13. [WebSockets e Eventos em Tempo Real](#13-websockets-e-eventos-em-tempo-real)
14. [Geolocalização e MapBox](#14-geolocalização-e-mapbox)
15. [Fluxos Principais](#15-fluxos-principais)
16. [Tratamento de Erros](#16-tratamento-de-erros)
17. [Paginação](#17-paginação)
18. [Rate Limiting](#18-rate-limiting)
19. [Versionamento](#19-versionamento)
20. [Ambiente de Desenvolvimento](#20-ambiente-de-desenvolvimento)
21. [Boas Práticas](#21-boas-práticas)
22. [SDKs e Bibliotecas Recomendadas](#22-sdks-e-bibliotecas-recomendadas)

---

## 1. Introdução

RappiDrive é uma plataforma white-label de mobilidade urbana. A API REST fornece todos os endpoints necessários para construir aplicativos móveis para **motoristas** e **passageiros**.

### 1.1 Características

- ✅ **RESTful** com JSON
- ✅ **OAuth 2.0** + JWT para autenticação
- ✅ **Multi-tenancy** (suporta múltiplas marcas/empresas)
- ✅ **WebSockets** para eventos em tempo real
- ✅ **PostGIS** para buscas geoespaciais
- ✅ **OpenAPI 3.0** (Swagger)
- ✅ **HTTPS** obrigatório em produção

### 1.2 Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────┐
│              Mobile Apps (iOS/Android)              │
├─────────────────────────────────────────────────────┤
│  Driver App           │       Passenger App         │
│  - Login              │       - Login               │
│  - Accept trips       │       - Request ride        │
│  - Navigation         │       - Track driver        │
│  - Earnings           │       - Payment             │
└────────┬──────────────┴─────────────┬───────────────┘
         │                             │
         │        HTTPS/REST           │
         ├─────────────────────────────┤
         ▼                             ▼
┌──────────────────────────────────────────────────────┐
│         API Gateway (Kong/Nginx) + Load Balancer     │
└────────┬─────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────┐
│     Spring Boot Backend (Hexagonal Architecture)    │
│  - Virtual Threads (Java 21)                        │
│  - Multi-tenant support                             │
│  - Event-driven (Outbox Pattern)                    │
└────────┬────────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────┐
│      PostgreSQL 16 + PostGIS 3.4 (Geospatial)      │
└────────────────────────────────────────────────────┘
```

---

## 2. Autenticação e Segurança

### 2.1 Fluxo de Autenticação OAuth 2.0

```
1. App Mobile solicita login:
   POST /oauth/token
   {
     "grant_type": "password",
     "username": "driver@email.com",
     "password": "senha123",
     "tenant_id": "550e8400-e29b-41d4-a716-446655440000"
   }

2. Backend retorna token JWT:
   {
     "access_token": "eyJhbGciOiJIUzI1NiIsInR...",
     "token_type": "Bearer",
     "expires_in": 3600,
     "refresh_token": "def502004f1c8b...",
     "scope": "driver:read driver:write trip:read trip:write"
   }

3. App Mobile usa token em todas as requests:
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR...
```

### 2.2 Refresh Token

```http
POST /oauth/token
Content-Type: application/json

{
  "grant_type": "refresh_token",
  "refresh_token": "def502004f1c8b..."
}
```

### 2.3 Logout

```http
POST /oauth/revoke
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR...

{
  "token": "eyJhbGciOiJIUzI1NiIsInR..."
}
```

---

## 3. Headers Obrigatórios

Todas as requisições autenticadas devem incluir:

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
Accept: application/json
X-Tenant-Id: <UUID>
X-Request-Id: <UUID>  # Para tracking
X-Platform: ios | android
X-App-Version: 1.2.3
```

**Exemplo**:
```http
GET /api/v1/drivers/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR...
X-Tenant-Id: 550e8400-e29b-41d4-a716-446655440000
X-Request-Id: 123e4567-e89b-12d3-a456-426614174000
X-Platform: ios
X-App-Version: 1.2.3
```

---

## 4. Estrutura de Resposta

### 4.1 Resposta de Sucesso (200/201)

```json
{
  "id": "650e8400-e29b-41d4-a716-446655440001",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "status": "ACTIVE",
  "createdAt": "2026-01-14T12:30:00Z"
}
```

### 4.2 Resposta de Erro (400/404/500)

```json
{
  "timestamp": "2026-01-14T12:30:45Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid CPF format",
  "path": "/api/v1/drivers",
  "traceId": "123e4567-e89b-12d3-a456-426614174000",
  "details": {
    "field": "cpf",
    "rejectedValue": "123",
    "reason": "CPF must have 11 digits"
  }
}
```

### 4.3 Resposta de Validação (422)

```json
{
  "timestamp": "2026-01-14T12:30:45Z",
  "status": 422,
  "error": "Validation Failed",
  "message": "Input validation errors",
  "path": "/api/v1/drivers",
  "errors": [
    {
      "field": "email",
      "message": "Email format is invalid"
    },
    {
      "field": "cpf",
      "message": "CPF already registered"
    }
  ]
}
```

---

## 5. Códigos de Status HTTP

| Código | Significado | Quando Usar |
|--------|-------------|-------------|
| **200** | OK | Request processada com sucesso |
| **201** | Created | Recurso criado (POST) |
| **204** | No Content | Sucesso sem retorno de dados (DELETE) |
| **400** | Bad Request | Dados inválidos enviados |
| **401** | Unauthorized | Token ausente ou inválido |
| **403** | Forbidden | Usuário sem permissão |
| **404** | Not Found | Recurso não encontrado |
| **409** | Conflict | Conflito (ex: email duplicado) |
| **422** | Unprocessable Entity | Validação de negócio falhou |
| **429** | Too Many Requests | Rate limit excedido |
| **500** | Internal Server Error | Erro no servidor |
| **503** | Service Unavailable | Serviço temporariamente indisponível |

---

## 6. Endpoints - Motorista (Driver)

### 6.1 Criar Motorista

```http
POST /api/v1/drivers
Content-Type: application/json
```

**Request Body**:
```json
{
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "cpf": "12345678901",
  "phone": "+5511999999999",
  "driverLicense": {
    "number": "12345678901",
    "category": "B",
    "issueDate": "2020-01-15",
    "expirationDate": "2030-01-15"
  }
}
```

**Response (201 Created)**:
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440001",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "cpf": "123.456.789-01",
  "phone": "+55 11 99999-9999",
  "status": "INACTIVE",
  "driverLicense": {
    "number": "12345678901",
    "category": "B",
    "issueDate": "2020-01-15",
    "expirationDate": "2030-01-15"
  },
  "createdAt": "2026-01-14T12:30:00Z",
  "updatedAt": "2026-01-14T12:30:00Z"
}
```

### 6.2 Buscar Motorista por ID

```http
GET /api/v1/drivers/{id}
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440001",
  "fullName": "João Silva",
  "email": "joao@email.com",
  "status": "ACTIVE",
  "currentLocation": {
    "latitude": -23.5505,
    "longitude": -46.6333
  },
  "rating": {
    "average": 4.8,
    "count": 1523
  },
  "totalTrips": 2341,
  "createdAt": "2026-01-01T10:00:00Z"
}
```

### 6.3 Buscar Dados do Motorista Atual (Me)

```http
GET /api/v1/drivers/me
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**: Mesmo formato de 6.2

### 6.4 Atualizar Localização do Motorista

```http
PUT /api/v1/drivers/{id}/location
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "latitude": -23.5505,
  "longitude": -46.6333,
  "heading": 180.5,
  "speed": 35.2,
  "accuracy": 10.5,
  "timestamp": "2026-01-14T12:35:00Z"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "location": {
    "latitude": -23.5505,
    "longitude": -46.6333
  },
  "updatedAt": "2026-01-14T12:35:00Z"
}
```

**⚠️ Importante**: Enviar localização a cada 5-10 segundos quando motorista estiver ACTIVE ou em corrida.

### 6.5 Ativar Motorista (Disponível para Corridas)

```http
PUT /api/v1/drivers/{id}/activate
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440001",
  "status": "ACTIVE",
  "message": "Driver is now available for trips"
}
```

### 6.6 Desativar Motorista (Offline)

```http
PUT /api/v1/drivers/{id}/deactivate
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440001",
  "status": "INACTIVE",
  "message": "Driver is now offline"
}
```

### 6.7 Buscar Motoristas Disponíveis (Próximos)

```http
GET /api/v1/drivers/available?latitude=-23.5505&longitude=-46.6333&radius=5
Authorization: Bearer <TOKEN>
```

**Query Parameters**:
- `latitude` (required): Latitude do ponto de partida
- `longitude` (required): Longitude do ponto de partida
- `radius` (optional, default: 5): Raio de busca em km

**Response (200 OK)**:
```json
{
  "drivers": [
    {
      "id": "650e8400-e29b-41d4-a716-446655440001",
      "fullName": "João Silva",
      "currentLocation": {
        "latitude": -23.5505,
        "longitude": -46.6333
      },
      "distanceKm": 1.2,
      "rating": {
        "average": 4.8,
        "count": 1523
      },
      "vehicle": {
        "brand": "Toyota",
        "model": "Corolla",
        "color": "Preto",
        "licensePlate": "ABC-1234"
      },
      "estimatedArrivalMinutes": 3
    }
  ],
  "total": 15,
  "averageDistanceKm": 2.5
}
```

---

## 7. Endpoints - Passageiro (Passenger)

### 7.1 Criar Passageiro

```http
POST /api/v1/passengers
Content-Type: application/json
```

**Request Body**:
```json
{
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "Maria Silva",
  "email": "maria@email.com",
  "phone": "+5511988888888"
}
```

**Response (201 Created)**:
```json
{
  "id": "750e8400-e29b-41d4-a716-446655440002",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "Maria Silva",
  "email": "maria@email.com",
  "phone": "+55 11 98888-8888",
  "status": "ACTIVE",
  "rating": {
    "average": 5.0,
    "count": 0
  },
  "totalTrips": 0,
  "createdAt": "2026-01-14T12:30:00Z"
}
```

### 7.2 Buscar Passageiro por ID

```http
GET /api/v1/passengers/{id}
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**: Mesmo formato de 7.1

### 7.3 Buscar Dados do Passageiro Atual (Me)

```http
GET /api/v1/passengers/me
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**: Mesmo formato de 7.1

### 7.4 Atualizar Perfil do Passageiro

```http
PUT /api/v1/passengers/{id}
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "fullName": "Maria Silva Santos",
  "phone": "+5511988888888",
  "favoriteAddresses": [
    {
      "label": "Casa",
      "address": "Rua das Flores, 123",
      "latitude": -23.5505,
      "longitude": -46.6333
    },
    {
      "label": "Trabalho",
      "address": "Av. Paulista, 1000",
      "latitude": -23.5505,
      "longitude": -46.6555
    }
  ]
}
```

**Response (200 OK)**: Passageiro atualizado

---

## 8. Endpoints - Corrida (Trip)

### 8.1 Solicitar Corrida

```http
POST /api/v1/trips
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "passengerId": "750e8400-e29b-41d4-a716-446655440002",
  "origin": {
    "latitude": -23.5505,
    "longitude": -46.6333,
    "address": "Rua das Flores, 123 - São Paulo, SP"
  },
  "destination": {
    "latitude": -23.5605,
    "longitude": -46.6555,
    "address": "Av. Paulista, 1000 - São Paulo, SP"
  },
  "vehicleType": "STANDARD",
  "paymentMethod": "CREDIT_CARD"
}
```

**Response (201 Created)**:
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440003",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "passengerId": "750e8400-e29b-41d4-a716-446655440002",
  "status": "REQUESTED",
  "origin": {
    "latitude": -23.5505,
    "longitude": -46.6333,
    "address": "Rua das Flores, 123 - São Paulo, SP"
  },
  "destination": {
    "latitude": -23.5605,
    "longitude": -46.6555,
    "address": "Av. Paulista, 1000 - São Paulo, SP"
  },
  "estimatedFare": {
    "amount": 15.50,
    "currency": "BRL"
  },
  "estimatedDistanceKm": 5.2,
  "estimatedDurationMinutes": 12,
  "requestedAt": "2026-01-14T12:40:00Z"
}
```

### 8.2 Buscar Corrida por ID

```http
GET /api/v1/trips/{id}
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440003",
  "status": "DRIVER_ASSIGNED",
  "passenger": {
    "id": "750e8400-e29b-41d4-a716-446655440002",
    "fullName": "Maria Silva",
    "phone": "+55 11 98888-8888",
    "rating": 5.0
  },
  "driver": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "fullName": "João Silva",
    "phone": "+55 11 99999-9999",
    "rating": 4.8,
    "currentLocation": {
      "latitude": -23.5505,
      "longitude": -46.6333
    },
    "vehicle": {
      "brand": "Toyota",
      "model": "Corolla",
      "color": "Preto",
      "licensePlate": "ABC-1234"
    }
  },
  "origin": {
    "latitude": -23.5505,
    "longitude": -46.6333,
    "address": "Rua das Flores, 123"
  },
  "destination": {
    "latitude": -23.5605,
    "longitude": -46.6555,
    "address": "Av. Paulista, 1000"
  },
  "estimatedFare": {
    "amount": 15.50,
    "currency": "BRL"
  },
  "actualFare": null,
  "estimatedDistanceKm": 5.2,
  "estimatedDurationMinutes": 12,
  "requestedAt": "2026-01-14T12:40:00Z",
  "assignedAt": "2026-01-14T12:42:00Z",
  "startedAt": null,
  "completedAt": null
}
```

### 8.3 Atribuir Motorista à Corrida

```http
POST /api/v1/trips/{id}/assign
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "driverId": "650e8400-e29b-41d4-a716-446655440001"
}
```

**Response (200 OK)**:
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440003",
  "status": "DRIVER_ASSIGNED",
  "driver": {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "fullName": "João Silva"
  },
  "assignedAt": "2026-01-14T12:42:00Z"
}
```

### 8.4 Iniciar Corrida

```http
POST /api/v1/trips/{id}/start
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440003",
  "status": "IN_PROGRESS",
  "startedAt": "2026-01-14T12:45:00Z"
}
```

### 8.5 Completar Corrida

```http
POST /api/v1/trips/{id}/complete-with-payment
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "actualDistanceKm": 5.8,
  "actualDurationMinutes": 15,
  "paymentMethod": "CREDIT_CARD"
}
```

**Response (200 OK)**:
```json
{
  "trip": {
    "id": "850e8400-e29b-41d4-a716-446655440003",
    "status": "COMPLETED",
    "completedAt": "2026-01-14T13:00:00Z"
  },
  "fare": {
    "id": "950e8400-e29b-41d4-a716-446655440004",
    "baseFare": 5.00,
    "distanceFare": 14.50,
    "timeFare": 2.50,
    "totalFare": 22.00,
    "currency": "BRL"
  },
  "payment": {
    "id": "a50e8400-e29b-41d4-a716-446655440005",
    "amount": 22.00,
    "method": "CREDIT_CARD",
    "status": "COMPLETED",
    "processedAt": "2026-01-14T13:00:05Z"
  }
}
```

### 8.6 Cancelar Corrida

```http
POST /api/v1/trips/{id}/cancel
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "reason": "PASSENGER_WAIT_TOO_LONG",
  "additionalNotes": "Motorista demorando muito"
}
```

**Response (200 OK)**:
```json
{
  "tripId": "850e8400-e29b-41d4-a716-446655440003",
  "cancelled": true,
  "cancelledBy": "PASSENGER",
  "reason": "PASSENGER_WAIT_TOO_LONG",
  "feeCharged": {
    "amount": 5.00,
    "currency": "BRL"
  },
  "cancelledAt": "2026-01-14T12:50:00Z",
  "message": "Trip cancelled successfully. Fee charged: R$ 5,00"
}
```

**Motivos de Cancelamento**:
- Passageiro: `PASSENGER_CHANGE_OF_PLANS`, `PASSENGER_PRICE_TOO_HIGH`, `PASSENGER_WAIT_TOO_LONG`, `PASSENGER_WRONG_LOCATION`, `PASSENGER_OTHER`
- Motorista: `DRIVER_PASSENGER_NOT_FOUND`, `DRIVER_UNSAFE_LOCATION`, `DRIVER_VEHICLE_ISSUE`, `DRIVER_OTHER`

**Política de Tarifa de Cancelamento**:
- **Status REQUESTED**:
  - < 5 minutos: grátis
  - ≥ 5 minutos: R$ 5,00
- **Status DRIVER_ASSIGNED**:
  - < 2 minutos (desde atribuição): grátis
  - ≥ 2 minutos: R$ 8,00
- **Motorista**: sempre grátis

### 8.7 Listar Corridas do Passageiro

```http
GET /api/v1/passengers/{passengerId}/trips?status=COMPLETED&page=0&size=20
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "content": [
    {
      "id": "850e8400-e29b-41d4-a716-446655440003",
      "status": "COMPLETED",
      "origin": "Rua das Flores, 123",
      "destination": "Av. Paulista, 1000",
      "fare": 22.00,
      "distanceKm": 5.8,
      "completedAt": "2026-01-14T13:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3
}
```

### 8.8 Listar Corridas do Motorista

```http
GET /api/v1/drivers/{driverId}/trips?status=COMPLETED&page=0&size=20
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**: Mesmo formato de 8.7

---

## 9. Endpoints - Veículo (Vehicle)

### 9.1 Cadastrar Veículo

```http
POST /api/v1/vehicles
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "driverId": "650e8400-e29b-41d4-a716-446655440001",
  "licensePlate": "ABC-1234",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2023,
  "color": "Preto",
  "vehicleType": "STANDARD"
}
```

**Response (201 Created)**:
```json
{
  "id": "b50e8400-e29b-41d4-a716-446655440006",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "driverId": "650e8400-e29b-41d4-a716-446655440001",
  "licensePlate": "ABC-1234",
  "brand": "Toyota",
  "model": "Corolla",
  "year": 2023,
  "color": "Preto",
  "vehicleType": "STANDARD",
  "status": "ACTIVE",
  "createdAt": "2026-01-14T12:30:00Z"
}
```

### 9.2 Buscar Veículo por ID

```http
GET /api/v1/vehicles/{id}
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**: Mesmo formato de 9.1

### 9.3 Listar Veículos do Motorista

```http
GET /api/v1/drivers/{driverId}/vehicles
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "vehicles": [
    {
      "id": "b50e8400-e29b-41d4-a716-446655440006",
      "licensePlate": "ABC-1234",
      "brand": "Toyota",
      "model": "Corolla",
      "year": 2023,
      "color": "Preto",
      "status": "ACTIVE"
    }
  ],
  "total": 1
}
```

---

## 10. Endpoints - Pagamento (Payment)

### 10.1 Processar Pagamento

```http
POST /api/v1/payments
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "tripId": "850e8400-e29b-41d4-a716-446655440003",
  "amount": 22.00,
  "currency": "BRL",
  "method": "CREDIT_CARD",
  "cardToken": "tok_1234567890abcdef",
  "description": "Corrida São Paulo - Av. Paulista"
}
```

**Response (201 Created)**:
```json
{
  "id": "a50e8400-e29b-41d4-a716-446655440005",
  "tripId": "850e8400-e29b-41d4-a716-446655440003",
  "amount": 22.00,
  "currency": "BRL",
  "method": "CREDIT_CARD",
  "status": "COMPLETED",
  "transactionId": "ch_3MtFBaLkdIwHu7ix28a3tqPa",
  "processedAt": "2026-01-14T13:00:05Z"
}
```

### 10.2 Solicitar Reembolso

```http
POST /api/v1/payments/refund
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "paymentId": "a50e8400-e29b-41d4-a716-446655440005",
  "amount": 22.00,
  "reason": "Trip cancelled by driver after start"
}
```

**Response (200 OK)**:
```json
{
  "id": "c50e8400-e29b-41d4-a716-446655440007",
  "paymentId": "a50e8400-e29b-41d4-a716-446655440005",
  "amount": 22.00,
  "status": "REFUNDED",
  "refundedAt": "2026-01-14T13:05:00Z"
}
```

---

## 11. Endpoints - Avaliação (Rating)

### 11.1 Criar Avaliação

```http
POST /api/v1/ratings
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "tripId": "850e8400-e29b-41d4-a716-446655440003",
  "raterType": "PASSENGER",
  "raterId": "750e8400-e29b-41d4-a716-446655440002",
  "ratedType": "DRIVER",
  "ratedId": "650e8400-e29b-41d4-a716-446655440001",
  "score": 5,
  "comment": "Excelente motorista! Muito educado e dirigiu com segurança.",
  "tags": ["POLITE", "SAFE_DRIVER", "CLEAN_CAR"]
}
```

**Response (201 Created)**:
```json
{
  "id": "d50e8400-e29b-41d4-a716-446655440008",
  "tripId": "850e8400-e29b-41d4-a716-446655440003",
  "raterType": "PASSENGER",
  "ratedType": "DRIVER",
  "score": 5,
  "comment": "Excelente motorista! Muito educado e dirigiu com segurança.",
  "tags": ["POLITE", "SAFE_DRIVER", "CLEAN_CAR"],
  "createdAt": "2026-01-14T13:10:00Z"
}
```

**Tags Disponíveis**:
- Positivas: `POLITE`, `SAFE_DRIVER`, `CLEAN_CAR`, `GOOD_CONVERSATION`, `ON_TIME`
- Negativas: `RUDE`, `UNSAFE_DRIVING`, `DIRTY_CAR`, `LATE`, `WRONG_ROUTE`

### 11.2 Buscar Avaliações do Motorista

```http
GET /api/v1/drivers/{driverId}/ratings?page=0&size=20
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "averageScore": 4.8,
  "totalRatings": 1523,
  "ratings": [
    {
      "id": "d50e8400-e29b-41d4-a716-446655440008",
      "score": 5,
      "comment": "Excelente motorista!",
      "createdAt": "2026-01-14T13:10:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalPages": 77
}
```

---

## 12. Endpoints - Notificação (Notification)

### 12.1 Listar Notificações

```http
GET /api/v1/notifications?userId={userId}&unreadOnly=true
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "notifications": [
    {
      "id": "e50e8400-e29b-41d4-a716-446655440009",
      "type": "TRIP_ASSIGNED",
      "title": "Nova corrida disponível!",
      "message": "Passageiro Maria Silva solicitou uma corrida próxima a você.",
      "data": {
        "tripId": "850e8400-e29b-41d4-a716-446655440003",
        "passengerId": "750e8400-e29b-41d4-a716-446655440002"
      },
      "read": false,
      "createdAt": "2026-01-14T12:40:30Z"
    }
  ],
  "unreadCount": 3,
  "total": 15
}
```

### 12.2 Marcar Notificação como Lida

```http
PUT /api/v1/notifications/{id}/read
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "id": "e50e8400-e29b-41d4-a716-446655440009",
  "read": true,
  "readAt": "2026-01-14T12:45:00Z"
}
```

### 12.3 Registrar Token de Push Notification

```http
POST /api/v1/notifications/register-token
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "userId": "650e8400-e29b-41d4-a716-446655440001",
  "platform": "IOS",
  "token": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]"
}
```

**Response (200 OK)**:
```json
{
  "success": true,
  "message": "Push notification token registered successfully"
}
```

---

## 13. WebSockets e Eventos em Tempo Real

### 13.1 Conectar ao WebSocket

```javascript
// URL: wss://api.rappidrive.com/ws
const socket = new WebSocket('wss://api.rappidrive.com/ws');

socket.onopen = () => {
  // Autenticar
  socket.send(JSON.stringify({
    type: 'AUTH',
    token: 'Bearer eyJhbGciOiJIUzI1NiIsInR...',
    userId: '650e8400-e29b-41d4-a716-446655440001',
    userType: 'DRIVER'
  }));
};
```

### 13.2 Eventos para Motorista

**13.2.1 Nova Corrida Disponível**
```json
{
  "type": "TRIP_REQUEST",
  "data": {
    "tripId": "850e8400-e29b-41d4-a716-446655440003",
    "passenger": {
      "name": "Maria Silva",
      "rating": 5.0
    },
    "origin": {
      "latitude": -23.5505,
      "longitude": -46.6333,
      "address": "Rua das Flores, 123"
    },
    "destination": {
      "latitude": -23.5605,
      "longitude": -46.6555,
      "address": "Av. Paulista, 1000"
    },
    "estimatedFare": 15.50,
    "distanceKm": 5.2,
    "expiresIn": 30
  }
}
```

**13.2.2 Corrida Cancelada pelo Passageiro**
```json
{
  "type": "TRIP_CANCELLED",
  "data": {
    "tripId": "850e8400-e29b-41d4-a716-446655440003",
    "cancelledBy": "PASSENGER",
    "reason": "PASSENGER_WAIT_TOO_LONG"
  }
}
```

### 13.3 Eventos para Passageiro

**13.3.1 Motorista Atribuído**
```json
{
  "type": "DRIVER_ASSIGNED",
  "data": {
    "tripId": "850e8400-e29b-41d4-a716-446655440003",
    "driver": {
      "id": "650e8400-e29b-41d4-a716-446655440001",
      "name": "João Silva",
      "phone": "+55 11 99999-9999",
      "rating": 4.8,
      "currentLocation": {
        "latitude": -23.5505,
        "longitude": -46.6333
      },
      "vehicle": {
        "brand": "Toyota",
        "model": "Corolla",
        "color": "Preto",
        "licensePlate": "ABC-1234"
      }
    },
    "estimatedArrivalMinutes": 5
  }
}
```

**13.3.2 Atualização de Localização do Motorista**
```json
{
  "type": "DRIVER_LOCATION_UPDATE",
  "data": {
    "tripId": "850e8400-e29b-41d4-a716-446655440003",
    "location": {
      "latitude": -23.5510,
      "longitude": -46.6340
    },
    "heading": 180.5,
    "speed": 35.2,
    "estimatedArrivalMinutes": 4
  }
}
```

**13.3.3 Motorista Chegou**
```json
{
  "type": "DRIVER_ARRIVED",
  "data": {
    "tripId": "850e8400-e29b-41d4-a716-446655440003",
    "arrivedAt": "2026-01-14T12:44:00Z"
  }
}
```

**13.3.4 Corrida Iniciada**
```json
{
  "type": "TRIP_STARTED",
  "data": {
    "tripId": "850e8400-e29b-41d4-a716-446655440003",
    "startedAt": "2026-01-14T12:45:00Z"
  }
}
```

**13.3.5 Corrida Concluída**
```json
{
  "type": "TRIP_COMPLETED",
  "data": {
    "tripId": "850e8400-e29b-41d4-a716-446655440003",
    "completedAt": "2026-01-14T13:00:00Z",
    "fare": {
      "amount": 22.00,
      "currency": "BRL"
    }
  }
}
```

---

## 14. Geolocalização e MapBox

### 14.1 Calcular Tarifa Estimada

```http
POST /api/v1/fares/calculate
Authorization: Bearer <TOKEN>
Content-Type: application/json
```

**Request Body**:
```json
{
  "origin": {
    "latitude": -23.5505,
    "longitude": -46.6333
  },
  "destination": {
    "latitude": -23.5605,
    "longitude": -46.6555
  },
  "vehicleType": "STANDARD"
}
```

**Response (200 OK)**:
```json
{
  "baseFare": 5.00,
  "distanceFare": 13.00,
  "timeFare": 0.00,
  "totalFare": 18.00,
  "currency": "BRL",
  "estimatedDistanceKm": 5.2,
  "estimatedDurationMinutes": 12,
  "breakdown": {
    "baseFareDescription": "Tarifa mínima",
    "distanceFareDescription": "R$ 2,50 por km × 5.2 km",
    "timeFareDescription": "Tempo estimado não aplicado"
  }
}
```

### 14.2 Buscar Endereço por Coordenadas (Reverse Geocoding)

```http
GET /api/v1/geocoding/reverse?latitude=-23.5505&longitude=-46.6333
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "address": "Rua das Flores, 123",
  "neighborhood": "Jardins",
  "city": "São Paulo",
  "state": "SP",
  "country": "Brasil",
  "postalCode": "01452-000",
  "formattedAddress": "Rua das Flores, 123 - Jardins, São Paulo - SP, 01452-000"
}
```

### 14.3 Buscar Coordenadas por Endereço (Geocoding)

```http
GET /api/v1/geocoding/forward?address=Av.%20Paulista,%201000,%20São%20Paulo
Authorization: Bearer <TOKEN>
```

**Response (200 OK)**:
```json
{
  "latitude": -23.5605,
  "longitude": -46.6555,
  "address": "Av. Paulista, 1000",
  "confidence": 0.95
}
```

---

## 15. Fluxos Principais

### 15.1 Fluxo Completo - Passageiro

```
1. Passageiro abre app → GET /passengers/me
2. Passageiro seleciona origem e destino → POST /fares/calculate
3. Passageiro confirma → POST /trips (status: REQUESTED)
4. WebSocket: Aguardando motorista...
5. WebSocket recebe: DRIVER_ASSIGNED
6. App exibe: Motorista João chegando em 5 min
7. WebSocket recebe: DRIVER_LOCATION_UPDATE (a cada 5s)
8. App atualiza mapa com localização do motorista
9. WebSocket recebe: DRIVER_ARRIVED
10. App mostra: "Motorista chegou!"
11. WebSocket recebe: TRIP_STARTED
12. App entra em modo "Em viagem"
13. WebSocket recebe: TRIP_COMPLETED
14. App exibe resumo da corrida e solicita pagamento
15. Passageiro confirma → POST /trips/{id}/complete-with-payment
16. App solicita avaliação → POST /ratings
17. Fim
```

### 15.2 Fluxo Completo - Motorista

```
1. Motorista abre app → GET /drivers/me
2. Motorista clica "Ficar Online" → PUT /drivers/{id}/activate
3. App inicia envio de localização → PUT /drivers/{id}/location (a cada 5s)
4. WebSocket recebe: TRIP_REQUEST
5. App exibe: "Nova corrida! Aceitar?"
6. Motorista aceita → POST /trips/{id}/assign
7. App entra em modo "Indo buscar passageiro"
8. Motorista chega → App detecta proximidade (Geofence)
9. App envia: POST /trips/{id}/notify-arrival
10. WebSocket envia para passageiro: DRIVER_ARRIVED
11. Motorista clica "Iniciar" → POST /trips/{id}/start
12. App entra em modo "Em viagem"
13. Motorista segue rota até destino
14. Motorista clica "Finalizar" → POST /trips/{id}/complete-with-payment
15. App exibe: "Corrida concluída! R$ 22,00"
16. App solicita avaliação → POST /ratings
17. Motorista fica disponível novamente
18. Fim
```

### 15.3 Fluxo de Cancelamento

**Por Passageiro**:
```
1. Passageiro cancela → POST /trips/{id}/cancel
2. Backend calcula tarifa (se aplicável)
3. Backend processa pagamento da taxa
4. WebSocket notifica motorista: TRIP_CANCELLED
5. Motorista fica disponível novamente
```

**Por Motorista**:
```
1. Motorista cancela → POST /trips/{id}/cancel
2. Backend registra cancelamento (sem taxa para motorista)
3. WebSocket notifica passageiro: TRIP_CANCELLED
4. Passageiro pode solicitar nova corrida
```

---

## 16. Tratamento de Erros

### 16.1 Erros Comuns

**401 Unauthorized** - Token inválido ou expirado
```json
{
  "timestamp": "2026-01-14T12:30:45Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token has expired",
  "path": "/api/v1/trips"
}
```

**Ação**: Fazer refresh do token ou solicitar novo login.

**404 Not Found** - Recurso não encontrado
```json
{
  "timestamp": "2026-01-14T12:30:45Z",
  "status": 404,
  "error": "Not Found",
  "message": "Trip not found with id: 850e8400-...",
  "path": "/api/v1/trips/850e8400-..."
}
```

**409 Conflict** - Conflito de estado
```json
{
  "timestamp": "2026-01-14T12:30:45Z",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot cancel a completed trip",
  "path": "/api/v1/trips/850e8400-.../cancel"
}
```

**500 Internal Server Error** - Erro no servidor
```json
{
  "timestamp": "2026-01-14T12:30:45Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later.",
  "path": "/api/v1/trips",
  "traceId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Ação**: Mostrar mensagem genérica ao usuário e reportar `traceId` para suporte.

---

## 17. Paginação

Todos os endpoints que retornam listas suportam paginação:

```http
GET /api/v1/passengers/{passengerId}/trips?page=0&size=20&sort=createdAt,desc
```

**Query Parameters**:
- `page` (default: 0): Número da página (zero-based)
- `size` (default: 20): Itens por página
- `sort` (optional): Campo de ordenação (ex: `createdAt,desc`)

**Response**:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 145,
  "totalPages": 8,
  "first": true,
  "last": false,
  "numberOfElements": 20
}
```

---

## 18. Rate Limiting

**Limites**:
- **Autenticado**: 1000 requests/hora por usuário
- **Não autenticado**: 100 requests/hora por IP
- **Location updates**: 720 requests/hora (1 a cada 5s)

**Headers de resposta**:
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1642176000
```

**Quando exceder**:
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again in 45 seconds.",
  "retryAfter": 45
}
```

---

## 19. Versionamento

**URL versionada**: `/api/v1/...`

Quando houver breaking changes, será lançada `/api/v2/...`

Versões antigas suportadas por **6 meses** após lançamento da nova versão.

---

## 20. Ambiente de Desenvolvimento

### 20.1 Credenciais de Teste

**Staging API**: `https://staging-api.rappidrive.com`

**Motorista de Teste**:
```
Email: driver.test@rappidrive.com
Password: Test@123
TenantId: 550e8400-e29b-41d4-a716-446655440000
```

**Passageiro de Teste**:
```
Email: passenger.test@rappidrive.com
Password: Test@123
TenantId: 550e8400-e29b-41d4-a716-446655440000
```

**Cartão de Crédito de Teste (Stripe)**:
```
Número: 4242 4242 4242 4242
Validade: 12/34
CVV: 123
```

### 20.2 Ferramentas Recomendadas

- **Postman Collection**: [Download aqui](https://api.rappidrive.com/postman-collection.json)
- **Swagger UI**: https://staging-api.rappidrive.com/swagger-ui.html
- **Health Check**: https://staging-api.rappidrive.com/actuator/health

---

## 21. Boas Práticas

### 21.1 Gerenciamento de Token

```swift
// Swift - Exemplo
class AuthManager {
    var accessToken: String?
    var refreshToken: String?
    var tokenExpiry: Date?
    
    func isTokenExpired() -> Bool {
        guard let expiry = tokenExpiry else { return true }
        return Date() > expiry.addingTimeInterval(-300) // Refresh 5min antes
    }
    
    func refreshTokenIfNeeded() async {
        if isTokenExpired() {
            await refreshAccessToken()
        }
    }
}
```

### 21.2 Retry Logic

```swift
func performRequest(retryCount: Int = 0) async throws -> Response {
    do {
        return try await networkClient.execute(request)
    } catch {
        if retryCount < 3 && error.isRetryable {
            let delay = pow(2.0, Double(retryCount)) // Exponential backoff
            try await Task.sleep(nanoseconds: UInt64(delay * 1_000_000_000))
            return try await performRequest(retryCount: retryCount + 1)
        }
        throw error
    }
}
```

### 21.3 Atualização de Localização Eficiente

```swift
// Enviar apenas quando houve mudança significativa
var lastSentLocation: CLLocation?

func shouldSendLocationUpdate(_ newLocation: CLLocation) -> Bool {
    guard let lastLocation = lastSentLocation else { return true }
    
    let distance = newLocation.distance(from: lastLocation)
    let timeDiff = newLocation.timestamp.timeIntervalSince(lastLocation.timestamp)
    
    // Enviar se: moveu > 50m OU passou > 10s
    return distance > 50 || timeDiff > 10
}
```

### 21.4 Handling de WebSocket Reconnection

```swift
class WebSocketManager {
    var reconnectAttempts = 0
    let maxReconnectAttempts = 10
    
    func connect() {
        socket.onDisconnect { [weak self] reason in
            self?.handleDisconnect(reason)
        }
    }
    
    func handleDisconnect(_ reason: String) {
        guard reconnectAttempts < maxReconnectAttempts else {
            showError("Connection lost. Please restart the app.")
            return
        }
        
        reconnectAttempts += 1
        let delay = min(30, pow(2.0, Double(reconnectAttempts)))
        
        DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
            self.connect()
        }
    }
}
```

---

## 22. SDKs e Bibliotecas Recomendadas

### 22.1 iOS (Swift)

```swift
// Networking
import Alamofire

// WebSocket
import Starscream

// Maps
import MapboxMaps

// Push Notifications
import FirebaseMessaging

// Analytics
import FirebaseAnalytics
```

### 22.2 Android (Kotlin)

```kotlin
// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'

// WebSocket
implementation 'com.squareup.okhttp3:okhttp:4.11.0'

// Maps
implementation 'com.mapbox.maps:android:10.16.0'

// Push Notifications
implementation 'com.google.firebase:firebase-messaging:23.3.1'

// Location
implementation 'com.google.android.gms:play-services-location:21.0.1'
```

### 22.3 Exemplo de Client HTTP (Swift)

```swift
class RappiDriveAPIClient {
    static let shared = RappiDriveAPIClient()
    
    private let baseURL = "https://api.rappidrive.com"
    private let session = URLSession.shared
    
    func createTrip(origin: Location, destination: Location) async throws -> Trip {
        let endpoint = "\(baseURL)/api/v1/trips"
        
        var request = URLRequest(url: URL(string: endpoint)!)
        request.httpMethod = "POST"
        request.setValue("Bearer \(AuthManager.shared.accessToken!)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "tenantId": AuthManager.shared.tenantId,
            "passengerId": AuthManager.shared.userId,
            "origin": [
                "latitude": origin.latitude,
                "longitude": origin.longitude
            ],
            "destination": [
                "latitude": destination.latitude,
                "longitude": destination.longitude
            ]
        ]
        
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw APIError.invalidResponse
        }
        
        return try JSONDecoder().decode(Trip.self, from: data)
    }
}
```

---

## Changelog

| Versão | Data | Mudanças |
|--------|------|----------|
| 1.0 | 2026-01-14 | Documentação inicial completa |

---

## Suporte

- **Email**: mobile-support@rappidrive.com
- **Slack**: #mobile-dev (workspace RappiDrive)
- **Status Page**: https://status.rappidrive.com

---

**Última atualização**: 14 de janeiro de 2026
