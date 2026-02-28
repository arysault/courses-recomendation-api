# Exemplos JSON — Todas as rotas

---

## AUTH

### `POST /auth/register`
**Request:**
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123"
}
```
**Response 201:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvQGVtYWlsLmNvbSJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER"
}
```
**Response 400:**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already registered: joao@email.com"
}
```

---

### `POST /auth/login`
**Request:**
```json
{
  "email": "joao@email.com",
  "password": "senha123"
}
```
**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvQGVtYWlsLmNvbSJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER"
}
```
**Response 401:**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

---

## USERS

### `GET /users/me`
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
**Response 200:**
```json
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER",
  "isActive": true,
  "createdAt": "2024-01-01T10:00:00",
  "lastAccess": "2024-01-01T12:00:00",
  "hasPreferences": true
}
```
**Response 401:**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

---

## PREFERENCES

### `GET /users/preferences/options`
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
**Response 200:**
```json
{
  "technologies": [
    "Angular",
    "GCP",
    "Java",
    "PostgreSQL",
    "Spring Boot"
  ],
  "platforms": [
    "Alura",
    "Coursera",
    "LinkedIn Learning",
    "Udemy",
    "YouTube"
  ],
  "languages": [
    "Inglês",
    "Português"
  ],
  "levels": [
    "iniciante",
    "intermediario",
    "avancado"
  ]
}
```

---

### `GET /users/preferences`
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
**Response 200:**
```json
{
  "id": 1,
  "userId": 1,
  "languages": "Português",
  "technologies": "Java",
  "platforms": "Udemy",
  "level": "intermediario",
  "minimumRating": 4.5,
  "courses": null
}
```
**Response 404:**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Preferences not found for user: joao@email.com"
}
```

---

### `POST /users/preferences`
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
**Request:**
```json
{
  "languages": "Português",
  "technologies": "Java",
  "platforms": "Udemy",
  "level": "intermediario",
  "minimumRating": 4.5
}
```
**Response 201:**
```json
{
  "id": 1,
  "userId": 1,
  "languages": "Português",
  "technologies": "Java",
  "platforms": "Udemy",
  "level": "intermediario",
  "minimumRating": 4.5,
  "courses": [
    {
      "id": 1,
      "title": "Java COMPLETO Programação Orientada a Objetos + Projetos",
      "technology": "Java",
      "platform": "Udemy",
      "instructor": "Nélio Alves",
      "rating": 4.9,
      "description": "Curso best-seller absoluto no Brasil. Foca intensamente em engenharia de software, memória e POO antes de frameworks.",
      "language": "Português",
      "link": "https://www.udemy.com/course/java-curso-completo/",
      "score": 0.95,
      "reason": "Semantic similarity: 95%"
    },
    {
      "id": 62,
      "title": "Testes Unitários em Java: Domine JUnit, Mockito e TDD",
      "technology": "Java",
      "platform": "Udemy",
      "instructor": "Alexandre Ximenes",
      "rating": 4.6,
      "description": "Focado em qualidade de software e testes automatizados no ecossistema Java.",
      "language": "Português",
      "link": "https://www.udemy.com/course/testes-unitarios-em-java/",
      "score": 0.88,
      "reason": "Semantic similarity: 88%"
    },
    {
      "id": 72,
      "title": "Estrutura de Dados e Algoritmos em Java",
      "technology": "Java",
      "platform": "Udemy",
      "instructor": "Loiane Groner",
      "rating": 4.8,
      "description": "Curso fundamental para ciência da computação e entrevistas técnicas.",
      "language": "Português",
      "link": "https://loiane.training/curso/estrutura-de-dados-e-algoritmos-java",
      "score": 0.85,
      "reason": "Combination of ontology and semantic similarity"
    }
  ]
}
```
**Response 401:**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

---

### `PUT /users/preferences`
**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
**Request:** *(só os campos que quer atualizar)*
```json
{
  "technologies": "Spring Boot",
  "level": "avancado"
}
```
**Response 200:**
```json
{
  "id": 1,
  "userId": 1,
  "languages": "Português",
  "technologies": "Spring Boot",
  "platforms": "Udemy",
  "level": "avancado",
  "minimumRating": 4.5,
  "courses": [
    {
      "id": 24,
      "title": "Spring Boot Completo: Do Zero à AWS",
      "technology": "Spring Boot",
      "platform": "Udemy",
      "instructor": "Michelli Brito",
      "rating": 4.8,
      "description": "Abordagem prática levando uma API do zero até o deploy na nuvem AWS.",
      "language": "Português",
      "link": "https://www.udemy.com/course/spring-boot-expert/",
      "score": 0.93,
      "reason": "Semantic similarity: 93%"
    },
    {
      "id": 26,
      "title": "Spring Security 6: Zero to Master",
      "technology": "Spring Boot",
      "platform": "Udemy",
      "instructor": "Eazy Bytes",
      "rating": 4.6,
      "description": "Focado exclusivamente em segurança. Essencial para proteger APIs contra ataques comuns.",
      "language": "Inglês",
      "link": "https://www.udemy.com/course/spring-security-zero-to-master/",
      "score": 0.87,
      "reason": "Combination of ontology and semantic similarity"
    },
    {
      "id": 23,
      "title": "Microservices with Spring Boot and Spring Cloud",
      "technology": "Spring Boot",
      "platform": "Udemy",
      "instructor": "Ranga Karanam",
      "rating": 4.6,
      "description": "Focado em arquitetura distribuída. Crucial para migrar de Monolito para Microsserviços.",
      "language": "Inglês",
      "link": "https://www.udemy.com/course/microservices-with-spring-boot-and-spring-cloud/",
      "score": 0.84,
      "reason": "Combination of ontology and semantic similarity"
    }
  ]
}
```
**Response 404:**
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Preferences not found for user: joao@email.com"
}
```

---

## ERROS GENÉRICOS

### `401 — Token inválido ou expirado`
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

### `403 — Sem permissão`
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### `500 — Erro interno`
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Internal Server Error"
}
```

---

## Resumo das rotas

```
POST /auth/register                → cria usuário → retorna JWT
POST /auth/login                   → autentica → retorna JWT

GET  /users/me                     → dados do usuário logado

GET  /users/preferences/options    → opções do wizard (sem Python)
GET  /users/preferences            → preferências salvas (sem cursos)
POST /users/preferences            → salva + chama Python → preferências + cursos
PUT  /users/preferences            → atualiza + chama Python → preferências + cursos
```