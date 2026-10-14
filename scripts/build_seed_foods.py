"""Builds the app's food seed from the nutritionist's portion table.

Usage, from the repository root:
    python3 scripts/build_seed_foods.py

Reads docs/piano-alimentare/porzioni_alternative.csv and writes
app/src/main/assets/seed/foods.json, one food per line. Combinations ("ASSOCIAZIONI"),
fruit and vegetables are not alternatives and are skipped. Meat is split into the app's
three protein categories following the Italian guidelines (docs/sviluppo.md).
"""
import csv
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / 'docs/piano-alimentare/porzioni_alternative.csv'
TARGET = ROOT / 'app/src/main/assets/seed/foods.json'

MEAT = {
    'WHITE_MEAT': ['Coniglio', 'Coscia di pollo (senza pelle)', 'Coscia di tacchino', 'Fesa di tacchino',
                   'Gallina', 'Petto di pollo', 'Petto di tacchino'],
    'RED_MEAT': ['Arista di maiale', 'Albese', 'Agnello', 'Carne di cavallo', 'Fegato', 'Lingua',
                 "Hamburger di chianina - Terre d'Italia", 'Hamburger di chianina - Sapori & Dintorni',
                 'Bistecca di maiale', 'Filetto di vitello', 'Vitellone'],
    'CURED_MEAT': ['Bresaola', 'Coppa', 'Mortadella', 'Prosciutto cotto', 'Prosciutto crudo', 'Salame',
                   'Salsiccia', 'Salsiccia di Bra', 'Speck'],
}
MEAT_CATEGORY = {name: category for category, names in MEAT.items() for name in names}
PROTEIN_GROUPS = {'Legumi': 'LEGUMES', 'Pesce': 'FISH', 'Uova': 'EGGS', 'Formaggi': 'CHEESE'}
# Carbs a meal-size portion stands for; side portions ("Pane e sostituti") and mixes have none.
CARBS = {
    'Riso / pasta / orzo / bulgur ecc.': ['PASTA', 'RICE', 'BARLEY'],
    'Farro': ['SPELT'],
    'Gnocchi': ['GNOCCHI'],
    'Udon': ['PASTA'],
    'Polenta': ['POLENTA'],
    "Pasta all'uovo": ['PASTA'],
    'Tortellini': ['PASTA'],
    'Agnolotti': ['PASTA'],
    'Cracker · invece della pasta': ['BREAD'],
    'Focaccia · invece della pasta': ['FOCACCIA'],
    'Gallette di riso · invece della pasta': ['BREAD'],
    'Mais · invece della pasta': ['POLENTA'],
    'Patate · invece della pasta': ['POTATOES'],
    'Pane · invece della pasta': ['BREAD'],
    'Piadina · invece della pasta': ['PIADINA'],
    'Riso / farro / cereali vari · invece della pasta': ['RICE', 'SPELT'],
}
OTHER_GROUPS = {'CEREALI E FONTI DI CARBOIDRATI': 'CARBS', 'COLAZIONE': 'BREAKFAST', 'SPUNTINI': 'SNACKS'}
# Same foods as "Pane e sostituti" but with the larger portion that replaces pasta.
INSTEAD_OF_PASTA = 'Pane (invece della pasta)'


def to_food(row):
    """Returns (group, category, name) for a row, or None if the row is not an alternative."""
    section, subsection, name = row['categoria'], row['sottocategoria'], row['alimento'].strip()
    if section == 'FONTI PROTEICHE':
        if subsection == 'Carne':
            return 'MEAT', MEAT_CATEGORY[name], name
        group = PROTEIN_GROUPS[subsection]
        return group, group, name
    if section in OTHER_GROUPS:
        if subsection == INSTEAD_OF_PASTA:
            name += ' · invece della pasta'
        return OTHER_GROUPS[section], None, name
    return None


def main():
    foods, seen = [], set()
    with SOURCE.open(encoding='utf-8') as source:
        for row in csv.DictReader(source):
            food = to_food(row)
            if food is None:
                continue
            group, category, name = food
            portion = row['porzione'].strip()
            # "Primi" repeats some "instead of pasta" foods with the same portion: keep one.
            key = (group, name.removesuffix(' · invece della pasta'), portion)
            if key in seen:
                continue
            seen.add(key)
            foods.append({'group': group, 'category': category, 'name': name, 'portion': portion,
                          'carbs': CARBS.get(name, []) if group == 'CARBS' else []})

    meat = Counter(f['category'] for f in foods if f['group'] == 'MEAT')
    assert meat == {category: len(names) for category, names in MEAT.items()}, meat
    assert {f['name'] for f in foods if f['carbs']} == set(CARBS), 'every carb mapping must match a food'
    TARGET.write_text('[\n' + ',\n'.join(json.dumps(f, ensure_ascii=False) for f in foods) + '\n]\n',
                      encoding='utf-8')
    print(dict(Counter(f['group'] for f in foods)))


if __name__ == '__main__':
    main()
