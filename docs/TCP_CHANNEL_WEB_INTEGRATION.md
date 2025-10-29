# Integração de Canais TCP com Interface Web

## Resumo das Mudanças

Este documento descreve as modificações implementadas para tornar a declaração de canais TCP interativa via interface web do MiniPar.

## Arquivos Modificados

### 1. `src/parser/InputCallback.java`
**Mudanças:**
- Adicionado método `requestTCPConfig(String canalName, String comp1, String comp2)`
- Criada classe interna `TCPChannelConfig` para encapsular configurações (isServer, host, port)

**Propósito:** Permitir que o interpretador solicite configurações de canal TCP do usuário de forma assíncrona.

### 2. `src/parser/Interpreter.java`
**Mudanças:**
- Modificado método `execCanalDecl()` para verificar se há callback disponível
- Se callback existe (interface web), usa `requestTCPConfig()` para obter configurações
- Mantém comportamento original via console quando callback não está disponível

**Propósito:** Tornar a declaração de canais compatível com ambos os modos de execução (console e web).

### 3. `src/ExecutionSession.java`
**Mudanças:**
- Adicionada fila `tcpConfigQueue` para armazenar configurações TCP
- Novos estados: `waitingForTCPConfig` e `currentTCPConfigInfo`
- Implementado método `requestTCPConfig()` no callback
- Adicionado método público `provideTCPConfig(boolean isServer, String host, int port)`

**Propósito:** Gerenciar o estado de espera por configuração TCP e permitir que a interface web forneça as configurações.

### 4. `src/WebServer.java`
**Mudanças:**
- Adicionado novo endpoint `/session/tcpconfig` (handler `ProvideTCPConfigHandler`)
- Modificado `SessionStatusHandler` para incluir campos:
  - `waitingForTCPConfig`: indica se está aguardando configuração
  - `tcpConfigInfo`: informações do canal sendo configurado (nome, componentes)

**Propósito:** Fornecer API REST para envio e verificação de configurações TCP.

### 5. `web/app.js`
**Mudanças:**
- Adicionada variável de estado `waitingForTCPConfig`
- Implementada função `parseTCPConfigInfo()` para extrair informações do canal
- Criada função `showTCPConfigDialog()` que:
  - Mostra dialog modal com opções de configuração
  - Permite escolher entre Servidor/Cliente
  - Solicita porta (servidor) ou host+porta (cliente)
  - Envia configuração via POST para `/session/tcpconfig`
- Modificado polling para detectar `waitingForTCPConfig` e mostrar dialog

**Propósito:** Fornecer interface interativa para configuração de canais TCP.

### 6. `web/style.css`
**Mudanças:**
- Adicionados estilos para `.tcp-config-dialog` e componentes relacionados:
  - Dialog modal com overlay escuro
  - Formulário estilizado para entrada de configurações
  - Botões de confirmação/cancelamento
  - Classes de feedback visual (`output-info`, `output-success`)
  - Animação de fade-in

**Propósito:** Garantir uma experiência visual consistente e profissional.

## Fluxo de Funcionamento

### 1. Execução do Código
```
Usuário executa código MiniPar → Sessão criada → Polling iniciado
```

### 2. Declaração de Canal TCP Detectada
```
Interpretador encontra c_channel → Verifica callback → Chama requestTCPConfig()
```

### 3. Interface Web
```
Polling detecta waitingForTCPConfig=true
↓
Extrai informações (canal, comp1, comp2)
↓
Mostra dialog modal com formulário
↓
Usuário preenche configuração
↓
POST /session/tcpconfig com {isServer, host, port}
↓
ExecutionSession recebe config via tcpConfigQueue
↓
Canal TCP é criado e configurado
↓
Execução continua
```

## Exemplo de Uso

### Código MiniPar
```minipar
c_channel comunicacao processo1 processo2

send(comunicacao, "Olá, mundo!")
```

### Interface Web
1. Ao executar o código, aparece um dialog:
   ```
   🔌 Configuração do Canal TCP 'comunicacao'
   Componentes: processo1 ⟷ processo2
   ```

2. Usuário escolhe:
   - **Servidor**: Porta 8081
   - **Cliente**: Host "localhost", Porta 8081

3. Confirmação enviada ao servidor

4. Canal TCP configurado e execução continua

## Endpoints da API

### GET `/session/status?sessionId={id}`
**Resposta:**
```json
{
  "running": true,
  "waitingForInput": false,
  "waitingForTCPConfig": true,
  "tcpConfigInfo": "canal=comunicacao,comp1=processo1,comp2=processo2",
  "output": "...",
  "error": ""
}
```

### POST `/session/tcpconfig`
**Body:**
```json
{
  "sessionId": "uuid",
  "isServer": true,
  "host": "localhost",
  "port": 8081
}
```

**Resposta:**
```json
{
  "success": true
}
```

## Benefícios

1. **Experiência Consistente**: Mesmo fluxo interativo para input e configuração TCP
2. **Compatibilidade**: Mantém funcionamento via console inalterado
3. **Facilidade de Uso**: Interface visual clara com validação de entrada
4. **Flexibilidade**: Suporta configuração tanto de servidor quanto cliente
5. **Feedback Visual**: Mensagens claras sobre o estado da configuração

## Testes Sugeridos

1. **Teste Básico**: Declarar canal TCP e verificar dialog
2. **Teste Servidor**: Configurar como servidor na porta 8081
3. **Teste Cliente**: Configurar como cliente conectando a localhost:8081
4. **Teste Cancelamento**: Cancelar configuração e verificar interrupção
5. **Teste Múltiplos Canais**: Declarar vários canais em sequência
6. **Teste Console**: Verificar que modo console ainda funciona
