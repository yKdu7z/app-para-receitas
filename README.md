# Caderno de Receitas

Aplicativo Android para guardar receitas caseiras e usá-las na hora de cozinhar.
O usuário mantém o próprio caderno no celular (criar, editar, favoritar, excluir),
marca os ingredientes conforme separa tudo antes do preparo e pode buscar ideias
novas na internet (TheMealDB) para salvar no caderno e adaptar depois.

## Como rodar

1. Instale o [Android Studio](https://developer.android.com/studio) (2026.2 ou mais recente). Ele já traz o JDK necessário.
2. Clone o repositório e abra a pasta no Android Studio (**File > Open**).
3. Aguarde o *Gradle Sync* terminar (na primeira vez ele baixa o Gradle e as dependências).
4. Selecione um emulador ou celular com Android 8.0 (API 26) ou superior e clique em **Run ▶**.

Pela linha de comando: `./gradlew installDebug` (Linux/macOS) ou `gradlew.bat installDebug` (Windows).
Testes: `./gradlew testDebugUnitTest`.

A aba **Descobrir** precisa de internet; o caderno funciona offline.

### Chave da API

A TheMealDB oferece a chave pública de testes `1`, que é usada por padrão — **não é preciso configurar nada**.
Para usar outra chave, adicione ao arquivo `local.properties` (que não vai para o GitHub):

```
MEALDB_API_KEY=sua_chave
```

O valor é lido no `app/build.gradle.kts` e exposto ao código como `BuildConfig.MEALDB_API_KEY`, sem ficar escrito no código versionado.

## Fluxo principal

1. **Meu caderno**: lista das receitas salvas, com filtros (todas, favoritas, salgados, doces, bebidas).
2. **Detalhe**: ingredientes com checklist, modo de preparo, dica; favoritar, compartilhar, editar e excluir.
3. **Nova receita / Editar**: formulário com validação (nome, categoria, tempo, rendimento, ingredientes, passos, dica, foto).
4. **Descobrir**: busca na TheMealDB com fotos; ao abrir uma receita é possível **salvar no caderno**.
5. Tudo o que foi salvo continua lá depois de fechar o app (Room), assim como o tema escolhido (DataStore).

## Arquitetura

MVVM com fluxo unidirecional de dados: a tela envia eventos ao `ViewModel`, que expõe um `StateFlow<UiState>`
observado com `collectAsStateWithLifecycle`. Os dados chegam pelos `Repository`.

```
app/src/main/java/br/com/cadernodereceitas/
├── CadernoApp.kt / AppContainer.kt   # injeção de dependências manual
├── MainActivity.kt / TemaViewModel.kt # tema claro/escuro e início da navegação
├── model/                             # modelos de domínio (data classes imutáveis)
├── data/
│   ├── ReceitaRepository.kt           # interface + implementação com Room
│   ├── DescobertaRepository.kt        # interface + implementação com a TheMealDB
│   ├── PreferenciasRepository.kt      # DataStore (tema, receitas iniciais)
│   ├── local/                         # Room: entidade, DAO, banco, conversores, receitas iniciais
│   └── remoto/                        # Retrofit: API e DTO -> modelo de domínio
└── ui/
    ├── navegacao/                     # Navigation 3: rotas, back stack, abas
    ├── tema/                          # cores (claro/escuro) e tipografia
    ├── componentes/                   # componentes reutilizáveis e estados (carregando, vazio, erro)
    ├── caderno/  detalhe/  formulario/  descobrir/   # telas + ViewModels
```

| Requisito | Onde |
|---|---|
| Navigation 3 (rotas, entries, back stack, argumentos) | `ui/navegacao/Navegacao.kt` |
| `LazyColumn` / `LazyRow` | lista e filtros do caderno, detalhe; grade adaptativa em Descobrir |
| Formulário com validação | `ui/formulario/` (regras em `ValidacaoReceita.kt`) |
| Estados carregando / conteúdo / vazio / erro | `ListaCaderno`, `ResultadoBusca`, `DetalheUiState`, `DetalheDescobertaUiState` |
| Estado após rotação | ViewModels + `SavedStateHandle` (filtro, busca, ingredientes marcados) |
| Operação assíncrona com cancelamento | busca com `debounce` + `flatMapLatest` (cancela a anterior); recarga cancela o `Job` anterior |
| Persistência local reativa | Room com `Flow` e DataStore |
| Acessibilidade | descrições em ícones, `heading()` nos títulos, áreas de toque ≥ 48 dp, `liveRegion` nos avisos, textos em `sp` |
| Recursos do celular | Photo Picker (foto da receita) e compartilhamento |

## Bibliotecas

| Biblioteca | Para que é usada |
|---|---|
| Jetpack Compose + Material 3 | Interface declarativa e componentes visuais. |
| Navigation 3 | Navegação entre telas com back stack controlado pelo app. |
| Lifecycle (ViewModel, runtime-compose, viewmodel-navigation3) | ViewModels por tela e coleta de fluxos respeitando o ciclo de vida. |
| Room (+ KSP) | Banco de dados local das receitas. |
| DataStore Preferences | Preferência de tema e controle das receitas iniciais. |
| Retrofit + OkHttp | Chamadas HTTP à TheMealDB. |
| kotlinx.serialization | Leitura do JSON da API e gravação de listas no Room. |
| Coil | Carregamento das fotos (internet e arquivos locais). |
| JUnit + kotlinx-coroutines-test | Testes de regras, mapeamento da API e ViewModel. |

## Versões anteriores

A Etapa 1 (Android Views com XML) está preservada na tag `etapa-1`: `git checkout etapa-1`.
