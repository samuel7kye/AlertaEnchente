# Alerta Enchentes - Itajubá/MG

Painel comunitário de monitoramento e alertas de chuva para Itajubá/MG. Reconstrução do projeto original (Next.js) usando **Java 21 + Spring Boot 3.3** com **PostgreSQL**.

## 📋 Funcionalidades

- **Dashboard público** com situação dos pontos de risco em tempo real
- **Régua linimétrica visual** (staff gauge) para cada ponto monitorado
- **Sistema de avisos** da equipe (amarelo/vermelho/verde) com atualização automática de status dos pontos
- **Relatos da comunidade** com envio de fotos (base64)
- **Dados meteorológicos** via API pública Open-Meteo
- **Painel administrativo** protegido por token (Bearer)
- **Persistência em PostgreSQL** com migrações Flyway
- **Design responsivo** fiel ao original (CSS custom properties, fontes Google Fonts)

## 🛠 Stack Tecnológica

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Template Engine | Thymeleaf |
| Banco de Dados | PostgreSQL 16+ |
| Migração | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Segurança | Spring Security (filtro custom Bearer token) |
| HTTP Client | WebClient (reactive) |
| Build | Maven |
| Frontend | HTML5 + CSS3 (custom properties) + Vanilla JS |

## 🚀 Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.9+
- PostgreSQL 16+ rodando localmente

### Configuração do Banco
```sql
CREATE DATABASE alerta_enchentes;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE alerta_enchentes TO postgres;
```

### Variáveis de Ambiente
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export ADMIN_TOKEN=alerta-admin-2024
export ADMIN_PASSWORD=admin123
```

### Build e Execução
```bash
# Clone e entre no diretório
cd AlertaEnchentes-SpringBoot

# Build
./mvnw clean package

# Execute
./mvnw spring-boot:run
# ou
java -jar target/alerta-enchentes-1.0.0-SNAPSHOT.jar
```

A aplicação sobe em **http://localhost:8080**

## 📡 API Endpoints

### Públicos
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/` | Página principal (Thymeleaf) |
| GET | `/api/status` | Status completo (pontos, alertas, relatos) |
| GET | `/api/weather` | Dados meteorológicos Open-Meteo |
| GET | `/api/health` | Health check |
| POST | `/api/relatos` | Enviar relato da comunidade |

### Administrativos (requer `Authorization: Bearer <token>`)
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/admin/session` | Verificar token admin |
| POST | `/api/admin/alertas` | Publicar aviso (atualiza ponto) |
| PATCH | `/api/admin/pontos/{id}` | Atualizar status do ponto |

## 🗂 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/alertaenchentes/
│   │   ├── AlertaEnchentesApplication.java
│   │   ├── config/          # DataInitializer, WebConfig
│   │   ├── controller/      # PageController, StatusController, RelatoController, AdminController
│   │   ├── model/           # Entidades JPA + DTOs/Requests
│   │   ├── repository/      # Spring Data JPA Repositories
│   │   ├── security/        # SecurityConfig, AdminTokenFilter
│   │   └── service/         # Business logic services
│   └── resources/
│       ├── application.yml
│       ├── db/migration/    # Flyway SQL migrations
│       ├── static/
│       │   ├── css/styles.css
│       │   └── js/main.js
│       └── templates/
│           ├── index.html
│           └── fragments/banner.html
└── test/
```

## 🎨 Design System (CSS)

O projeto mantém o design original com:
- **Cores semânticas** via CSS custom properties (`--ok`, `--warn`, `--red`, `--river`)
- **Modo escuro automático** via `prefers-color-scheme`
- **Tipografia**: Archivo (display), Source Sans 3 (body), IBM Plex Mono (mono)
- **Régua linimétrica animada** (`.staff i` com `transition: height`)
- **Layout responsivo** (grid + flexbox, breakpoints 760px)

## 🔐 Segurança

- Token admin configurável via `app.admin.token` (env `ADMIN_TOKEN`)
- Filtro `AdminTokenFilter` intercepta rotas `/api/admin/**`, `/api/alertas`, `/api/pontos/**`
- Autenticação stateless (Bearer token no header)
- CSRF desabilitado para API REST

## 📊 Monitoramento

- Spring Boot Actuator em `/actuator/health`, `/actuator/info`, `/actuator/metrics`
- Logs estruturados (SLF4J + Logback)

## 🧪 Testes

```bash
./mvnw test
```

Usa Testcontainers para PostgreSQL real nos testes de integração.

## 📝 Licença

Projeto acadêmico - Projeto Bota pra Fazer · Itajubá/MG