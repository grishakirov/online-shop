# BI-TJV semestral project



## Idea:

Mým semestrálním projektem bude implementace **internetového obchodu** „Hvězda“.

## Business logika:

* **Registrace uživatele**: Uživatel se může registrovat zadáním osobních údajů, včetně data narození (nullable).
*	**Vytvoření objednávky**: Uživatel může vytvořit objednávku produktů z e-shopu. Při vytváření objednávky se kontroluje dostupné množství produktů. Pokud uživatel požaduje více, než je skladem, objednávka upraví množství na maximální dostupné.
*	**Omezení prodeje (věkový limit)**: Při nákupu produktů s věkovým omezením systém kontroluje věk uživatele podle jeho data narození. Pokud není datum narození zadáno nebo uživatel nesplňuje věkový požadavek, objednávka bude zamítnuta a operace vrátí chybu.

## Složitější dotaz:

**Vymazání uživatele a/nebo jeho bonusové karty**: Administrátor může odstranit uživatele z věrnostního programu (a/nebo Bonus_card) pouze v případě, že uživatel **nemá žádné aktivní objednávky** (se statusem processing nebo shipped). Pokud žádné takové objednávky nejsou, může admin uživatele odstranit jak z věrnostního programu, tak případně i z celého systému. V opačném případě systém vrátí false.
