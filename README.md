# Caderno de Receitas

Aplicativo Android para guardar receitas caseiras e consultá-las na hora de cozinhar.
O usuário navega pelas receitas, filtra por categoria ou favoritas, abre os detalhes
e vai marcando os ingredientes conforme separa tudo antes de começar o preparo.

> **Etapa atual: Parcial (Android Views + XML).** Os dados são simulados (mocks) em memória.

## Como rodar

1. Instale o [Android Studio](https://developer.android.com/studio) (versão recente; ele já traz o JDK necessário).
2. Clone o repositório e abra a pasta do projeto no Android Studio (**File > Open**).
3. Aguarde o *Gradle Sync* terminar (na primeira vez ele baixa o Gradle e as dependências).
   Se o Android Studio pedir para escolher o *Gradle JVM*, selecione **JVM 21** (o projeto usa Gradle 8.9, compatível com Java até a versão 22).
4. Crie ou selecione um emulador (Android 8.0 / API 26 ou superior) ou conecte um celular com depuração USB.
5. Clique em **Run ▶** (configuração `app`).

Pela linha de comando: `./gradlew installDebug` (Linux/macOS) ou `gradlew.bat installDebug` (Windows).

O projeto não usa chaves de API nem senhas.

## Telas

| Tela | O que faz |
|---|---|
| **Lista de receitas** (`MainActivity`) | Lista com `RecyclerView`, filtros por categoria/favoritas (`ChipGroup`) e botão de favoritar em cada item. |
| **Detalhe da receita** (`DetalheReceitaActivity`) | Recebe o id da receita por `Intent` explícita e mostra tempo, rendimento, ingredientes, modo de preparo e dica. Permite favoritar pela barra superior. |

Os ingredientes ficam em um `Fragment` (`IngredientesFragment`) com checkboxes; o contador
"x de y separados" atualiza a cada toque e o estado é mantido ao girar a tela.

## Estrutura

```
app/src/main/java/br/com/cadernodereceitas/
├── model/Receita.kt          # data classes Receita e Ingrediente, enum Categoria
├── data/ReceitasMock.kt      # receitas simuladas e controle de favoritas
└── ui/
    ├── MainActivity.kt
    ├── ReceitaAdapter.kt      # ListAdapter + DiffUtil da lista
    ├── DetalheReceitaActivity.kt
    ├── IngredientesFragment.kt
    └── Formatos.kt            # formatação de tempo/resumo e ajuste de insets
```

## Bibliotecas

| Biblioteca | Para que é usada |
|---|---|
| `androidx.core:core-ktx` | Extensões Kotlin do Android (`isVisible`, `bundleOf`, `ContextCompat`, insets). |
| `androidx.appcompat:appcompat` | `AppCompatActivity` e suporte à barra de ações. |
| `androidx.activity:activity-ktx` | `enableEdgeToEdge()` para desenhar atrás das barras do sistema. |
| `androidx.fragment:fragment-ktx` | `Fragment` e a função `commit { }` do `FragmentManager`. |
| `androidx.recyclerview:recyclerview` | Lista de receitas. |
| `com.google.android.material:material` | Componentes Material 3: `Chip`, `MaterialToolbar`, `MaterialCheckBox`, divisores. |
