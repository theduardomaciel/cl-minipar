# Instruções para Teste 1 - Calculadora Cliente-Servidor

## Como executar:

### 1. Iniciar o SERVIDOR em um terminal:

```bash
java -cp out Main tests/teste1_servidor.minipar
```

Quando perguntado:
- "Este processo é (1) Servidor ou (2) Cliente?" → Digite: **1**
- "Digite a porta para escutar (ex: 8080):" → Digite: **8080**

Aguarde a mensagem: `[TCPChannel calculadora] Servidor escutando na porta 8080`

### 2. Iniciar o CLIENTE em outro terminal:

```bash
java -cp out Main tests/teste1_cliente.minipar
```

Quando perguntado:
- "Este processo é (1) Servidor ou (2) Cliente?" → Digite: **2**
- "Digite o host do servidor (ex: localhost):" → Digite: **localhost**
- "Digite a porta do servidor:" → Digite: **8080**

### 3. Testar a calculadora:

No terminal do CLIENTE, você verá:
```
=== Calculadora Aritmética ===
Operações disponíveis: +, -, *, /
Digite a operação desejada: *
Digite o primeiro valor: 2
Digite o segundo valor: 3
```

No terminal do SERVIDOR, você verá:
```
Recebido: *
```

De volta ao CLIENTE, você verá:
```
Resultado: 
6
```

## Observações:

- O servidor deve ser iniciado ANTES do cliente
- Use `localhost` para testar na mesma máquina
- Para testar em máquinas diferentes, use o IP da máquina servidora no cliente
- A porta pode ser qualquer porta livre (recomendado: acima de 1024)
