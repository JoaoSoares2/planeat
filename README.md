# planeat
Projeto desenvolvido para a Global Solution 2 de 2026 (CitizenTech) da FIAP

## Front-end

O MVP web está disponível em `index.html` e pode ser aberto diretamente no navegador ou servido por qualquer servidor estático. O menu conecta o planejamento semanal, o cadastro de receitas e a lista de compras consolidada.

Receitas, refeições planejadas e o progresso da lista são salvos no `localStorage` do navegador. O projeto ainda não está conectado ao back-end Java ou a um banco de dados; essa integração pode substituir a camada de persistência local quando a API estiver disponível.

### Fluxo

1. Em **Receitas**, cadastre ou edite receitas e seus ingredientes no formato `ingrediente; quantidade; unidade`, um por linha.
2. Em **Planejamento**, escolha o dia, a refeição, a receita e o número de porções.
3. Em **Lista de compras**, confira os ingredientes somados entre as receitas e marque os itens no mercado.
