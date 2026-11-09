# Especificação de Projeto: PlanEat - CitizenTech

## 1. Quem é o cidadão beneficiado
A solução é voltada para pessoas que moram sozinhas (com tempo escasso para gerenciar a rotina), jovens adultos em sua primeira experiência de gestão domiciliar, e os responsáveis pelas compras de supermercado em pequenos domicílios. O foco é o cidadão que busca otimização financeira, praticidade e uma rotina de consumo mais sustentável.

## 2. Qual problema será resolvido
O alto índice de desperdício de alimentos em ambiente doméstico. No Brasil, cerca de 60% das 27 milhões de toneladas de comida desperdiçadas anualmente vão para o lixo dentro das próprias casas. O PlanEat resolve a raiz desse problema: as compras impulsivas, a ausência de planejamento prévio de cardápio e a compra de porções superestimadas (a cultura da "fartura"). 

## 3. Como a solução vai funcionar
O sistema operará como uma aplicação web baseada em um funil de três passos (Planeja, Processa e Executa):
* **Planejamento:** O usuário acessa a plataforma (com autenticação) e cadastra suas refeições/receitas da semana ou dieta estipulada.
* **Processamento:** O sistema cruza os dados dos ingredientes necessários para os dias escolhidos e gera uma lista de compras inteligente e exata, consolidando ingredientes repetidos (evitando comprar a mais).
* **Execução e Histórico:** O cidadão vai ao mercado com uma lista cirúrgica. Todos os dados (usuários, receitas, listas) ficam salvos no banco de dados para rápida reutilização no futuro.

## 4. Quais tecnologias serão usadas
O projeto foi estruturado para entregar um MVP robusto e que atende a todos os critérios da Global Solution:
* **Back-end (Java):** Aplicação orientada a objetos (POO), estruturada em camadas rigorosas (Model, DAO, Service) e com tratamento de exceções.
* **Banco de Dados (Relacional):** Banco modelado com Diagrama Entidade-Relacionamento (DER), contendo no mínimo 4 tabelas interligadas (ex: `T_USUARIO`, `T_RECEITA`, `T_INGREDIENTE`, `T_RECEITA_INGREDIENTE`), com chaves primárias (PK) e estrangeiras (FK), inicializado por um arquivo `script.sql`.
* **Persistência de Dados (JDBC):** Conexão nativa e segura realizando o CRUD completo.
* **Front-end (Web):** HTML semântico, CSS (responsivo e intuitivo para uso no celular durante as compras) e JavaScript. A interface web será interligada com o back-end para gravar e ler dados reais do banco (critério de diferencial).

## 5. Quem participa do ecossistema
* **Usuário Final (Cidadão):** O protagonista que planeja e consome.
* **Supermercados e Produtores Locais:** No futuro, a plataforma poderá indicar a disponibilidade de ingredientes frescos diretamente de produtores próximos.
* **Nutricionistas:** Profissionais de saúde que poderão cadastrar dietas prontas para seus pacientes importarem no sistema.

## 6. Como o impacto será medido e ampliado
* **Alinhamento Global:** O projeto atua ativamente sobre o ODS 2 (Fome zero e agricultura sustentável) e o ODS 11 (Cidades e comunidades sustentáveis).
* **Medição:** O impacto primário será quantificado pela economia financeira do usuário (cruzando relatórios via SQL do quanto se gastava antes vs. o gasto exato da lista gerada) e por avaliações qualitativas da redução de lixo orgânico na lixeira.
* **Ampliação Futura:** Implementação de alertas automáticos de validade dos produtos guardados na geladeira e sugestão de receitas baseadas nas "sobras" (ingredientes que restaram na despensa).