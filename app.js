(() => {
  "use strict";

  const STORAGE_KEYS = {
    recipes: "planeat.recipes.v1",
    plan: "planeat.plan.v1",
    checked: "planeat.checked.v1"
  };
  const DAYS = ["Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"];
  const DAY_SHORT = ["SEG", "TER", "QUA", "QUI", "SEX", "SÁB", "DOM"];
  const SEED_RECIPES = [
    { id: "seed-bowl", name: "Bowl de grão-de-bico", category: "Almoço", servings: 2, ingredients: [{ name: "grão-de-bico", quantity: 200, unit: "g" }, { name: "tomate", quantity: 2, unit: "unidade" }, { name: "pepino", quantity: 1, unit: "unidade" }, { name: "azeite", quantity: 1, unit: "colher" }] },
    { id: "seed-omelete", name: "Omelete de espinafre", category: "Jantar", servings: 2, ingredients: [{ name: "ovo", quantity: 4, unit: "unidade" }, { name: "espinafre", quantity: 100, unit: "g" }, { name: "cebola", quantity: 1, unit: "unidade" }] },
    { id: "seed-massa", name: "Massa ao molho de tomate", category: "Almoço", servings: 2, ingredients: [{ name: "macarrão", quantity: 200, unit: "g" }, { name: "tomate", quantity: 3, unit: "unidade" }, { name: "cebola", quantity: 1, unit: "unidade" }, { name: "azeite", quantity: 1, unit: "colher" }] }
  ];
  const SEED_PLAN = [
    { id: "seed-plan-1", day: 0, meal: "Almoço", recipeId: "seed-bowl", servings: 2 },
    { id: "seed-plan-2", day: 1, meal: "Jantar", recipeId: "seed-omelete", servings: 2 },
    { id: "seed-plan-3", day: 3, meal: "Almoço", recipeId: "seed-massa", servings: 2 },
    { id: "seed-plan-4", day: 5, meal: "Jantar", recipeId: "seed-bowl", servings: 1 }
  ];

  function load(key, fallback) {
    try {
      const saved = localStorage.getItem(key);
      return saved === null ? fallback : JSON.parse(saved);
    } catch (error) {
      console.error(`Não foi possível carregar ${key} do armazenamento local.`, error);
      return fallback;
    }
  }

  function save(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
      return true;
    } catch (error) {
      console.error(`Não foi possível salvar ${key} no armazenamento local.`, error);
      return false;
    }
  }

  let recipes = load(STORAGE_KEYS.recipes, SEED_RECIPES);
  let plan = load(STORAGE_KEYS.plan, SEED_PLAN);
  let checkedItems = load(STORAGE_KEYS.checked, []);
  if (!Array.isArray(recipes)) recipes = [];
  if (!Array.isArray(plan)) plan = [];
  if (!Array.isArray(checkedItems)) checkedItems = [];

  function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, (char) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[char]);
  }

  function persistRecipes() { return save(STORAGE_KEYS.recipes, recipes); }
  function persistPlan() { return save(STORAGE_KEYS.plan, plan); }

  function weekDates() {
    const now = new Date();
    const monday = new Date(now);
    const weekday = (now.getDay() + 6) % 7;
    monday.setDate(now.getDate() - weekday);
    monday.setHours(0, 0, 0, 0);
    return DAYS.map((_, index) => {
      const date = new Date(monday);
      date.setDate(monday.getDate() + index);
      return date;
    });
  }

  function formatDate(date) {
    return new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "2-digit" }).format(date);
  }

  function initializeNavigation() {
    const activePage = document.body.dataset.page;
    document.querySelectorAll("[data-nav]").forEach((link) => {
      if (link.dataset.nav === activePage) {
        link.classList.add("active");
        link.setAttribute("aria-current", "page");
      }
    });
    const toggle = document.querySelector(".menu-toggle");
    const nav = document.querySelector(".main-nav");
    if (toggle && nav) {
      toggle.addEventListener("click", () => {
        const open = toggle.getAttribute("aria-expanded") !== "true";
        toggle.setAttribute("aria-expanded", String(open));
        nav.classList.toggle("open", open);
      });
    }
  }

  function getRecipe(id) {
    return recipes.find((recipe) => recipe.id === id);
  }

  function scaledIngredients(entry, recipe) {
    const multiplier = Number(entry.servings) / Number(recipe.servings);
    return recipe.ingredients.map((ingredient) => ({
      ...ingredient,
      quantity: Number(ingredient.quantity) * multiplier
    }));
  }

  function shoppingItems() {
    const combined = new Map();
    plan.forEach((entry) => {
      const recipe = getRecipe(entry.recipeId);
      if (!recipe) return;
      scaledIngredients(entry, recipe).forEach((ingredient) => {
        const name = ingredient.name.trim();
        const unit = ingredient.unit.trim();
        const key = `${name.toLocaleLowerCase("pt-BR")}::${unit.toLocaleLowerCase("pt-BR")}`;
        if (!combined.has(key)) combined.set(key, { key, name, unit, quantity: 0 });
        combined.get(key).quantity += ingredient.quantity;
      });
    });
    return [...combined.values()].sort((a, b) => a.name.localeCompare(b.name, "pt-BR"));
  }

  function formatQuantity(quantity) {
    return new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 2 }).format(quantity);
  }

  function createId() {
    return typeof crypto !== "undefined" && typeof crypto.randomUUID === "function"
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  }

  function renderPlanner() {
    const dates = weekDates();
    const label = document.getElementById("week-label");
    if (label) {
      label.textContent = `${formatDate(dates[0])} – ${formatDate(dates[6])}`;
      const daySelect = document.getElementById("plan-day");
      daySelect.innerHTML = DAYS.map((day, index) => `<option value="${index}">${day} · ${formatDate(dates[index])}</option>`).join("");
      const recipeSelect = document.getElementById("plan-recipe");
      recipeSelect.innerHTML = recipes.length
        ? recipes.map((recipe) => `<option value="${escapeHtml(recipe.id)}">${escapeHtml(recipe.name)}</option>`).join("")
        : '<option value="">Cadastre uma receita primeiro</option>';
      recipeSelect.disabled = recipes.length === 0;
      document.querySelector("#plan-form button[type=submit]").disabled = recipes.length === 0;

      const weekGrid = document.getElementById("week-grid");
      const today = new Date();
      const todayIndex = (today.getDay() + 6) % 7;
      weekGrid.innerHTML = DAYS.map((day, index) => {
        const meals = plan.filter((item) => Number(item.day) === index);
        const mealHtml = meals.map((entry) => {
          const recipe = getRecipe(entry.recipeId);
          if (!recipe) return "";
          return `<div class="meal-item"><div class="meal-info"><strong>${escapeHtml(recipe.name)}</strong><span>${escapeHtml(entry.meal)} · ${entry.servings} porç${Number(entry.servings) === 1 ? "ão" : "ões"}</span></div><button class="meal-remove" type="button" data-remove-plan="${escapeHtml(entry.id)}" aria-label="Remover ${escapeHtml(recipe.name)}">×</button></div>`;
        }).join("");
        return `<article class="day-card${index === todayIndex ? " today" : ""}"><div class="day-header"><span>${DAY_SHORT[index]}</span><time>${formatDate(dates[index])}${index === todayIndex ? " · HOJE" : ""}</time></div>${mealHtml || '<span class="muted">Sem refeições planejadas</span>'}</article>`;
      }).join("");
      document.getElementById("plan-empty").hidden = plan.length > 0;
      document.getElementById("planned-count").textContent = String(plan.length);
      document.getElementById("recipe-count").textContent = String(recipes.length);
      document.getElementById("ingredient-count").textContent = String(shoppingItems().length);
      weekGrid.onclick = (event) => {
        const button = event.target.closest("[data-remove-plan]");
        if (!button) return;
        plan = plan.filter((entry) => entry.id !== button.dataset.removePlan);
        if (persistPlan()) renderPlanner();
      };
    }

    const form = document.getElementById("plan-form");
    if (form) {
      form.onsubmit = (event) => {
        event.preventDefault();
        const message = document.getElementById("plan-message");
        const data = new FormData(form);
        const servings = Number(data.get("servings"));
        if (!getRecipe(data.get("recipe")) || !Number.isInteger(servings) || servings < 1 || servings > 20) {
          message.textContent = "Escolha uma receita e informe de 1 a 20 porções.";
          message.classList.remove("success");
          return;
        }
        plan.push({ id: createId(), day: Number(data.get("day")), meal: data.get("meal"), recipeId: data.get("recipe"), servings });
        if (persistPlan()) {
          message.textContent = "Refeição adicionada ao planejamento.";
          message.classList.add("success");
          form.querySelector("#plan-servings").value = "1";
          renderPlanner();
        } else message.textContent = "Não foi possível salvar. Verifique o armazenamento do navegador.";
      };
      form.querySelectorAll("[data-step]").forEach((button) => { button.onclick = () => {
        const input = document.getElementById("plan-servings");
        input.value = String(Math.min(20, Math.max(1, Number(input.value || 1) + Number(button.dataset.step))));
      }; });
    }
  }

  function parseIngredients(text) {
    const lines = text.split(/\r?\n/).map((line) => line.trim()).filter(Boolean);
    if (!lines.length) throw new Error("Informe pelo menos um ingrediente.");
    return lines.map((line, index) => {
      const parts = line.split(";").map((part) => part.trim());
      const quantity = Number(parts[1]?.replace(",", "."));
      if (parts.length !== 3 || !parts[0] || !Number.isFinite(quantity) || quantity <= 0 || !parts[2]) {
        throw new Error(`Confira a linha ${index + 1}. Use: ingrediente; quantidade; unidade.`);
      }
      return { name: parts[0], quantity, unit: parts[2] };
    });
  }

  function renderRecipes() {
    const list = document.getElementById("recipe-list");
    if (!list) return;
    document.getElementById("recipe-total").textContent = String(recipes.length);
    document.getElementById("recipes-empty").hidden = recipes.length > 0;
    list.innerHTML = recipes.map((recipe) => {
      const preview = recipe.ingredients.slice(0, 4).map((ingredient) => `${escapeHtml(ingredient.name)} · ${formatQuantity(Number(ingredient.quantity))} ${escapeHtml(ingredient.unit)}`).join("<br>");
      const extra = recipe.ingredients.length > 4 ? `<br>+ ${recipe.ingredients.length - 4} ingrediente(s)` : "";
      return `<article class="recipe-card"><div class="recipe-card-top"><div><h3>${escapeHtml(recipe.name)}</h3><span class="category-tag">${escapeHtml(recipe.category)}</span></div><div class="recipe-actions"><button type="button" class="icon-button" data-edit-recipe="${escapeHtml(recipe.id)}" aria-label="Editar ${escapeHtml(recipe.name)}">✎</button><button type="button" class="icon-button delete" data-delete-recipe="${escapeHtml(recipe.id)}" aria-label="Excluir ${escapeHtml(recipe.name)}">×</button></div></div><p class="recipe-meta">${recipe.servings} porç${Number(recipe.servings) === 1 ? "ão" : "ões"} · ${recipe.ingredients.length} ingrediente(s)</p><p class="ingredient-preview">${preview}${extra}</p></article>`;
    }).join("");

    const form = document.getElementById("recipe-form");
    const message = document.getElementById("recipe-message");
    const resetForm = (clearMessage = true) => {
      form.reset();
      document.getElementById("recipe-id").value = "";
      document.getElementById("recipe-servings").value = "2";
      document.getElementById("form-heading").textContent = "Nova receita";
      document.getElementById("recipe-submit").textContent = "Salvar receita";
      document.getElementById("recipe-cancel").hidden = true;
      if (clearMessage) {
        message.textContent = "";
        message.classList.remove("success");
      }
    };
    form.onsubmit = (event) => {
      event.preventDefault();
      const name = document.getElementById("recipe-name").value.trim();
      const servings = Number(document.getElementById("recipe-servings").value);
      try {
        if (name.length < 2) throw new Error("O nome precisa ter pelo menos 2 caracteres.");
        if (!Number.isInteger(servings) || servings < 1 || servings > 20) throw new Error("Informe entre 1 e 20 porções.");
        const ingredients = parseIngredients(document.getElementById("recipe-ingredients").value);
        const id = document.getElementById("recipe-id").value;
        const recipe = { id: id || createId(), name, category: document.getElementById("recipe-category").value.trim(), servings, ingredients };
        if (id) recipes = recipes.map((item) => item.id === id ? recipe : item);
        else recipes.unshift(recipe);
        if (!persistRecipes()) throw new Error("Não foi possível salvar. Verifique o armazenamento do navegador.");
        const successMessage = id ? "Receita atualizada com sucesso." : "Receita cadastrada com sucesso.";
        resetForm(false);
        message.textContent = successMessage;
        message.classList.add("success");
        renderRecipes();
      } catch (error) {
        message.textContent = error.message;
        message.classList.remove("success");
      }
    };
    document.getElementById("recipe-cancel").onclick = resetForm;
    list.onclick = (event) => {
      const editButton = event.target.closest("[data-edit-recipe]");
      if (editButton) {
        const recipe = getRecipe(editButton.dataset.editRecipe);
        if (!recipe) return;
        document.getElementById("recipe-id").value = recipe.id;
        document.getElementById("recipe-name").value = recipe.name;
        document.getElementById("recipe-category").value = recipe.category;
        document.getElementById("recipe-servings").value = recipe.servings;
        document.getElementById("recipe-ingredients").value = recipe.ingredients.map((item) => `${item.name}; ${item.quantity}; ${item.unit}`).join("\n");
        document.getElementById("form-heading").textContent = "Editar receita";
        document.getElementById("recipe-submit").textContent = "Salvar alterações";
        document.getElementById("recipe-cancel").hidden = false;
        message.textContent = "";
        document.getElementById("recipe-name").focus();
      }
      const deleteButton = event.target.closest("[data-delete-recipe]");
      if (deleteButton) {
        const recipe = getRecipe(deleteButton.dataset.deleteRecipe);
        if (!recipe || !window.confirm(`Excluir "${recipe.name}"? Refeições planejadas com ela também serão removidas.`)) return;
        recipes = recipes.filter((item) => item.id !== recipe.id);
        plan = plan.filter((item) => item.recipeId !== recipe.id);
        persistRecipes();
        persistPlan();
        renderRecipes();
      }
    };
  }

  function renderShopping() {
    const list = document.getElementById("shopping-list");
    if (!list) return;
    const items = shoppingItems();
    const dates = weekDates();
    const subtitle = document.getElementById("shopping-subtitle");
    subtitle.textContent = `Semana de ${formatDate(dates[0])} a ${formatDate(dates[6])} · quantidades ajustadas às porções.`;
    document.getElementById("shopping-meals").textContent = String(plan.length);
    document.getElementById("shopping-empty").hidden = items.length > 0;
    list.hidden = items.length === 0;
    list.innerHTML = items.map((item, index) => {
      const checked = checkedItems.includes(item.key);
      return `<label class="shopping-item${checked ? " checked" : ""}"><input type="checkbox" data-shopping-key="${escapeHtml(item.key)}" ${checked ? "checked" : ""}><span>${escapeHtml(item.name)}</span><strong>${formatQuantity(item.quantity)} ${escapeHtml(item.unit)}</strong></label>`;
    }).join("");
    updateProgress(items);
    list.onchange = (event) => {
      const checkbox = event.target.closest("[data-shopping-key]");
      if (!checkbox) return;
      if (checkbox.checked && !checkedItems.includes(checkbox.dataset.shoppingKey)) checkedItems.push(checkbox.dataset.shoppingKey);
      if (!checkbox.checked) checkedItems = checkedItems.filter((key) => key !== checkbox.dataset.shoppingKey);
      if (!save(STORAGE_KEYS.checked, checkedItems)) document.getElementById("shopping-message").textContent = "Não foi possível salvar o progresso desta lista.";
      renderShopping();
    };
    document.getElementById("clear-checked").onclick = () => {
      checkedItems = [];
      if (save(STORAGE_KEYS.checked, checkedItems)) {
        document.getElementById("shopping-message").textContent = "Itens desmarcados.";
        document.getElementById("shopping-message").classList.add("success");
      }
      renderShopping();
    };
  }

  function updateProgress(items) {
    const count = items.filter((item) => checkedItems.includes(item.key)).length;
    document.getElementById("checked-count").textContent = `${count}/${items.length}`;
    document.getElementById("progress-bar").style.width = `${items.length ? count / items.length * 100 : 0}%`;
  }

  initializeNavigation();
  renderPlanner();
  renderRecipes();
  renderShopping();
})();
