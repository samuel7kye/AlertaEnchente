# Alerta Enchentes Itajubá

Painel comunitário do Projeto Bota pra Fazer. A página reúne a situação dos pontos de risco, avisos da equipe, relatos com fotos, contatos de emergência e a previsão de chuva para Itajubá/MG. A interface usa HTML, CSS e JavaScript servidos pelo Spring Boot.

## Iniciar com o banco local já configurado

Neste computador, execute `./iniciar-local.ps1` na pasta do projeto. O script inicia o banco dedicado na porta 5433 e em seguida inicia o Spring Boot. Abra [http://localhost:8080](http://localhost:8080).

Use `./iniciar-local.ps1 -SomenteBanco` para iniciar apenas o banco e rodar o Maven em outro terminal. A configuração da conexão fica em `config/application.properties`, um arquivo local ignorado pelo Git. Para usar outro PostgreSQL, configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` como indicado abaixo. A pasta de dados da instância local fica em `%LOCALAPPDATA%\AlertaEnchentes\PostgreSQL16`.

## Executar no Windows

Use um JDK 21 ou mais recente e um banco PostgreSQL 16 ou mais recente. Crie o banco uma vez, pelo pgAdmin ou pelo `psql`, com um usuário autorizado a criar tabelas nesse banco.

```sql
CREATE DATABASE alerta_enchentes;
```

Na pasta do projeto, execute no PowerShell.

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/alerta_enchentes'
$env:DB_USERNAME = 'postgres'
$env:DB_PASSWORD = Read-Host 'Senha do PostgreSQL' -MaskInput

$env:ADMIN_EMAIL = Read-Host 'E-mail da equipe'
$env:ADMIN_PASSWORD = Read-Host 'Senha da equipe' -MaskInput

.\mvnw.cmd spring-boot:run
```

Abra [http://localhost:8080](http://localhost:8080). No Linux ou macOS, configure as mesmas variáveis e execute `sh ./mvnw spring-boot:run`. O Maven Wrapper baixa o Maven na primeira execução.

O Flyway cria as tabelas e cadastra os pontos de Varginha, Medicina e São Sebastião. O mesmo projeto pode usar um PostgreSQL gerenciado. Informe a URL JDBC e as credenciais do banco nas variáveis acima, incluindo `sslmode=require` na URL quando o provedor exigir TLS.

Configure `ADMIN_EMAIL` e `ADMIN_PASSWORD` para habilitar o acesso da equipe. O login usa uma sessão Spring Security. Para clientes de API existentes, `ADMIN_TOKEN` habilita autenticação opcional por `Authorization: Bearer <token>` nas rotas protegidas.

## Coleta meteorológica

O Spring Boot consulta a [API Open-Meteo](https://open-meteo.com/en/docs) na inicialização e a cada 15 minutos enquanto o servidor estiver em execução. Cada coleta válida gera uma linha em `weather_readings`, com o horário informado pelo provedor, o horário da coleta, a precipitação estimada e a maior probabilidade de chuva nas próximas seis horas.

O painel lê a última coleta em `GET /api/weather`. Se uma coleta falhar, a leitura anterior continua disponível e é identificada como desatualizada após 16 minutos no intervalo padrão. Antes da primeira coleta válida, o endpoint retorna HTTP 503 e o painel informa a indisponibilidade. Para testes locais, `WEATHER_REFRESH_INTERVAL` aceita uma duração ISO 8601, como `PT5S`, e `WEATHER_API_URL` permite apontar para um servidor de teste.

## Compilação e testes

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
java -jar target/alerta-enchentes-1.0.0-SNAPSHOT.jar
```

O site é um protótipo universitário. Em risco imediato, consulte a Defesa Civil pelo 199.
