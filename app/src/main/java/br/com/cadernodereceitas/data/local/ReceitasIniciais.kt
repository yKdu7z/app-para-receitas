package br.com.cadernodereceitas.data.local

import br.com.cadernodereceitas.model.Categoria
import br.com.cadernodereceitas.model.Ingrediente
import br.com.cadernodereceitas.model.Receita

/** Receitas gravadas no banco na primeira vez que o app é aberto. */
object ReceitasIniciais {

    val lista = listOf(
        Receita(
            nome = "Bolo de cenoura com calda de chocolate",
            favorita = true,
            categoria = Categoria.DOCES,
            tempoPreparoMin = 50,
            rendimento = "12 fatias",
            ingredientes = listOf(
                Ingrediente("Cenouras médias", "3"),
                Ingrediente("Ovos", "4"),
                Ingrediente("Óleo", "1 xícara"),
                Ingrediente("Açúcar", "2 xícaras"),
                Ingrediente("Farinha de trigo", "2 xícaras"),
                Ingrediente("Fermento em pó", "1 colher (sopa)"),
                Ingrediente("Chocolate em pó (calda)", "4 colheres (sopa)"),
                Ingrediente("Açúcar (calda)", "1 xícara"),
                Ingrediente("Manteiga (calda)", "1 colher (sopa)"),
                Ingrediente("Leite (calda)", "4 colheres (sopa)"),
            ),
            passos = listOf(
                "Bata no liquidificador as cenouras picadas, os ovos e o óleo até ficar liso.",
                "Passe para uma tigela, junte o açúcar e a farinha e misture. Por último, acrescente o fermento.",
                "Asse em forma untada a 180 °C por cerca de 40 minutos.",
                "Para a calda, leve todos os ingredientes ao fogo baixo, mexendo até engrossar, e espalhe sobre o bolo ainda quente.",
            ),
            dica = "Faça o teste do palito antes de tirar do forno: se sair limpo, está pronto.",
        ),
        Receita(
            nome = "Pão de queijo",
            categoria = Categoria.SALGADOS,
            tempoPreparoMin = 45,
            rendimento = "25 unidades",
            ingredientes = listOf(
                Ingrediente("Polvilho azedo", "500 g"),
                Ingrediente("Leite", "1 xícara"),
                Ingrediente("Óleo", "½ xícara"),
                Ingrediente("Sal", "1 colher (chá)"),
                Ingrediente("Ovos", "2"),
                Ingrediente("Queijo meia cura ralado", "200 g"),
            ),
            passos = listOf(
                "Ferva o leite com o óleo e o sal.",
                "Escalde o polvilho com essa mistura e mexa até amornar.",
                "Junte os ovos um a um e depois o queijo, sovando até a massa soltar das mãos.",
                "Faça bolinhas e asse a 200 °C por 25 minutos.",
            ),
            dica = "Dá para congelar as bolinhas cruas e assar direto do freezer.",
        ),
        Receita(
            nome = "Brigadeiro de panela",
            categoria = Categoria.DOCES,
            tempoPreparoMin = 15,
            rendimento = null,
            ingredientes = listOf(
                Ingrediente("Leite condensado", "1 lata"),
                Ingrediente("Manteiga", "1 colher (sopa)"),
                Ingrediente("Chocolate em pó", "3 colheres (sopa)"),
                Ingrediente("Granulado"),
            ),
            passos = listOf(
                "Coloque o leite condensado, a manteiga e o chocolate em uma panela em fogo baixo.",
                "Mexa sem parar até desgrudar do fundo, por uns 10 minutos.",
                "Sirva de colher com granulado por cima ou deixe esfriar para enrolar.",
            ),
        ),
        Receita(
            nome = "Arroz de forno",
            categoria = Categoria.SALGADOS,
            tempoPreparoMin = 40,
            rendimento = "6 porções",
            ingredientes = listOf(
                Ingrediente("Arroz cozido", "4 xícaras"),
                Ingrediente("Frango cozido e desfiado", "2 xícaras"),
                Ingrediente("Creme de leite", "1 caixa"),
                Ingrediente("Milho", "1 lata"),
                Ingrediente("Ervilha", "1 lata"),
                Ingrediente("Muçarela", "200 g"),
                Ingrediente("Sal e pimenta-do-reino"),
            ),
            passos = listOf(
                "Misture o arroz, o frango, o creme de leite, o milho e a ervilha. Tempere com sal e pimenta.",
                "Coloque em um refratário e cubra com a muçarela.",
                "Leve ao forno a 200 °C por 20 minutos, até o queijo derreter.",
            ),
            dica = "Bom jeito de aproveitar o arroz que sobrou do almoço.",
        ),
        Receita(
            nome = "Feijão tropeiro",
            categoria = Categoria.SALGADOS,
            tempoPreparoMin = 35,
            rendimento = "4 porções",
            ingredientes = listOf(
                Ingrediente("Feijão carioca cozido", "3 xícaras"),
                Ingrediente("Bacon em cubos", "150 g"),
                Ingrediente("Linguiça calabresa", "1 unidade"),
                Ingrediente("Ovos", "3"),
                Ingrediente("Farinha de mandioca", "1 xícara"),
                Ingrediente("Cebola", "1 unidade"),
                Ingrediente("Alho", "2 dentes"),
                Ingrediente("Cheiro-verde"),
            ),
            passos = listOf(
                "Frite o bacon e a linguiça picada até dourar.",
                "Junte a cebola e o alho e refogue.",
                "Abra um espaço na panela e mexa os ovos até cozinharem.",
                "Acrescente o feijão escorrido e, aos poucos, a farinha, mexendo sempre. Finalize com cheiro-verde.",
            ),
            dica = "Use o feijão bem escorrido para a farinha não empelotar.",
        ),
        Receita(
            nome = "Torta de frango de liquidificador",
            categoria = Categoria.SALGADOS,
            tempoPreparoMin = 60,
            rendimento = "10 pedaços",
            ingredientes = listOf(
                Ingrediente("Ovos", "3"),
                Ingrediente("Leite", "2 xícaras"),
                Ingrediente("Óleo", "½ xícara"),
                Ingrediente("Farinha de trigo", "2 xícaras"),
                Ingrediente("Queijo parmesão ralado", "3 colheres (sopa)"),
                Ingrediente("Fermento em pó", "1 colher (sopa)"),
                Ingrediente("Frango cozido e desfiado", "2 xícaras"),
                Ingrediente("Tomate", "1 unidade"),
                Ingrediente("Cebola", "1 unidade"),
                Ingrediente("Azeitonas"),
            ),
            passos = listOf(
                "Refogue o frango com a cebola e o tomate picados e reserve.",
                "Bata no liquidificador os ovos, o leite, o óleo, a farinha e o queijo. Misture o fermento com uma colher.",
                "Despeje metade da massa numa forma untada, espalhe o recheio e as azeitonas e cubra com o restante.",
                "Asse a 180 °C por 40 minutos, até dourar.",
            ),
        ),
        Receita(
            nome = "Pudim de leite condensado",
            categoria = Categoria.DOCES,
            tempoPreparoMin = 90,
            rendimento = "8 fatias",
            ingredientes = listOf(
                Ingrediente("Leite condensado", "1 lata"),
                Ingrediente("Leite", "1 lata (mesma medida)"),
                Ingrediente("Ovos", "3"),
                Ingrediente("Açúcar (calda)", "1 xícara"),
            ),
            passos = listOf(
                "Derreta o açúcar em fogo baixo até virar caramelo e espalhe na forma de furo central.",
                "Bata o leite condensado, o leite e os ovos no liquidificador.",
                "Despeje na forma, cubra com papel-alumínio e asse em banho-maria a 180 °C por cerca de 1 hora.",
            ),
            dica = "Deixe na geladeira por pelo menos 4 horas antes de desenformar.",
        ),
        Receita(
            nome = "Limonada suíça",
            favorita = true,
            categoria = Categoria.BEBIDAS,
            tempoPreparoMin = 10,
            rendimento = "1 litro",
            ingredientes = listOf(
                Ingrediente("Limões-taiti", "4"),
                Ingrediente("Água gelada", "1 litro"),
                Ingrediente("Leite condensado", "½ lata"),
                Ingrediente("Gelo"),
            ),
            passos = listOf(
                "Lave bem os limões, corte as pontas e divida cada um em quatro.",
                "Bata no liquidificador com a água e o leite condensado por poucos segundos.",
                "Coe e sirva com gelo.",
            ),
            dica = "Bata rápido e coe na hora para não amargar.",
        ),
        Receita(
            nome = "Suco de maracujá",
            categoria = Categoria.BEBIDAS,
            tempoPreparoMin = 10,
            rendimento = null,
            ingredientes = listOf(
                Ingrediente("Maracujás", "2"),
                Ingrediente("Água gelada", "1 litro"),
                Ingrediente("Açúcar"),
                Ingrediente("Gelo"),
            ),
            passos = listOf(
                "Retire a polpa dos maracujás.",
                "Bata com a água em pulsos rápidos, só para soltar as sementes.",
                "Coe, adoce a gosto e sirva gelado.",
            ),
        ),
    )
}
