# Tenant Onboarding - Exemplos Práticos de Uso

## 📖 Guia Prático com Exemplos Reais

---

## 1. Exemplo cURL Completo

### Onboarding de Goiânia

```bash
# Salvar em onboard-goiania.json
cat > onboard-goiania.json << 'EOF'
{
  "tenantId": "goiania-go",
  "displayName": "RappiDrive Goiânia",
  "adminEmail": "rafael.costa@goiania.rappidrive.com",
  "currency": "BRL",
  "baseFare": 5.00,
  "pricePerKm": 2.50,
  "pricePerMin": 0.30,
  "serviceAreaName": "Centro de Goiânia",
  "geoJsonPolygon": {
    "type": "Polygon",
    "coordinates": [
      [
        [-49.26, -15.79],
        [-49.26, -15.88],
        [-49.16, -15.88],
        [-49.16, -15.79],
        [-49.26, -15.79]
      ]
    ]
  }
}
EOF

# Obter JWT token (exemplo)
JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Fazer requisição
curl -v -X POST http://localhost:8080/api/admin/tenants \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d @onboard-goiania.json | jq .
```

**Resposta esperada (201 Created)**:
```json
{
  "tenant_id": "goiania-go",
  "display_name": "RappiDrive Goiânia",
  "admin_email": "rafael.costa@goiania.rappidrive.com",
  "temporary_password": "aB7cDeFgHiJkLmNoP",
  "keycloak_user_id": "550e8400-e29b-41d4-a716-446655440001",
  "keycloak_group_id": "650e8400-e29b-41d4-a716-446655440002",
  "service_area_id": "750e8400-e29b-41d4-a716-446655440003",
  "fare_configuration_id": "850e8400-e29b-41d4-a716-446655440004",
  "message": "Tenant 'RappiDrive Goiânia' onboarded successfully. Admin user created: rafael.costa@goiania.rappidrive.com. User must change temporary password on first login."
}
```

---

## 2. Exemplo Java/Spring Boot

### Criar novo tenant programaticamente

```java
@Service
public class TenantOnboardingService {
    
    private final OnboardNewTenantInputPort onboardTenantUseCase;
    private final RestTemplate restTemplate;
    
    public TenantOnboardingService(OnboardNewTenantInputPort onboardTenantUseCase,
                                  RestTemplate restTemplate) {
        this.onboardTenantUseCase = onboardTenantUseCase;
        this.restTemplate = restTemplate;
    }
    
    /**
     * Onboard a new tenant programmatically.
     */
    public void onboardSãoPauloCity() {
        // 1. Preparar dados
        TenantId tenantId = TenantId.of("sao-paulo-sp");
        Email adminEmail = Email.of("operacoes@saopaulo.rappidrive.com");
        String displayName = "RappiDrive São Paulo";
        String currency = "BRL";
        BigDecimal baseFare = new BigDecimal("6.00");
        BigDecimal pricePerKm = new BigDecimal("2.75");
        BigDecimal pricePerMin = new BigDecimal("0.35");
        String serviceAreaName = "Zona Centro-Leste";
        
        // GeoJSON: Centro de SP
        String geoJsonPolygon = """
                {
                  "type": "Polygon",
                  "coordinates": [
                    [
                      [-46.63, -23.54],
                      [-46.63, -23.56],
                      [-46.61, -23.56],
                      [-46.61, -23.54],
                      [-46.63, -23.54]
                    ]
                  ]
                }
                """;
        
        // 2. Criar comando
        OnboardNewTenantInputPort.OnboardingCommand command = 
                new OnboardNewTenantInputPort.OnboardingCommand(
                        tenantId,
                        displayName,
                        adminEmail,
                        currency,
                        baseFare,
                        pricePerKm,
                        pricePerMin,
                        serviceAreaName,
                        geoJsonPolygon
                );
        
        // 3. Executar use case
        try {
            OnboardNewTenantInputPort.OnboardingResult result = 
                    onboardTenantUseCase.execute(command);
            
            // 4. Processar resultado
            System.out.println("✅ Tenant onboarded successfully!");
            System.out.println("Admin Email: " + result.adminEmail());
            System.out.println("Temporary Password: " + result.temporaryPassword());
            System.out.println("Keycloak User ID: " + result.keycloakUserId());
            System.out.println("Service Area ID: " + result.serviceAreaId());
            
            // 5. Enviar email com credenciais (exemplo)
            sendWelcomeEmail(result);
            
        } catch (TenantAlreadyExistsException e) {
            System.err.println("❌ Tenant already exists: " + e.getMessage());
        } catch (IdentityProvisioningException e) {
            System.err.println("❌ Keycloak error: " + e.getMessage());
        }
    }
    
    private void sendWelcomeEmail(OnboardNewTenantInputPort.OnboardingResult result) {
        // Enviar email com credenciais para admin
        String emailBody = String.format(
                "Bem-vindo ao RappiDrive!\n\n" +
                "Seu usuário foi criado com sucesso.\n" +
                "Email: %s\n" +
                "Senha temporária: %s\n\n" +
                "Você deve mudar a senha no primeiro login.\n" +
                "Acesse: https://app.rappidrive.com/login",
                result.adminEmail(),
                result.temporaryPassword()
        );
        
        // Implementar envio de email
        // emailService.send(result.adminEmail(), "Bem-vindo ao RappiDrive", emailBody);
    }
}
```

### Usar o service

```java
@SpringBootApplication
public class RappiDriveApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RappiDriveApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner onboardTenants(TenantOnboardingService service) {
        return args -> {
            System.out.println("🚀 Starting tenant onboarding...");
            service.onboardSãoPauloCity();
        };
    }
}
```

---

## 3. Exemplo com validação de GeoJSON

### Obter coordenadas de um mapa

```java
public class GeoJsonBuilder {
    
    /**
     * Converter lista de coordenadas em GeoJSON Polygon.
     * Coordenadas devem formar um polígono fechado.
     */
    public static String buildPolygonFromCoordinates(List<Location> locations) {
        if (locations.size() < 4) {
            throw new IllegalArgumentException("At least 4 points needed for a polygon");
        }
        
        StringBuilder coords = new StringBuilder();
        coords.append("[");
        
        // Adicionar todos os pontos
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            coords.append("[").append(loc.getLongitude()).append(", ")
                  .append(loc.getLatitude()).append("]");
            if (i < locations.size() - 1) {
                coords.append(", ");
            }
        }
        
        // Fechar o polígono (repetir o primeiro ponto)
        Location first = locations.get(0);
        coords.append(", [").append(first.getLongitude()).append(", ")
              .append(first.getLatitude()).append("]");
        
        coords.append("]");
        
        return String.format(
                "{\"type\": \"Polygon\", \"coordinates\": [%s]}",
                coords
        );
    }
}

// Uso
@Test
public void buildGeoJsonFromMapMarkers() {
    List<Location> locations = List.of(
            new Location(-49.26, -15.79),  // Canto superior-esquerdo
            new Location(-49.26, -15.88),  // Canto inferior-esquerdo
            new Location(-49.16, -15.88),  // Canto inferior-direito
            new Location(-49.16, -15.79)   // Canto superior-direito
    );
    
    String geoJson = GeoJsonBuilder.buildPolygonFromCoordinates(locations);
    
    System.out.println("GeoJSON gerado:");
    System.out.println(geoJson);
    
    // Usar no onboarding
    // ...
}
```

---

## 4. Exemplo de Script para Batch Onboarding

### Onboarding de múltiplas cidades

```java
@Service
public class BatchTenantOnboardingService {
    
    private final OnboardNewTenantInputPort onboardTenantUseCase;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public BatchTenantOnboardingService(OnboardNewTenantInputPort onboardTenantUseCase) {
        this.onboardTenantUseCase = onboardTenantUseCase;
    }
    
    /**
     * Onboard múltiplos tenants de um CSV.
     * Formato: tenantId,displayName,adminEmail,currency,baseFare,pricePerKm,...
     */
    public void onboardFromCsv(String csvPath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(csvPath));
        
        int success = 0;
        int failed = 0;
        
        for (int i = 1; i < lines.size(); i++) { // Skip header
            String line = lines.get(i);
            String[] fields = line.split(",");
            
            try {
                OnboardNewTenantInputPort.OnboardingCommand command = 
                        parseCommand(fields);
                
                var result = onboardTenantUseCase.execute(command);
                success++;
                
                log.info("✅ Onboarded: {} ({})", 
                        fields[1], result.keycloakUserId());
                        
            } catch (Exception e) {
                failed++;
                log.error("❌ Failed to onboard {}: {}", 
                        fields[0], e.getMessage());
            }
        }
        
        log.info("📊 Batch completed: {} success, {} failed", success, failed);
    }
    
    private OnboardNewTenantInputPort.OnboardingCommand parseCommand(String[] fields) {
        return new OnboardNewTenantInputPort.OnboardingCommand(
                TenantId.of(fields[0]),      // tenantId
                fields[1],                    // displayName
                Email.of(fields[2]),          // adminEmail
                fields[3],                    // currency
                new BigDecimal(fields[4]),    // baseFare
                new BigDecimal(fields[5]),    // pricePerKm
                new BigDecimal(fields[6]),    // pricePerMin
                fields[7],                    // serviceAreaName
                fields[8]                     // geoJsonPolygon
        );
    }
}
```

### CSV de entrada (tenants.csv)

```csv
tenantId,displayName,adminEmail,currency,baseFare,pricePerKm,pricePerMin,serviceAreaName,geoJsonPolygon
goiania-go,RappiDrive Goiânia,admin@goiania.rappidrive.com,BRL,5.00,2.50,0.30,Centro de Goiânia,"{""type"":""Polygon"",""coordinates"":[[[-49.26,-15.79],[-49.26,-15.88],[-49.16,-15.88],[-49.16,-15.79],[-49.26,-15.79]]]}"
brasilia-df,RappiDrive Brasília,admin@brasilia.rappidrive.com,BRL,5.50,2.75,0.35,Eixo Monumental,"{""type"":""Polygon"",""coordinates":[[[-47.9,-15.8],[-47.9,-15.9],[-48.0,-15.9],[-48.0,-15.8],[-47.9,-15.8]]]}"
```

### Usar

```java
@SpringBootApplication
public class BatchOnboardingApp {
    
    public static void main(String[] args) throws IOException {
        ApplicationContext context = SpringApplication.run(this.getClass(), args);
        
        BatchTenantOnboardingService service = 
                context.getBean(BatchTenantOnboardingService.class);
        
        service.onboardFromCsv("tenants.csv");
    }
}
```

---

## 5. Exemplo de Listener para Evento

### Enviar email após onboarding

```java
@Component
public class TenantOnboardingEmailListener {
    
    private final EmailService emailService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public TenantOnboardingEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Escuta TenantOnboardedEvent e envia email de boas-vindas.
     * Implementar quando estiver disponível o EventPublisher.
     */
    @EventListener
    public void onTenantOnboarded(TenantOnboardedEvent event) {
        log.info("🎉 Processing onboarding event for: {}", event.getTenantId());
        
        try {
            sendWelcomeEmail(event);
            sendOperationsNotification(event);
            logAnalytics(event);
        } catch (Exception e) {
            log.error("Error processing onboarding event", e);
            // Não falha o fluxo se email fails
        }
    }
    
    private void sendWelcomeEmail(TenantOnboardedEvent event) {
        String subject = "🎉 Bem-vindo ao RappiDrive - " + event.getDisplayName();
        String body = String.format(
                """
                Olá!
                
                Parabéns! Sua plataforma RappiDrive foi criada com sucesso.
                
                🏙️ Cidade: %s
                📧 Email Admin: %s
                
                Próximos passos:
                1. Acesse o painel administrativo
                2. Cadastre motoristas
                3. Configure zonas de entrega
                4. Inicie as operações
                
                Suporte: support@rappidrive.com
                """,
                event.getDisplayName(),
                event.getAdminEmail()
        );
        
        emailService.send(
                event.getAdminEmail(),
                subject,
                body
        );
        
        log.info("✅ Welcome email sent to: {}", event.getAdminEmail());
    }
    
    private void sendOperationsNotification(TenantOnboardedEvent event) {
        String slackMessage = String.format(
                "🚀 Novo tenant onboarded!\n" +
                "Cidade: %s\n" +
                "Admin: %s\n" +
                "Timestamp: %s",
                event.getDisplayName(),
                event.getAdminEmail(),
                event.getOccurredAt()
        );
        
        // emailService.sendSlack("#operations", slackMessage);
        log.info("📢 Operations notified: {}", event.getDisplayName());
    }
    
    private void logAnalytics(TenantOnboardedEvent event) {
        // Enviar para analytics/metrics service
        // analyticsService.trackEvent("tenant.onboarded", 
        //     Map.of("tenantId", event.getTenantId().getValue(),
        //            "timestamp", event.getOccurredAt())
        // );
        log.info("📊 Analytics event logged");
    }
}
```

---

## 6. Exemplo de Teste Unitário

```java
@ExtendWith(MockitoExtension.class)
class OnboardNewTenantUseCaseTest {
    
    @Mock
    private ServiceAreaRepositoryPort serviceAreaRepository;
    
    @Mock
    private FareConfigurationRepositoryPort fareConfigRepository;
    
    @Mock
    private IdentityProvisioningPort identityProvisioning;
    
    @Mock
    private ParallelExecutor parallelExecutor;
    
    @InjectMocks
    private OnboardNewTenantUseCase useCase;
    
    @Test
    void shouldOnboardNewTenantSuccessfully() {
        // Arrange
        TenantId tenantId = TenantId.of("test-city");
        Email adminEmail = Email.of("admin@test.com");
        
        OnboardNewTenantInputPort.OnboardingCommand command = 
                new OnboardNewTenantInputPort.OnboardingCommand(
                        tenantId,
                        "Test City",
                        adminEmail,
                        "BRL",
                        new BigDecimal("5.00"),
                        new BigDecimal("2.50"),
                        new BigDecimal("0.30"),
                        "Test Area",
                        "{\"type\":\"Polygon\",\"coordinates\":[[...]]}"
                );
        
        // Mock Keycloak responses
        when(identityProvisioning.createTenantAdmin(tenantId, adminEmail, anyString()))
                .thenReturn("keycloak-user-id-123");
        
        when(identityProvisioning.createTenantGroup(tenantId))
                .thenReturn("keycloak-group-id-456");
        
        when(serviceAreaRepository.existsByTenantId(tenantId))
                .thenReturn(false);
        
        when(identityProvisioning.tenantGroupExists(tenantId))
                .thenReturn(false);
        
        // Act
        var result = useCase.execute(command);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.tenantId()).isEqualTo(tenantId);
        assertThat(result.keycloakUserId()).isEqualTo("keycloak-user-id-123");
        assertThat(result.keycloakGroupId()).isEqualTo("keycloak-group-id-456");
        
        // Verify interactions
        verify(identityProvisioning).createTenantAdmin(eq(tenantId), eq(adminEmail), anyString());
        verify(identityProvisioning).createTenantGroup(tenantId);
        verify(fareConfigRepository).save(any());
        verify(serviceAreaRepository).save(any());
    }
    
    @Test
    void shouldFailWhenTenantAlreadyExists() {
        // Arrange
        TenantId tenantId = TenantId.of("existing-city");
        
        when(serviceAreaRepository.existsByTenantId(tenantId))
                .thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(createCommand(tenantId)))
                .isInstanceOf(TenantAlreadyExistsException.class);
    }
    
    private OnboardNewTenantInputPort.OnboardingCommand createCommand(TenantId tenantId) {
        return new OnboardNewTenantInputPort.OnboardingCommand(
                tenantId,
                "Test",
                Email.of("test@test.com"),
                "BRL",
                new BigDecimal("5.00"),
                new BigDecimal("2.50"),
                new BigDecimal("0.30"),
                "Test",
                "{\"type\":\"Polygon\",\"coordinates\":[[...]]}"
        );
    }
}
```

---

## 7. Exemplo de Postman Collection

### Importar no Postman

```json
{
  "info": {
    "name": "RappiDrive Admin API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Onboard New Tenant",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{JWT_SUPER_ADMIN}}",
            "type": "text"
          },
          {
            "key": "Content-Type",
            "value": "application/json",
            "type": "text"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"tenantId\": \"curitiba-pr\",\n  \"displayName\": \"RappiDrive Curitiba\",\n  \"adminEmail\": \"admin@curitiba.rappidrive.com\",\n  \"currency\": \"BRL\",\n  \"baseFare\": 5.25,\n  \"pricePerKm\": 2.60,\n  \"pricePerMin\": 0.32,\n  \"serviceAreaName\": \"Bairro Alto\",\n  \"geoJsonPolygon\": \"{\\\"type\\\":\\\"Polygon\\\",\\\"coordinates\\\":[[[...]]]}\" \n}"
        },
        "url": {
          "raw": "{{BASE_URL}}/api/admin/tenants",
          "host": ["{{BASE_URL}}"],
          "path": ["api", "admin", "tenants"]
        }
      }
    },
    {
      "name": "Admin API Health",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{JWT_SUPER_ADMIN}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{BASE_URL}}/api/admin/tenants/health",
          "host": ["{{BASE_URL}}"],
          "path": ["api", "admin", "tenants", "health"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "BASE_URL",
      "value": "http://localhost:8080"
    },
    {
      "key": "JWT_SUPER_ADMIN",
      "value": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  ]
}
```

---

## 8. Troubleshooting

### ❌ Erro: "401 Unauthorized"

**Causa**: Token JWT não é válido ou expirou

**Solução**:
```bash
# Gerar novo token
curl -X POST http://localhost:8080/auth/realms/rappidrive/protocol/openid-connect/token \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_secret=xxx"
```

### ❌ Erro: "403 Forbidden"

**Causa**: Usuário não tem `ROLE_SUPER_ADMIN`

**Solução**: Adicionar role no Keycloak
```bash
# Via Keycloak Admin Console
1. Ir para Clients → admin-cli
2. Service Account Roles → Realm Roles
3. Adicionar SUPER_ADMIN
```

### ❌ Erro: "409 Conflict - Tenant already exists"

**Causa**: Tenant com mesmo ID já foi criado

**Solução**: Usar `tenantId` único ou deletar primeiro

### ❌ Erro: "422 Unprocessable Entity"

**Causa**: GeoJSON inválido

**Solução**: Validar em https://geojson.io/

---

**Data**: 14 de janeiro de 2026
