# SmartGarden Back

Backend inicial do projeto SmartGarden usando `Java 21`, `Spring Boot`, `PostgreSQL` e API REST para receber leituras do ESP32.

## Arquitetura desta fase

`ESP32 -> HTTP/REST -> Spring Boot -> PostgreSQL -> Angular`

Nesta primeira etapa, o ESP32 envia leituras de temperatura e umidade do ar para o backend por HTTP. O backend persiste os dados no PostgreSQL e expõe endpoints para consulta no frontend.

## Stack

- Java 21
- Spring Boot 3.5.14
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- SpringDoc OpenAPI / Swagger UI

## Como subir o banco local

Na pasta `smartgarden-back`, execute:

```bash
docker compose up -d
```

Isso cria um PostgreSQL local com:

- banco: `smartgarden`
- usuário: `smartgarden`
- senha: `smartgarden`

## Migrações de banco

O schema agora é versionado com `Flyway`.

- scripts: `src/main/resources/db/migration`
- estratégia JPA: `ddl-auto=validate`

Em banco vazio, o Flyway cria toda a estrutura. Em banco já existente, a opção `baseline-on-migrate` evita conflito com schema legado e permite passar a controlar as próximas mudanças por migração.

## Como rodar a aplicação

```bash
mvn spring-boot:run
```

Se quiser sobrescrever a conexão:

```bash
export SMARTGARDEN_DB_URL=jdbc:postgresql://localhost:5432/smartgarden
export SMARTGARDEN_DB_USERNAME=smartgarden
export SMARTGARDEN_DB_PASSWORD=smartgarden
mvn spring-boot:run
```

## Swagger / OpenAPI

Com a aplicação no ar, a documentação interativa fica em:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`

Os endpoints OpenAPI ficam em:

- `http://localhost:8080/v3/api-docs`
- `http://localhost:8080/v3/api-docs.yaml`

## Endpoints principais

### Criar dispositivo

`POST /api/devices`

Exemplo de body:

```json
{
  "deviceCode": "esp32-jardim-bloco-a",
  "name": "ESP32 Jardim Bloco A",
  "location": "Universidade - Bloco A"
}
```

### Listar dispositivos

`GET /api/devices`

### Registrar leitura

`POST /api/readings`

Exemplo de body:

```json
{
  "deviceCode": "esp32-jardim-bloco-a",
  "temperatureC": 27.4,
  "humidityPercent": 63.1,
  "recordedAt": "2026-05-21T14:30:00Z"
}
```

Se o dispositivo ainda não existir, ele é criado automaticamente com nome padrão.

### Listar leituras

`GET /api/readings`

Filtros disponíveis:

- `deviceCode`
- `page`
- `size`
- `startAt`
- `endAt`

Exemplo:

`GET /api/readings?deviceCode=esp32-jardim-bloco-a&page=0&size=20`

Exemplo de resposta:

```json
{
  "content": [
    {
      "id": 2,
      "deviceId": 1,
      "deviceCode": "esp32-jardim-bloco-a",
      "deviceName": "ESP32 Jardim Bloco A",
      "temperatureC": 31.9,
      "humidityPercent": 64.6,
      "recordedAt": "2026-05-21T12:16:53.231716916Z",
      "receivedAt": "2026-05-21T12:16:53.235524803Z",
      "createdAt": "2026-05-21T12:16:53.235524803Z",
      "updatedAt": "2026-05-21T12:16:53.235524803Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Última leitura de um dispositivo

`GET /api/readings/latest?deviceCode=esp32-jardim-bloco-a`

### Resumo para dashboard

`GET /api/dashboard/summary`

Parâmetros:

- `hours`: janela de agregação em horas, entre `1` e `168`

Exemplo:

`GET /api/dashboard/summary?hours=24`

Esse endpoint retorna:

- total de dispositivos
- total de dispositivos ativos
- total de leituras
- leituras dentro da janela
- média de temperatura na janela
- média de umidade na janela
- última leitura conhecida por dispositivo

## Exemplo de envio pelo ESP32

Quando você migrar do monitor serial para Wi-Fi + HTTP, o ESP32 pode enviar um JSON neste formato:

```json
{
  "deviceCode": "esp32-jardim-bloco-a",
  "temperatureC": 26.8,
  "humidityPercent": 61.5
}
```

## Próximo passo natural

Depois que esse fluxo estiver estável, a evolução para MQTT fica mais limpa:

`ESP32 -> MQTT Broker -> Spring Boot -> PostgreSQL -> Angular`
