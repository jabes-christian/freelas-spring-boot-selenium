# 🤖 RoboFreelas — Monitor Automático de Oportunidades

> **Status: Em andamento** 🚧 — Scraper do Workana funcional. Integração com Telegram e agendamento em desenvolvimento.

Sistema de monitoramento automatizado de oportunidades de freelance e licitações públicas, com notificações em tempo real via Telegram. O robô acessa periodicamente os portais, filtra projetos relevantes por palavras-chave (RPA, IA, Automação) e notifica instantaneamente quando uma oportunidade inédita é encontrada.

---

## 📋 Índice

- [Visão Geral](#-visão-geral)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Estrutura de Pacotes](#-estrutura-de-pacotes)
- [Pré-requisitos](#-pré-requisitos)
- [Como Rodar](#-como-rodar)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Configuração do Bot do Telegram](#-configuração-do-bot-do-telegram)
- [Roadmap](#-roadmap)

---

## 🎯 Visão Geral

O RoboFreelas resolve um problema simples: monitorar manualmente portais de freelance é lento e faz você perder oportunidades para quem responde primeiro. O sistema automatiza esse processo rodando em background, filtrando apenas o que é relevante e notificando no exato momento em que uma nova vaga aparece.

**Exemplo de notificação recebida no Telegram:**

```
🚀 Nova Oportunidade Encontrada!

Projeto : Desenvolvimento de Agente de IA para CRM
Plataforma: Workana
Valor    : R$ 1.500 - R$ 3.000
Link     : https://workana.com/job/...
Status   : Analisado em 08/04/2026 às 14:32
```

---

## ✅ Funcionalidades

### Implementadas
- [x] Extração de oportunidades do Workana (título, valor, link, descrição)
- [x] Filtro por palavras-chave relevantes (RPA, IA, Automação, Selenium, Bot...)
- [x] Deduplicação automática — mesma oportunidade nunca é notificada duas vezes
- [x] Persistência no PostgreSQL com histórico completo
- [x] Modo headless — roda sem abrir janela do browser
- [x] Integração com Telegram Bot para notificações em tempo real
- [x] Agendamento automático a cada 30/60 minutos com `@Scheduled`
- [x] Reprocessamento de notificações pendentes em caso de falha do Telegram

### Em desenvolvimento 🚧
- [ ] Reprocessamento de notificações pendentes em caso de falha do Telegram
- [ ] Scraper do 99Freelas

### Planejadas 📌
- [ ] Scraper de Portais de Licitação públicos
- [ ] Dockerização completa da aplicação Spring Boot
- [ ] Configuração de perfis Spring (dev/prod)
- [ ] Dashboard simples para visualização do histórico

---

## 🛠 Tecnologias

### Back-end
| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.5 | Framework base |
| Spring Data JPA | 4.0.x | Persistência e repositórios |
| Spring Scheduler | 4.0.x | Agendamento de tarefas |
| Lombok | 1.18.x | Redução de boilerplate |

### RPA / Scraping
| Tecnologia | Versão | Uso |
|---|---|---|
| Selenium Java | 4.37.0 | Automação do browser |
| WebDriverManager | 5.8.0 | Gerenciamento automático do ChromeDriver |
| Google Chrome | — | Browser utilizado pelo robô |

### Banco de Dados
| Tecnologia | Versão | Uso |
|---|---|---|
| PostgreSQL | 16 (Alpine) | Banco de dados principal |
| HikariCP | 7.0.x | Pool de conexões |
| Docker / Docker Compose | — | Containerização do banco |

### Notificação
| Tecnologia | Versão | Uso |
|---|---|---|
| Telegram Bot API | 7.3.0 | Envio de notificações em tempo real |

### Build
| Tecnologia | Versão | Uso |
|---|---|---|
| Maven | 3.x | Gerenciamento de dependências e build |

---

## 🏗 Arquitetura

```
┌─────────────────────────────────────────────────────┐
│                  Spring Scheduler                    │
│              @Scheduled — 30/60 min                 │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  Scraper Layer                       │
│     WorkanaScraper · FreelasScraper (em breve)      │
│         Selenium Headless + WebDriverWait            │
└──────┬───────────────┬──────────────────────────────┘
       │               │
   Workana          99Freelas
  (funcional)      (planejado)
       │               │
┌──────▼───────────────▼──────────────────────────────┐
│               OpportunityService                     │
│    Deduplicação · Validação · Lógica de negócio     │
└──────┬───────────────┬──────────────────────────────┘
       │               │
┌──────▼──────┐  ┌─────▼──────────────────────────────┐
│ PostgreSQL  │  │      TelegramNotificationService    │
│ Repository  │  │         Bot API — Markdown          │
└─────────────┘  └────────────────────────────────────┘
```

---

## 📁 Estrutura de Pacotes

```
src/main/java/dev/jchristian/RoboFreelas/
│
├── config/
│   └── SeleniumConfig.java                # WebDriver bean (headless Chrome)
│
├── scraper/
│   ├── base/
│   │   └── BaseScraper.java               # Métodos comuns: navegação, esperas, interações
│   ├── WorkanaScraper.java                # ✅ Funcional
│   └── FreelasScraper.java                # 🚧 Planejado
│
├── service/
│   ├── OpportunityService.java            # Orquestra scrapers + deduplicação
│   └── TelegramNotificationService.java   # Serviço que faz integração com Telegarm
│
├── scheduler/
│   └── MonitorScheduler.java              # 🚧 Em desenvolvimento
│
├── repository/
│   └── OpportunityRepository.java         # JpaRepository + queries de deduplicação
│
├── entity/
│   └── Opportunity.java                   # Entidade JPA — tabela opportunities
│
└── dto/
    └── OpportunityDTO.java                # Record — objeto de transporte entre scraper e service
```

---

## 📦 Pré-requisitos

Antes de rodar o projeto, certifique-se de ter instalado:

- [Java 21+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/)
- [Docker + Docker Compose](https://www.docker.com/)
- [Google Chrome](https://www.google.com/chrome/) — necessário para o Selenium
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (recomendado) ou outra IDE Java

---

## 🚀 Como Rodar

### 1. Clone o repositório

```bash
git clone https://github.com/jabes-christian/freelas-spring-boot-selenium.git
cd RoboFreelas
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto com base no exemplo:

```bash
cp .env.example .env
```

Edite o `.env` com suas credenciais (veja a seção [Variáveis de Ambiente](#-variáveis-de-ambiente)).

### 3. Suba o banco de dados com Docker

```bash
docker-compose up -d
```

Aguarde o container ficar saudável:

```bash
docker ps
# STATUS deve mostrar: healthy
```

### 4. Rode a aplicação

```bash
mvn spring-boot:run
```

Ou pela IDE: execute a classe `RoboFreelasApplication.java`.

---

## 🔐 Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
# Banco de Dados
POSTGRES_DB=robofreelas
POSTGRES_USER=seu_usuario
POSTGRES_PASSWORD=sua_senha_segura
POSTGRES_PORT=5432

# Telegram Bot (configurar após criar o bot no @BotFather)
TELEGRAM_BOT_TOKEN=seu_token_aqui
TELEGRAM_CHAT_ID=seu_chat_id_aqui
```

> ⚠️ O arquivo `.env` está no `.gitignore` e **nunca deve ser commitado** com credenciais reais.

Crie também um `.env.example` para documentar as variáveis sem os valores:

```env
POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_PORT=5432
TELEGRAM_BOT_TOKEN=
TELEGRAM_CHAT_ID=
```

---

## 🤖 Configuração do Bot do Telegram

### 1. Criar o bot

1. Abra o Telegram e busque por `@BotFather`
2. Envie o comando `/newbot`
3. Escolha um nome e um username para o bot
4. O BotFather vai retornar o **TOKEN** — guarde-o no `.env`

### 2. Obter o Chat ID

1. Inicie uma conversa com o seu bot
2. Acesse no browser:
```
https://api.telegram.org/bot<SEU_TOKEN>/getUpdates
```
3. Localize o campo `"id"` dentro de `"chat"` — esse é o seu **CHAT_ID**
4. Guarde-o no `.env`

---

## 📌 Roadmap

```
v0.1 — Scraper Base                    ✅ Concluído
  └── WorkanaScraper funcional
  └── Deduplicação e persistência

v0.2 — Notificações                    ✅ Concluído
  └── TelegramNotificationService
  └── MonitorScheduler com @Scheduled
  └── Reprocessamento de pendências

v0.3 — Expansão de Fontes              🚧 Em andamento
  └── FreelasScraper (99Freelas)      
  └── LicitacaoScraper (portais públicos)

v0.4 — Dockerização Completa          📌 Planejado
  └── Dockerfile da aplicação Spring Boot
  └── docker-compose com app + banco
  └── Perfis Spring dev/prod

v0.5 — Melhorias                      📌 Planejado
  └── Dashboard de histórico
  └── Filtros configuráveis via properties
  └── Métricas e alertas de saúde do robô
```

---

## 👨‍💻 Autor

Desenvolvido por **Jabes Christian**
