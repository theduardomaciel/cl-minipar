# Comparação: INPUT vs READLN/READNUMBER

## Opção 1: Usar INPUT Existente (Modificado)

### Implementação Alternativa

```java
private Object evalInput(InputExpr in) {
    if (in.prompt != null) System.out.print(stringify(eval(in.prompt)));
    
    try {
        String line;
        
        // Se há callback (interface web), usa ele
        if (inputCallback != null) {
            line = inputCallback.readLine();
        } else {
            // Console
            line = reader.readLine();
        }
        
        if (line == null) return null;
        
        // Tenta número
        try {
            if (line.contains(".")) return Double.parseDouble(line.trim());
            return Integer.parseInt(line.trim());
        } catch (NumberFormatException e) {
            return line; // Retorna string se não for número
        }
    } catch (Exception e) {
        throw new RuntimeException("Erro de leitura de input: " + e.getMessage());
    }
}
```

### Uso no Código MiniPar

```minipar
# Comportamento automático
var nome = input();      # Retorna string se não for número
var idade = input();     # Retorna número se for numérico

# Problema: Ambiguidade!
var codigo = input();    # "123" vira número ou string?
```

---

## Opção 2: READLN e READNUMBER (Implementado)

### Vantagens

```minipar
# Tipo explícito e garantido
string nome = readln();        # SEMPRE string
number idade = readNumber();   # SEMPRE número, valida

string codigo = readln();      # "123" permanece string
number valor = readNumber();   # "abc" gera erro claro
```

### Por Que É Melhor?

1. **Segurança de Tipo**
   ```minipar
   string cpf = readln();  # "12345678900" não vira número
   number nota = readNumber();  # "dez" gera erro validado
   ```

2. **Código Mais Claro**
   ```minipar
   # Fácil de entender a intenção
   string resposta = readln();
   number quantidade = readNumber();
   ```

3. **Erros Melhores**
   ```
   readNumber() com "abc"
   ❌ "Entrada inválida: esperado um número"
   
   input() com "abc"  
   ✅ Retorna "abc" sem erro (mas programa pode quebrar depois)
   ```

4. **Padrão de Outras Linguagens**
   - Python: `input()` retorna string, precisa `int(input())`
   - Java: `scanner.nextLine()` vs `scanner.nextInt()`
   - C: `scanf("%s")` vs `scanf("%d")`

---

## Opção 3: Híbrido (Melhor dos Dois Mundos)

### Implementação Ideal

```java
// input() - comportamento original (automático)
private Object evalInput(InputExpr in) {
    String line = readLineFromSource();
    
    // Tenta converter automaticamente
    try {
        if (line.contains(".")) return Double.parseDouble(line);
        return Integer.parseInt(line);
    } catch (NumberFormatException e) {
        return line;
    }
}

// readln() - sempre string
private Object evalReadln() {
    return readLineFromSource();
}

// readNumber() - sempre número com validação
private Object evalReadNumber() {
    String line = readLineFromSource();
    try {
        if (line.contains(".")) return Double.parseDouble(line);
        return (double) Integer.parseInt(line);
    } catch (NumberFormatException e) {
        throw new RuntimeException("Entrada inválida: esperado um número");
    }
}

// Método auxiliar compartilhado
private String readLineFromSource() throws Exception {
    if (inputCallback != null) {
        return inputCallback.readLine();
    }
    return reader.readLine();
}
```

### Uso Flexível

```minipar
# Três opções disponíveis:

# 1. Automático (input)
var dado = input();  # String ou número automático

# 2. Explícito string (readln)
string texto = readln();  # SEMPRE string

# 3. Explícito número (readNumber)
number valor = readNumber();  # SEMPRE número, valida
```

---

## 📊 Comparação Técnica

| Aspecto | INPUT | READLN | READNUMBER |
|---------|-------|--------|------------|
| **Tipo de retorno** | Ambíguo | String | Number |
| **Validação** | Não | Não | Sim |
| **Conversão automática** | Sim | Não | Sim |
| **Erro em input inválido** | Não | Não | Sim |
| **Clareza de código** | ⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Segurança de tipo** | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Flexibilidade** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ |

---

## 🎯 Conclusão

### Sua Pergunta: Por que não usar INPUT?

**Resposta Curta:** Poderia usar, mas não seria ideal.

**Resposta Técnica:**

1. **Separação de Responsabilidades**
   - `input()` = conversão automática (conveniente mas perigoso)
   - `readln()` = apenas string (seguro e previsível)
   - `readNumber()` = apenas número (seguro e validado)

2. **Design Patterns**
   - Princípio da Responsabilidade Única
   - Fail-fast (erro cedo é melhor que erro tarde)
   - Explícito é melhor que implícito (Python Zen)

3. **Experiência do Desenvolvedor**
   ```minipar
   # Com input() - ambíguo
   var x = input();  # x é string ou número? 🤔
   
   # Com readln/readNumber - claro
   string x = readln();  # x é string ✅
   number y = readNumber();  # y é número ✅
   ```

### Recomendação Final

**Manter as três opções:**
- `input()` - Para compatibilidade e conveniência
- `readln()` - Para strings explícitas
- `readNumber()` - Para números validados

Isso dá ao programador **flexibilidade** sem sacrificar **segurança**.  
É o mesmo motivo pelo qual o Java tem `nextLine()` E `nextInt()` no Scanner, mesmo que `nextLine()` pudesse fazer tudo! 🎯
