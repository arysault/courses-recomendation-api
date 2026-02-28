# `DOCS.md` — Documentação Completa do Projeto

```markdown
# 🎓 Courses Recommendation API — Documentação Completa

## 📋 Índice
1. Visão Geral
2. Pré-requisitos
3. Banco de Dados
4. Backend Java — Como Rodar
5. Swagger — Como Usar
6. Python Service — O que falta
7. Frontend Angular — O que falta
8. Rotas Completas
9. Segurança JWT
10. Erros Comuns

---

## 1. Visão Geral

Sistema de recomendação de cursos com IA composto por 3 serviços:

┌──────────────────────────────────────────────────────┐
│                    FLUXO COMPLETO                    │
│                                                      │
│  Angular :4200 ──► Spring Boot :8080 ──► Python :8000│
│                          │                    │      │
│                          └────────────────────┘      │
│                               PostgreSQL :5432       │
└──────────────────────────────────────────────────────┘

Responsabilidades:
→ Angular      : Interface do usuário
→ Spring Boot  : Auth, regras de negócio, API REST
→ Python       : IA, embeddings, recomendações
→ PostgreSQL   : Banco de dados compartilhado

---

## 2. Pré-requisitos

### Instalar antes de tudo:

| Ferramenta     | Versão mínima | Download                          |
|----------------|---------------|-----------------------------------|
| Java JDK       | 21+           | https://adoptium.net              |
| Gradle         | 8+            | via wrapper (./gradlew)           |
| Python         | 3.10+         | https://python.org                |
| Node.js        | 18+           | https://nodejs.org                |
| PostgreSQL     | 15+           | https://postgresql.org            |
| Git            | qualquer      | https://git-scm.com               |

### Verificar instalações:
```bash
java --version       # Java 21+
python --version     # Python 3.10+
node --version       # Node 18+
psql --version       # PostgreSQL 15+
git --version        # qualquer
```

---

## 3. Banco de Dados

### 3.1 Criar o banco

```bash
# Acessar o PostgreSQL
psql -U postgres

# Criar banco
CREATE DATABASE recomencurso_db;

# Verificar se criou
\l

# Sair
\q
```

### 3.2 Tabelas (criadas pelo Python via Alembic)

O banco é gerenciado pelo Python.
As tabelas são criadas automaticamente ao rodar as migrations.

```
Tabelas criadas:
├── users                  → usuários do sistema
├── user_preferences       → preferências de cada usuário
├── courses                → cursos disponíveis
└── recommendation_history → histórico de recomendações
```

### 3.3 Estrutura das tabelas

#### users
```sql
id            INTEGER PRIMARY KEY
name          VARCHAR(200) NOT NULL
email         VARCHAR(200) UNIQUE NOT NULL
password_hash VARCHAR(255) NOT NULL
is_active     BOOLEAN DEFAULT TRUE
created_at    TIMESTAMP
updated_at    TIMESTAMP
```

#### user_preferences
```sql
id                   INTEGER PRIMARY KEY
user_id              INTEGER FK → users (único)
languages            TEXT        -- ex: "Português"
technologies         TEXT        -- ex: "Java, Spring Boot"
platforms            TEXT        -- ex: "Udemy, Alura"
level                VARCHAR(50) -- iniciante/intermediario/avancado
minimum_rating       FLOAT DEFAULT 4.0
interest_concepts    TEXT        -- gerado pelo Python
preference_embedding TEXT        -- gerado pelo Python
created_at           TIMESTAMP
updated_at           TIMESTAMP
```

#### courses
```sql
id                  INTEGER PRIMARY KEY
technology          VARCHAR(100)
title               VARCHAR(500)
platform            VARCHAR(100)
instructor          VARCHAR(200)
rating              FLOAT
colbert_description TEXT
ontology_syllabus   TEXT
language            VARCHAR(50)
link                VARCHAR(500)
embedding_vector    TEXT  -- gerado pelo Python
created_at          TIMESTAMP
updated_at          TIMESTAMP
```

#### recommendation_history
```sql
id                  INTEGER PRIMARY KEY
user_id             INTEGER FK → users
original_query      TEXT
recommended_courses TEXT    -- JSON dos cursos
algorithm_used      VARCHAR(50)
feedback_score      FLOAT   -- 1.0 a 5.0
viewed_course_id    INTEGER FK → courses
created_at          TIMESTAMP
```

---

## 4. Backend Java — Como Rodar

### 4.1 Configurar o application.properties

Arquivo em:
```
src/main/resources/application.properties
```

Conteúdo necessário:
```properties
# Aplicação
spring.application.name=courses-recommendation-api
server.port=8080

# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/recomencurso_db
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_AQUI
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# JWT usuário (Angular → Spring)
jwt.secret=SUA_SECRET_AQUI_MINIMO_32_CARACTERES
jwt.expiration=86400000

# JWT interno (Spring → Python)
internal.jwt.secret=SUA_SECRET_INTERNA_AQUI
internal.jwt.expiration=60000

# Python Service
python.service.url=http://localhost:8000

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.operationsSorter=method

# Actuator
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always

# Logs
logging.level.org.challengegroup=DEBUG
logging.level.org.springframework.security=WARN
```

### 4.2 Gerar as secrets JWT

```bash
# Secret do usuário (jwt.secret) → mínimo 32 caracteres
openssl rand -base64 64

# Secret interna (internal.jwt.secret)
openssl rand -base64 64

# Cole cada uma no application.properties
```

### 4.3 Rodar o projeto

```bash
# Entrar na pasta do projeto Java
cd courses-recommendation-api

# Dar permissão ao gradlew (Linux/Mac)
chmod +x gradlew

# Rodar o projeto
./gradlew bootRun

# OU compilar e rodar o jar
./gradlew build
java -jar build/libs/courses-recommendation-api-0.0.1-SNAPSHOT.jar
```

### 4.4 Verificar se subiu corretamente

```bash
# Health check
curl http://localhost:8080/actuator/health

# Resposta esperada:
{
  "status": "UP"
}
```

### 4.5 O que aparece no console ao subir

```
  .   ____          _
 /\\ / ___'_ __ _ _(_)_ __  
( ( )\___ | '_ | '_| | '_ \
 \\/  ___)| |_)| | | | | || |
  '  |____| .__|_| |_|_| |_|
 :: Spring Boot ::               (v4.0.x)

Started CoursesRecomendationApplication in X seconds
Tomcat started on port 8080
```

### 4.6 Estrutura do Projeto Java

```
src/main/java/org/challengegroup/coursesrecomendation/
├── config/
│   ├── CorsConfig.java            → CORS configurado
│   ├── SecurityConfig.java        → Spring Security + JWT
│   ├── SwaggerConfig.java         → Swagger UI
│   └── RestTemplateConfig.java    → HTTP client pro Python
├── controller/
│   ├── AuthController.java        → /auth/**
│   ├── UserController.java        → /users/**
│   └── RecommendationController.java → /recommendations/**
├── dto/
│   ├── AuthResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── UserMeResponse.java
│   ├── UserPreferenceRequest.java
│   ├── UserPreferenceResponse.java
│   ├── CourseResponse.java
│   ├── RecommendationResponse.java
│   └── SearchRequest.java
├── entity/
│   ├── Role.java (enum: USER, ADMIN)
│   ├── User.java
│   └── UserPreference.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── UserRepository.java
│   └── UserPreferenceRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthFilter.java
│   ├── JwtTokenProvider.java
│   └── InternalJwtProvider.java
└── service/
    ├── AuthService.java
    ├── UserService.java
    ├── RecommendationService.java
    └── PythonService.java
```

---

## 5. Swagger — Como Usar

### 5.1 Acessar

```
http://localhost:8080/swagger-ui.html
```

### 5.2 Como testar rotas autenticadas

```
Passo 1 → Abrir http://localhost:8080/swagger-ui.html

Passo 2 → Expandir POST /auth/login

Passo 3 → Clicar em "Try it out"

Passo 4 → Preencher:
{
  "email": "seu@email.com",
  "password": "suasenha"
}

Passo 5 → Clicar em "Execute"

Passo 6 → Copiar o campo "token" da resposta

Passo 7 → Clicar no botão "Authorize" (cadeado no topo)

Passo 8 → Colar o token e confirmar

Passo 9 → Agora todas as rotas autenticadas funcionam!
```

### 5.3 Rotas disponíveis no Swagger

```
Auth
├── POST /auth/register   → criar conta
└── POST /auth/login      → fazer login

Users
├── GET  /users/me                   → dados do usuário
├── GET  /users/preferences/options  → opções do wizard
├── GET  /users/preferences          → preferências + cursos
├── POST /users/preferences          → salvar preferências
└── PUT  /users/preferences          → atualizar preferências

Recommendations
├── GET  /recommendations        → cursos pelo perfil
└── POST /recommendations/search → busca por texto livre
```

---

## 6. Python Service — O que falta implementar

### 6.1 O que já existe no Python ✅

```
POST /recommend/user   → recomendação por perfil (usado pelo Spring)
POST /recommend/search → busca semântica (usado pelo Spring)
GET  /health           → health check
Ontology Service       → recomendação por conceitos
ColBERT Service        → recomendação por embeddings
Scripts de embedding   → gerar embeddings dos cursos e usuários
```

### 6.2 O que o Python precisa implementar ❌

#### Passo 1 — Instalar dependência JWT
```bash
pip install python-jose
```

#### Passo 2 — Criar validação do JWT interno

Criar arquivo `api/security/internal_jwt.py`:
```python
import os
from jose import jwt
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer

security = HTTPBearer()

INTERNAL_SECRET = os.getenv("INTERNAL_JWT_SECRET")
INTERNAL_ALGORITHM = "HS256"

def verify_internal_token(credentials = Depends(security)):
    token = credentials.credentials
    try:
        payload = jwt.decode(
            token,
            INTERNAL_SECRET,
            algorithms=[INTERNAL_ALGORITHM]
        )
        if payload.get("service") != "courses-recommendation":
            raise HTTPException(status_code=401, detail="Invalid service")
        return payload
    except Exception:
        raise HTTPException(
            status_code=401,
            detail="Invalid or missing internal token"
        )
```

#### Passo 3 — Adicionar validação nos endpoints

No arquivo `api/routes/recomendacao.py`:
```python
from api.security.internal_jwt import verify_internal_token

# Adicionar o Depends em cada endpoint:

@router.post("/user", response_model=RecommendationResponse)
def recommend_by_user(
    request: RecommendationRequest,
    db: Session = Depends(get_db),
    _: None = Depends(verify_internal_token)  # ← ADICIONAR
):
    ...

@router.post("/search", response_model=RecommendationResponse)
def semantic_search(
    request: SemanticSearchRequest,
    db: Session = Depends(get_db),
    _: None = Depends(verify_internal_token)  # ← ADICIONAR
):
    ...
```

#### Passo 4 — Configurar variáveis de ambiente Python

Criar arquivo `.env` na raiz do projeto Python:
```env
DATABASE_URL=postgresql://postgres:SUASENHA@localhost:5432/recomencurso_db
INTERNAL_JWT_SECRET=MESMO_VALOR_DO_JAVA_internal.jwt.secret
DEBUG=False
```

#### Passo 5 — Rodar o Python

```bash
# Entrar na pasta do projeto Python
cd python-service

# Criar ambiente virtual
python -m venv venv

# Ativar ambiente virtual
# Linux/Mac:
source venv/bin/activate
# Windows:
venv\Scripts\activate

# Instalar dependências
pip install -r requirements.txt

# Rodar migrations do banco
alembic upgrade head

# Popular banco com os cursos do CSV
python scripts/populate_db.py

# Gerar embeddings dos cursos
python scripts/generate_embeddings.py

# Rodar o servidor
uvicorn main:app --reload --port 8000
```

#### Passo 6 — Verificar se subiu

```bash
curl http://localhost:8000/health

# Resposta esperada:
{
  "status": "healthy",
  "database": "connected",
  "cursos": 150,
  "usuarios": 0
}
```

### 6.3 O que o Spring envia para o Python

#### POST /recommend/user
```json
Headers:
  Content-Type: application/json
  Authorization: Bearer {internalJWT}

Body:
{
  "user_id": 1,
  "limit": 10,
  "technologies": ["Java", "Spring Boot"],
  "languages": ["Português"],
  "minimum_rating": 4.5
}
```

#### POST /recommend/search
```json
Headers:
  Content-Type: application/json
  Authorization: Bearer {internalJWT}

Body:
{
  "query": "quero aprender microservices com Java",
  "limit": 10,
  "technologies": ["Java"],
  "languages": ["Português"]
}
```

### 6.4 O que o Python precisa retornar

```json
{
  "user_id": 1,
  "total": 10,
  "algorithm": "hybrid_ontology_colbert",
  "courses": [
    {
      "id": 1,
      "title": "Java COMPLETO Programação Orientada a Objetos",
      "technology": "Java",
      "platform": "Udemy",
      "instructor": "Nélio Alves",
      "rating": 4.9,
      "description": "Curso best-seller...",
      "syllabus": "Lógica, POO, Memória...",
      "language": "Português",
      "link": "https://www.udemy.com/...",
      "score": 0.95,
      "reason": "Semantic similarity: 95%"
    }
  ]
}
```

---

## 7. Frontend Angular — O que falta implementar

### 7.1 Criar o projeto

```bash
npm install -g @angular/cli
ng new courses-recommendation-frontend
cd courses-recommendation-frontend
```

### 7.2 Estrutura sugerida

```
src/app/
├── core/
│   ├── guards/
│   │   └── auth.guard.ts
│   ├── interceptors/
│   │   └── jwt.interceptor.ts
│   └── services/
│       ├── auth.service.ts
│       ├── user.service.ts
│       └── recommendation.service.ts
├── models/
│   ├── auth.model.ts
│   ├── user.model.ts
│   ├── course.model.ts
│   └── preference.model.ts
└── pages/
    ├── login/
    ├── register/
    ├── wizard/
    ├── recommendations/
    └── profile/
```

### 7.3 Interceptor JWT (obrigatório)

```typescript
// core/interceptors/jwt.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
```

### 7.4 Auth Guard (obrigatório)

```typescript
// core/guards/auth.guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
```

### 7.5 Models TypeScript

```typescript
// models/auth.model.ts
export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  name: string;
  email: string;
  role: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

// models/course.model.ts
export interface Course {
  id: number;
  title: string;
  technology: string;
  platform: string;
  instructor: string;
  rating: number;
  language: string;
  link: string;
  score: number;
  reason: string;
}

// models/preference.model.ts
export interface UserPreference {
  id: number;
  userId: number;
  languages: string;
  technologies: string;
  platforms: string;
  level: string;
  minimumRating: number;
  courses: Course[];
}

export interface PreferenceOptions {
  technologies: string[];
  platforms: string[];
  languages: string[];
  levels: string[];
}

export interface PreferenceRequest {
  languages?: string;
  technologies?: string;
  platforms?: string;
  level?: string;
  minimumRating?: number;
}

// models/search.model.ts
export interface SearchRequest {
  query: string;
  technologies?: string[];
  languages?: string[];
  limit?: number;
}
```

### 7.6 Fluxo de telas esperado

```
/login          
→ POST /auth/login
→ salva token no localStorage
→ redireciona para /wizard ou /recommendations

/register       
→ POST /auth/register
→ salva token no localStorage
→ redireciona para /wizard

/wizard         
→ GET  /users/preferences/options (carrega opções)
→ POST /users/preferences (salva + recebe cursos)
→ redireciona para /recommendations

/recommendations
→ GET  /recommendations (cursos pelo perfil)
→ POST /recommendations/search (busca por texto)

/profile        
→ GET  /users/me (dados do usuário)
→ GET  /users/preferences (preferências + cursos)
→ PUT  /users/preferences (editar preferências)
```

---

## 8. Rotas Completas

### 8.1 Rotas Públicas (sem token)

| Método | Rota            | Descrição          |
|--------|-----------------|--------------------|
| POST   | /auth/register  | Criar conta        |
| POST   | /auth/login     | Fazer login        |
| GET    | /actuator/health| Health check       |
| GET    | /swagger-ui.html| Swagger UI         |

### 8.2 Rotas Autenticadas (Bearer token obrigatório)

| Método | Rota                       | Descrição                        |
|--------|----------------------------|----------------------------------|
| GET    | /users/me                  | Dados do usuário logado          |
| GET    | /users/preferences/options | Opções do wizard                 |
| GET    | /users/preferences         | Preferências + cursos            |
| POST   | /users/preferences         | Salvar preferências + cursos     |
| PUT    | /users/preferences         | Atualizar preferências + cursos  |
| GET    | /recommendations           | Cursos pelo perfil               |
| POST   | /recommendations/search    | Busca por texto livre            |

### 8.3 Exemplos de Request/Response

#### POST /auth/register
```json
Request:
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "senha123"
}

Response 201:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER"
}
```

#### POST /auth/login
```json
Request:
{
  "email": "joao@email.com",
  "password": "senha123"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER"
}
```

#### GET /users/me
```json
Response 200:
{
  "id": 1,
  "name": "João Silva",
  "email": "joao@email.com",
  "role": "USER",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00",
  "lastAccess": "2024-01-01T12:00:00",
  "hasPreferences": true
}
```

#### GET /users/preferences/options
```json
Response 200:
{
  "technologies": ["Angular", "GCP", "Java", "PostgreSQL", "Spring Boot"],
  "platforms": ["Alura", "Coursera", "Udemy", "YouTube"],
  "languages": ["Inglês", "Português"],
  "levels": ["iniciante", "intermediario", "avancado"]
}
```

#### POST /users/preferences
```json
Request:
{
  "languages": "Português",
  "technologies": "Java, Spring Boot",
  "platforms": "Udemy, Alura",
  "level": "intermediario",
  "minimumRating": 4.5
}

Response 201:
{
  "id": 1,
  "userId": 1,
  "languages": "Português",
  "technologies": "Java, Spring Boot",
  "platforms": "Udemy, Alura",
  "level": "intermediario",
  "minimumRating": 4.5,
  "courses": [
    {
      "id": 1,
      "title": "Java COMPLETO Programação Orientada a Objetos",
      "technology": "Java",
      "platform": "Udemy",
      "instructor": "Nélio Alves",
      "rating": 4.9,
      "language": "Português",
      "link": "https://udemy.com/...",
      "score": 0.95,
      "reason": "Semantic similarity: 95%"
    }
  ]
}
```

#### GET /recommendations
```json
Response 200:
[
  {
    "id": 1,
    "title": "Java COMPLETO Programação Orientada a Objetos",
    "technology": "Java",
    "platform": "Udemy",
    "instructor": "Nélio Alves",
    "rating": 4.9,
    "language": "Português",
    "link": "https://udemy.com/...",
    "score": 0.95,
    "reason": "Semantic similarity: 95%"
  }
]
```

#### POST /recommendations/search
```json
Request:
{
  "query": "quero aprender microservices com Java",
  "technologies": ["Java", "Spring Boot"],
  "languages": ["Português"],
  "limit": 10
}

Response 200:
[
  {
    "id": 23,
    "title": "Microservices with Spring Boot and Spring Cloud",
    "technology": "Spring Boot",
    "platform": "Udemy",
    "instructor": "Ranga Karanam",
    "rating": 4.6,
    "language": "Inglês",
    "link": "https://udemy.com/...",
    "score": 0.91,
    "reason": "Semantic search: 91%"
  }
]
```

### 8.4 Padrão de Erro

```json
{
  "timestamp": "2024-01-01T00:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "descrição do erro"
}

Códigos:
400 → dados inválidos / regra de negócio
401 → token ausente, inválido ou expirado
403 → sem permissão para o recurso
404 → usuário ou preferência não encontrada
500 → erro interno do servidor
```

---

## 9. Segurança JWT

### 9.1 JWT do Usuário (Angular ↔ Spring)

```
Gerado em: POST /auth/login e POST /auth/register
Enviado em: Header Authorization: Bearer {token}
Expiração: configurado em jwt.expiration (ms)

Payload:
{
  "sub": "joao@email.com",
  "userId": 1,
  "nome": "João Silva",
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234567890
}
```

### 9.2 JWT Interno (Spring → Python)

```
Gerado em: toda requisição do Spring para o Python
Enviado em: Header Authorization: Bearer {internalToken}
Nunca exposto para o Angular
Expira em: 60 segundos (configurado em internal.jwt.expiration)

Payload:
{
  "service": "courses-recommendation",
  "iat": 1234567890,
  "exp": 1234567890
}

⚠️ IMPORTANTE:
A mesma secret deve estar em:
Java  → internal.jwt.secret no application.properties
Python → INTERNAL_JWT_SECRET no .env
```

### 9.3 Como gerar as secrets

```bash
# Gerar jwt.secret (usuário)
openssl rand -base64 64

# Gerar internal.jwt.secret (Spring→Python)
openssl rand -base64 64

# NUNCA commitar as secrets no Git!
# Adicionar no .gitignore:
echo "src/main/resources/application.properties" >> .gitignore
echo ".env" >> .gitignore
```

---

## 10. Erros Comuns

### Java não conecta no banco
```
Erro: Connection refused postgresql://localhost:5432
Solução: Verificar se o PostgreSQL está rodando
→ sudo service postgresql start (Linux)
→ Verificar usuário e senha no application.properties
```

### Swagger dá erro 500
```
Erro: NoSuchMethodError ControllerAdviceBean
Solução: Versão incompatível do springdoc
→ Usar springdoc-openapi-starter-webmvc-ui:2.8.6
→ Spring Boot 4 requer springdoc 2.8.x ou superior
```

### Token JWT inválido
```
Erro: 401 Unauthorized
Soluções:
→ Verificar se jwt.secret tem pelo menos 32 caracteres
→ Verificar se o token não expirou
→ Verificar se está enviando: Authorization: Bearer {token}
→ Não esquecer o "Bearer " antes do token
```

### Spring não conecta no Python
```
Erro: Connection refused http://localhost:8000
Soluções:
→ Verificar se o Python está rodando na porta 8000
→ Verificar python.service.url no application.properties
→ Verificar se o endpoint /recommend/user existe no Python
```

### Python rejeita o JWT interno
```
Erro: 401 Invalid internal token
Soluções:
→ Verificar se INTERNAL_JWT_SECRET é igual nos dois serviços
→ Java:   internal.jwt.secret no application.properties
→ Python: INTERNAL_JWT_SECRET no .env
```

---

## 11. Checklist para tudo funcionar

```
Banco de Dados
□ PostgreSQL instalado e rodando
□ Banco recomencurso_db criado

Python
□ Dependências instaladas (pip install -r requirements.txt)
□ python-jose instalado
□ .env configurado com DATABASE_URL e INTERNAL_JWT_SECRET
□ Migrations rodadas (alembic upgrade head)
□ Cursos populados (python scripts/populate_db.py)
□ Embeddings gerados (python scripts/generate_embeddings.py)
□ Validação JWT interno implementada nos endpoints
□ Servidor rodando na porta 8000

Java
□ application.properties configurado
□ jwt.secret com pelo menos 32 caracteres
□ internal.jwt.secret IGUAL ao Python
□ python.service.url=http://localhost:8000
□ ./gradlew bootRun rodando sem erros
□ http://localhost:8080/actuator/health retorna UP

Angular
□ jwt.interceptor.ts criado e registrado
□ auth.guard.ts criado e registrado nas rotas
□ Models TypeScript criados
□ API_URL apontando para http://localhost:8080
□ ng serve rodando na porta 4200
```

---

## 12. Status do Projeto

```
✅ PASSO 1 - Security + JWT         CONCLUÍDO
   → Spring Security configurado
   → JWT geração e validação
   → InternalJwtProvider (Spring→Python)

✅ PASSO 2 - User + Preferences     CONCLUÍDO
   → User entity + repository
   → UserPreference entity + repository
   → AuthService + AuthController
   → UserService + UserController
   → Todos os DTOs

✅ PASSO 3 - Recommendations        CONCLUÍDO
   → RecommendationController
   → RecommendationService
   → PythonService (integração HTTP)
   → SearchRequest DTO

✅ PASSO 3.5 - Swagger              CONCLUÍDO
   → SwaggerConfig
   → Bearer JWT configurado
   → Todas as rotas documentadas

⏳ PASSO 4 - Feedback               PENDENTE
   → PUT /recommendations/{id}/feedback

⏳ PASSO 5 - Frontend Angular       PENDENTE
   → Todas as telas

⏳ PASSO 6 - Docker + Cloud Run     PENDENTE
   → Dockerfiles
   → Docker Compose
   → Deploy GCP
```
```