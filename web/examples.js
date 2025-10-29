// Exemplos de código MiniPar
const examples = {
    hello: `# Exemplo: Hello World
println("Olá, MiniPar!");
println("Bem-vindo ao interpretador web!");`,

    variables: `# Exemplo: Variáveis e Operações
number x = 10;
number y = 20;
number soma = x + y;
number multiplicacao = x * y;

println("x = ", x);
println("y = ", y);
println("Soma: ", soma);
println("Multiplicação: ", multiplicacao);

bool ativo = true;
string nome = "MiniPar";
println("Linguagem: ", nome);
println("Ativo: ", ativo);`,

    function: `# Exemplo: Funções
func fatorial(number n) -> number {
    if (n <= 1) {
        return 1;
    }
    return n * fatorial(n - 1);
}

func fibonacci(number n) -> number {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

println("Fatorial de 5: ", fatorial(5));
println("Fibonacci de 7: ", fibonacci(7));

number resultado = fatorial(6);
println("Fatorial de 6: ", resultado);`,

    class: `# Exemplo: Classes e Objetos
class Pessoa {
    string nome;
    number idade;
    
    void inicializar(string n, number i) {
        this.nome = n;
        this.idade = i;
    }
    
    void apresentar() {
        println("Nome: ", this.nome);
        println("Idade: ", this.idade);
    }
}

class Estudante extends Pessoa {
    string curso;
    
    void definirCurso(string c) {
        this.curso = c;
    }
    
    void mostrarInfo() {
        this.apresentar();
        println("Curso: ", this.curso);
    }
}

Pessoa p = new Pessoa();
p.inicializar("João", 25);
p.apresentar();

println("---");

Estudante e = new Estudante();
e.inicializar("Maria", 20);
e.definirCurso("Ciência da Computação");
e.mostrarInfo();`,

    loop: `# Exemplo: Loops
println("=== For Loop ===");
for (number i in [1, 2, 3, 4, 5]) {
    println("Número: ", i);
}

println("\n=== While Loop ===");
number contador = 1;
while (contador <= 5) {
    println("Contador: ", contador);
    contador = contador + 1;
}

println("\n=== Do-While Loop ===");
number x = 1;
do {
    println("x = ", x);
    x = x + 1;
} while (x <= 3);`,

    list: `# Exemplo: Listas e Dicionários
# Listas
list numeros = [1, 2, 3, 4, 5];
println("Lista de números: ", numeros);

list frutas = ["maçã", "banana", "laranja"];
println("Frutas: ", frutas);

# Iterando sobre lista
println("\nIterando sobre frutas:");
for (string fruta in frutas) {
    println("- ", fruta);
}

# Dicionário
dict pessoa = {
    "nome": "Alice",
    "idade": 30,
    "cidade": "São Paulo"
};
println("\nDicionário pessoa: ", pessoa);`,

    parallel: `# Exemplo: Blocos Paralelos e Sequenciais
println("=== Bloco Sequencial ===");
seq {
    println("Tarefa 1");
    println("Tarefa 2");
    println("Tarefa 3");
}

println("\n=== Bloco Paralelo ===");
par {
    println("Processo A");
    println("Processo B");
    println("Processo C");
}

println("\nFim da execução");`
};

// Função para obter um exemplo
function getExample(name) {
    return examples[name] || examples.hello;
}
