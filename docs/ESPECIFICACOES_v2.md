# Tema 2 — Interpretador MiniPar 2025.1

> **Desenvolvimento de um Interpretador para a Linguagem MiniPar 2025.1 Orientada a Objetos**
>
> **Componentes:**  
> - Analisador Léxico  
> - Analisador Sintático Descendente  
> - Árvore Sintática  
> - Tabela de Símbolos  
> - Analisador Semântico  
>
> O projeto deve respeitar a gramática da linguagem orientada a objetos do MiniPar.

---

## 📑 Sumário

- [1. Enunciado](#1-enunciado)
- [2. A Linguagem MiniPar 2025.1](#2-a-linguagem-minipar-20251)
- [3. Programas de Teste](#3-programas-de-teste-na-linguagem-minipar-20251)
- [4. Entrega do Tema 2](#4-entrega-do-tema-2)
- [Apêndice 1](#apêndice-1)
- [Apêndice 2](#apêndice-2)

---

## 1. Enunciado

> [!TIP]
> **Desenvolvimento de um Interpretador para a Linguagem MiniPar Orientada a Objetos utilizando Componentes de Software**

Implemente o interpretador da linguagem MiniPar 2025.1 em **Java**, **Python** ou **C++**, seguindo o paradigma orientado a objetos.

A modelagem do interpretador deve utilizar técnicas de Engenharia de Software, especialmente Componentes de Software. A arquitetura deve ser composta por componentes para os analisadores (léxico, sintático e semântico) e para a tabela de símbolos.

Para a modelagem dos componentes (analisador léxico, analisador sintático, analisador semântico e tabela de símbolos), utilize **UML** para representar os sistemas orientados a objetos.

---

## 2. A Linguagem MiniPar 2025.1

### ⭐ Características

| Execução      | Comunicação      | Canal                |
|---------------|-----------------|----------------------|
| SEQ (Sequencial) | Via mensagem      | `c_channel` (socket) |
| PAR (Paralela)   | Sockets (Java/Python) | `localhost` para testes |

#### Execução

- **PAR**: Utiliza Threads para implementar paralelismo. Cada Thread é considerada um processo independente, sem compartilhamento de informações.
- **Comunicação**: Somente via mensagem.
- **Canal de Comunicação**: Implementado com sockets em Java ou Python.
- **Testes**: Podem ser realizados em `localhost`.

#### Operações de Comunicação

- **Send**: Envio de dados via canal de comunicação (socket) para outro computador.
- **Receive**: Recepção de dados de outro computador via canal de comunicação (socket).

#### Blocos de Instruções

- **SEQ**: Execução sequencial de um conjunto de instruções.
- **PAR**: Execução paralela (implementada com Threads) de um conjunto de instruções.

#### Tipos de Variáveis

- `Bool` – igual ao Python
- `Int` – igual ao Python
- `String` – igual ao Python
- `c_channel` – canal de comunicação entre dois computadores

#### Entrada e Saída

- **Input**: Entrada do teclado (igual ao Python)
- **Output**: Saída na tela (igual ao Python)

> [!NOTE]  
> **Comentários:** Use `#`  
> **Funções:** Permite declaração e uso de funções

---

### 📜 Gramática BNF (Produções)

```bnf
programa_minipar → bloco_stmt  
bloco_stmt → bloco_SEQ | bloco_PAR  
bloco_SEQ → SEQ stmts  
bloco_PAR → PAR stmts  
tipos_var → ...  
stmts → atribuição 
    | if ( bool ) stmt 
    | if ( bool ) stmt else stmt 
    | while ( bool ) stmt  
    | // instanciacao de objetos
    | // chamada de métodos
    | // outras instruções (do, for, etc.)

atribuição → id = expr  
expr → ...  
c_channel → chan id id_comp1 id_comp2  
...
```

> [!WARNING]  
> A produção `<stmts>` deve ser atualizada para considerar instanciação de objetos, chamada de métodos e outras instruções como `do` e `for`.

---

### 🧩 Orientação a Objetos (Exemplo em Java)

```bnf
<programa> → { <classe> }
<classe> → "class" <identificador> [ "extends" <identificador> ] "{" { <declaracao_metodo> } "}"
// Acrescentar não terminal para <declaração de atributos> em <classe>

<declaracao_metodo> → <tipo_retorno> <identificador> "(" ")" "{" { <comando> } "}"
<tipo_retorno> → "void" | "int" | "float" | "String" | <identificador> // tipo personalizado

<comando> → <instanciacao> | <chamada_metodo> | <print_comando>

<instanciacao> → <identificador> <identificador> "=" "new" <identificador> "(" ")" ";"
<chamada_metodo> → <identificador> "." <identificador> "(" ")" ";"
<print_comando> → "print" "(" <texto> ")" ";"

<identificador> → <letra> { <letra> | <digito> }
<texto> → '"' { <caractere> } '"'
<letra> → "A" | ... | "Z" | "a" | ... | "z"
```

> [!TIP]
> Essas regras representam a Orientação a Objetos em Java. Caso deseje utilizar outra sintaxe (ex: Python), adapte as produções para a abordagem orientada a objetos da linguagem escolhida.

---

### ⚠️ Observações

1. **programa_minipar**: Representa um programa a ser executado pelo Interpretador MiniPar.
2. **Precedência de Operadores**: Considere precedência para operadores aritméticos `+`, `-`, `*`, `/` ao completar a produção `expr`.
3. **Funções e Recursão**: Utilize funções e recursão.
4. **Gramática Completa**: Conclua a gramática da linguagem MiniPar para cobrir todos os programas de teste.
5. **Tratamento de Erros**: Implemente tratamento de erros no interpretador MiniPar.

---

## 3. Programas de Teste (na linguagem MINIPAR 2025.1)

> [!TIP]
> Lembre-se de utilizar comentários via `#`  
> Implemente utilizando classes e objetos da Linguagem MiniPar Orientada a Objetos

<details>
<summary><strong>Programa de Teste 1: Cliente-Servidor Calculadora</strong></summary>

```minipar
programa-minipar
# Programa cliente servidor de uma calculadora aritmética simples
# O cliente (computador_1) solicita a execução de uma operação aritmética e o servidor (computador_2) realiza o cálculo retornando o resultado para o cliente

c_channel calculadora computador_1 computador_2
# declaração do canal de comunicação calculadora associada a dois computadores (computador_1 e computador_2)

SEQ
# Depois do SEQ todas as seguintes instruções indentadas serão executadas de forma sequencial
# Apresentar na tela via print as opções da calculadora +, -, *, /

# Ler a operação aritmética desejada via sys.stdin.readline()
# Ler o operando 1 (valor) e operando 2 (valor) via sys.stdin.readline()

calculadora.send (operação, valor1, valor2, resultado)
# Computador cliente (computador_1)
# Imprime o resultado via print

---- Execução: Servidor (computador 2)
# Computador servidor
calculadora.receive (operação, valor1, valor2, resultado)
# Computador 2 recebe a solicitação do cliente (computador 1), executa o cálculo e retorna o resultado ao cliente (computador 1)
```

</details>

<details>
<summary><strong>Programa de Teste 2: Execução Paralela (Threads)</strong></summary>

```minipar
programa-minipar
PAR
# Execução paralela (implementação via uso de threads em Java ou Phyton)
# depois do PAR todas as seguintes instruções indentadas serão executadas de forma “paralela“

# A seguir colocar o Código do Cálculo do Fatorial de um número (thread 1)
... linhas de código do Cálculo do Fatorial de um número na linguagem MiniPar

# A seguir colocar o Código do Cálculo da Série de Fibonacci (thread 2)
... linhas de código do Cálculo da Série de Fibonacci na linguagem MiniPar
```

</details>

<details>
<summary><strong>Programa de Teste 3: Neurônio (Classes e Objetos)</strong></summary>

```python
class Neuronio:
    def __init__(self, input_val, output_desejado, peso_inicial=0.5, peso_bias_inicial=0.5, taxa_aprendizado=0.01):
        self.input_val = input_val
        self.output_desejado = output_desejado
        self.peso = peso_inicial
        self.bias = 1
        self.peso_bias = peso_bias_inicial
        self.taxa_aprendizado = taxa_aprendizado
        self.erro = float('inf')
        self.iteracao = 0

    def ativacao(self, soma):
        return 1 if soma >= 0 else 0

    def treinar(self):
        print(f"Entrada: {self.input_val} | Saída desejada: {self.output_desejado}")
        while self.erro != 0:
            self.iteracao += 1
            print(f"\n#### Iteração: {self.iteracao}")
            print(f"Peso: {self.peso:.4f}")
            soma = (self.input_val * self.peso) + (self.bias * self.peso_bias)
            saida = self.ativacao(soma)
            print(f"Saída: {saida}")
            self.erro = self.output_desejado - saida
            print(f"Erro: {self.erro}")
            if self.erro != 0:
                self.peso += self.taxa_aprendizado * self.input_val * self.erro
                print(f"Peso do bias: {self.peso_bias:.4f}")
                self.peso_bias += self.taxa_aprendizado * self.bias * self.erro
        print("\n✅ Parabéns! O neurônio aprendeu.")
        print(f"Valor desejado: {self.output_desejado}")

# Exemplo de uso
neuronio = Neuronio(input_val=1, output_desejado=0)
neuronio.treinar()
```

<details>
<summary>Execução</summary>

```text
Entrada: 1 | Saída desejada: 0
#### Iteração: 1
Peso: 0.5000
Saída: 1
Erro: -1
Peso do bias: 0.5000
...
#### Iteração: 51
Peso: -0.0000
Saída: 0
Erro: 0
Parabéns! O neurônio aprendeu.
Valor desejado: 0
```
</details>
</details>

<details>
<summary><strong>Programa de Teste 4: Rede Neural XOR</strong></summary>

```python
# Rede Neural para aprender XOR com camada oculta de 3 neurônios
# Implementação manual de feedforward e backpropagation

import random
import math

def sigmoid(x):
    return 1 / (1 + math.exp(-x))

def sigmoid_derivative(x):
    return x * (1 - x)

class Neuronio:
    def __init__(self, num_inputs):
        self.pesos = [random.random() for _ in range(num_inputs)]
        self.bias = random.random()
        self.saida = 0

    def feedforward(self, entradas):
        soma = sum([entradas[i] * self.pesos[i] for i in range(len(entradas))]) + self.bias
        self.saida = sigmoid(soma)
        return self.saida

    def calcular_derivada(self):
        return sigmoid_derivative(self.saida)

class RedeNeural:
    def __init__(self, taxa_aprendizado=0.2):
        self.taxa_aprendizado = taxa_aprendizado
        self.camada_oculta = [Neuronio(2) for _ in range(3)]
        self.neuronio_saida = Neuronio(3)

    def feedforward(self, entradas):
        saidas_ocultas = [neuronio.feedforward(entradas) for neuronio in self.camada_oculta]
        saida_final = self.neuronio_saida.feedforward(saidas_ocultas)
        return saidas_ocultas, saida_final

    def backpropagation(self, entradas, saida_desejada, saidas_ocultas, saida_final):
        erro = saida_desejada - saida_final
        delta_saida = erro * self.neuronio_saida.calcular_derivada()
        for i in range(len(self.neuronio_saida.pesos)):
            self.neuronio_saida.pesos[i] += saidas_ocultas[i] * delta_saida * self.taxa_aprendizado
            self.neuronio_saida.bias += delta_saida * self.taxa_aprendizado
        for i, neuronio in enumerate(self.camada_oculta):
            delta_oculto = delta_saida * self.neuronio_saida.pesos[i] * neuronio.calcular_derivada()
            for j in range(len(neuronio.pesos)):
                neuronio.pesos[j] += entradas[j] * delta_oculto * self.taxa_aprendizado
            neuronio.bias += delta_oculto * self.taxa_aprendizado

    def treinar(self, entradas, saidas_desejadas, epocas=20000):
        for _ in range(epocas):
            for entrada, saida_desejada in zip(entradas, saidas_desejadas):
                saidas_ocultas, saida_final = self.feedforward(entrada)
                self.backpropagation(entrada, saida_desejada, saidas_ocultas, saida_final)

    def testar(self, entradas):
        for entrada in entradas:
            _, saida_final = self.feedforward(entrada)
            print(f"Input: {entrada}, Predicted Output: {saida_final:.4f}")

entradas = [[0, 0], [0, 1], [1, 0], [1, 1]]
saidas_desejadas = [0, 1, 1, 0]

rede = RedeNeural()
rede.treinar(entradas, saidas_desejadas)
rede.testar(entradas)
```

<details open>
<summary>Execução</summary>

```text
Input: [0, 0], Predicted Output: 0.0089
Input: [0, 1], Predicted Output: 0.9769
Input: [1, 0], Predicted Output: 0.9758
Input: [1, 1], Predicted Output: 0.0291
```
</details>
</details>

<details>
<summary><strong>Programa de Teste 5: Sistema de Recomendação (Rede Neural)</strong></summary>

```python
import math

class Produto:
    def __init__(self, nome):
        self.nome = nome

class Categoria:
    def __init__(self, nome, produtos):
        self.nome = nome
        self.produtos = [Produto(p) for p in produtos]

class Usuario:
    def __init__(self, historico_compras):
        self.historico_compras = historico_compras

    def codificar_historico(self, todos_produtos):
        return [1 if produto in self.historico_compras else 0 for produto in todos_produtos]

class RedeNeural:
    def __init__(self, input_size, hidden_size, output_size):
        self.W1 = [[0.5 for _ in range(hidden_size)] for _ in range(input_size)]
        self.b1 = [0.5] * hidden_size
        self.W2 = [[0.5 for _ in range(output_size)] for _ in range(hidden_size)]
        self.b2 = [0.5] * output_size

    def relu(self, x):
        return [max(0, i) for i in x]

    def sigmoid(self, x):
        return [1 / (1 + math.exp(-i)) for i in x]

    def forward(self, X):
        Z1 = [sum(X[j] * self.W1[j][i] for j in range(len(X))) + self.b1[i] for i in range(len(self.b1))]
        A1 = self.relu(Z1)
        Z2 = [sum(A1[j] * self.W2[j][i] for j in range(len(A1))) + self.b2[i] for i in range(len(self.b2))]
        A2 = self.sigmoid(Z2)
        return A2

class Recomendador:
    def __init__(self, usuario, categorias):
        self.usuario = usuario
        self.categorias = categorias
        self.todos_produtos = [produto.nome for categoria in categorias for produto in categoria.produtos]

    def recomendar(self):
        entrada_codificada = self.usuario.codificar_historico(self.todos_produtos)
        input_size = len(entrada_codificada)
        hidden_size = 10
        output_size = len(entrada_codificada)
        rede = RedeNeural(input_size, hidden_size, output_size)
        saida = rede.forward(entrada_codificada)
        recomendacoes = [
            self.todos_produtos[i]
            for i in range(len(saida))
            if saida[i] > 0.5 and self.todos_produtos[i] not in self.usuario.historico_compras
        ]
        return recomendacoes

categorias = [
    Categoria("Eletrônicos", ["Smartphone", "Laptop", "Tablet", "Fones de ouvido"]),
    Categoria("Roupas", ["Camisa", "Jeans", "Jaqueta", "Sapatos"]),
    Categoria("Eletrodomésticos", ["Geladeira", "Micro-ondas", "Máquina de lavar", "Ar condicionado"]),
    Categoria("Livros", ["Ficção", "Não-ficção", "Ficção científica", "Fantasia"])
]

usuario = Usuario(["Smartphone", "Jeans", "Micro-ondas", "Ficção"])
recomendador = Recomendador(usuario, categorias)
recomendacoes = recomendador.recomendar()

print("Produtos recomendados para você:")
for produto in recomendacoes:
    print(produto)
```

<details open>
<summary>Execução</summary>

```text
Produtos recomendados para você:
Laptop
Tablet
Fones de ouvido
Camisa
Jaqueta
Sapatos
Geladeira
Máquina de lavar
Ar condicionado
Não-ficção
Ficção científica
Fantasia
```
</details>
</details>

<details>
<summary><strong>Programa de Teste 6: QuickSort</strong></summary>

```python
class Quicksort:
    def __init__(self, array):
        self.array = array

    def ordenar(self):
        return self._quicksort(self.array)

    def _quicksort(self, array):
        if len(array) <= 1:
            return array
        else:
            pivot = array[0]
            menores = [x for x in array[1:] if x <= pivot]
            maiores = [x for x in array[1:] if x > pivot]
            return self._quicksort(menores) + [pivot] + self._quicksort(maiores)

class InterfaceOrdenacao:
    def iniciar(self):
        print("==== Ordenação com Quicksort ====")
        print("Insira os elementos do vetor separados por espaço:")
        try:
            entrada = input().strip()
            array = list(map(int, entrada.split()))
            print(f"Vetor original: {array}")
            algoritmo = Quicksort(array)
            vetor_ordenado = algoritmo.ordenar()
            print(f"Vetor ordenado: {vetor_ordenado}")
        except ValueError:
            print("Por favor, insira apenas números separados por espaço.")
            self.iniciar() # Reinicia a interface em caso de erro

if __name__ == "__main__":
    interface = InterfaceOrdenacao()
    interface.iniciar()
```

<details open>
<summary>Execução</summary>

```text
==== Ordenação com Quicksort ====
Insira os elementos do vetor separados por espaço:
10 -3 -40 80 70 -100
Vetor original: [10, -3, -40, 80, 70, -100]
Vetor ordenado: [-100, -40, -3, 10, 70, 80]
```
</details>
</details>

---

## 4. Entrega do Tema 2

### 📋 Checklist de Entrega

- [ ] **Pseudocódigo dos Componentes**
  - Analisador Léxico (Lexer)
  - Analisador Sintático (Parser)
  - Analisador Semântico e Tratamento de Erros (incluindo suporte a comentários via `#`)
  - Tabela de Símbolos
- [ ] **Interface do Interpretador MINIPAR**
  <details>
    <summary><strong>Exemplo da Interface</strong></summary>
    <img src="./.github/images/interface_minipar.png" alt="Exemplo da interface do Interpretador MINIPAR" width="600"/>
  </details>
- [ ] **Product Backlog**  
  Lista de funcionalidades e requisitos do Interpretador MINIPAR
- [ ] **Sprints Backlog**  
  Planejamento das entregas incrementais (sprints) do projeto
- [ ] **Diagramas UML**
  - Diagrama de Casos de Uso
  - Arquitetura utilizando Componentes de Software
  - Diagrama de Classes dos Componentes
- [ ] **Testes de Software**
  - Testes de Integração do Compilador MINIPAR
  - Testes dos Programas de Exemplo na Linguagem MINIPAR
- [ ] **Implementação**
  - Tecnologias Utilizadas: Descrever linguagem, ambiente de desenvolvimento e ferramentas.
  - Código Fonte: O interpretador deve conter comentários explicativos.
  - Execução dos Programas de Teste: Passo a passo ilustrado das telas de execução.
- [ ] **Repositório no GitHub**
  - Código fonte, programas de teste e relatório do Compilador MINIPAR utilizando Componentes de Software.
  - Link do repositório incluído no relatório.

---

## Apêndice 1

<div align="center">

### Modelagem do Compilador para a Linguagem MINIPAR  
**Orientada a Objetos utilizando Componentes de Software**

</div>

---

### 🎨 Tela do Interpretador MINIPAR

<p align="center">
  <img src="./.github/images/interface_minipar.png" alt="Interface do Interpretador MINIPAR" width="600"/>
</p>

---

<div align="center">

## MODELAGEM DO INTERPRETADOR DA LINGUAGEM MINIPAR

</div>

<p align="center">
  <img src="./.github/images/arquitetura_uml.png" alt="Arquitetura do interpretador utilizando a UML" width="700"/>
</p>

<p align="center">
  <img src="./.github/images/componentes_uml.png" alt="Diagrama de componentes do Interpretador Utilizando a UML" width="700"/>
</p>

<p align="center">
  <img src="./.github/images/diagrama_classes_uml.png" alt="Diagrama de Classes dos Componentes" width="700"/>
</p>

---

## Apêndice 2

### Exemplo de modelagem de um Componente

> [!TIP]
> Representar um relógio através de um componente de software usando a notação UML.

![Componente de software "Relógio" utilizando a notação UML](./.github/images/componente_relogio_uml.png)

**Operações da interface `IRelogio` do componente `Relogio`:**
- `mostrar_hora()`: Exibe a hora atual.
- `atualizar_data_hora(data)`: Atualiza a data e hora do relógio.
- `forma_relogio(f)`: Define o formato do relógio, onde `f` pode ser 1 (digital) ou 0 (analógico).

#### Modelagem do Componente Relogio (via Diagrama de Classes UML)

![Modelagem do componente "Relogio" em UML](./.github/images/relogio_uml.png)

A classe Relogio deve fornecer a implementação de cada operação (`mostrar_hora ()` – `atualizar_data_hora (data)` – `forma_relogio (f)`) da Interface IRelogio.

#### Exemplo de Implementação do Componente Relógio

<details open>
<summary>Implementação em Java</summary>

```java
abstract class RelogioAbstrato {
    // Métodos abstratos: devem ser implementados pelas subclasses
    public abstract void atualizar_data_hora(String data);
    public abstract void forma_relogio(String f);
    public abstract void mostrar_hora();
}

class Relogio extends RelogioAbstrato {
    private String dataHora;
    private String formato;

    @Override
    public void mostrar_hora() {
        System.out.println("Hora atual: " + dataHora + " (Formato: " + formato + ")");
    }

    @Override
    public void atualizar_data_hora(String data) {
        this.dataHora = data;
        System.out.println("Data e hora atualizadas para: " + dataHora);
    }

    @Override
    public void forma_relogio(String f) {
        this.formato = f;
        System.out.println("Formato do relógio definido para: " + formato);
    }
}

public class Aplicacao {
    public static void main(String[] args) {
        // Usando a classe abstrata como tipo de referência
        RelogioAbstrato meuRelogio = new Relogio();
        meuRelogio.atualizar_data_hora("14/08/2025 08:00");
        meuRelogio.forma_relogio("24h");
        meuRelogio.mostrar_hora();
    }
}
```

</details>

<details>
<summary>Execução (Saída)</summary>

```text
Data e hora atualizadas para: 14/08/2025 08:00
Formato do relógio definido para: 24h
Hora atual: 14/08/2025 08:00 (Formato: 24h)
```
</details>
