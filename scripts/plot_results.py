"""Rebuild repository plots and the README results section from real Java timings."""
import csv
from pathlib import Path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
with (ROOT / "results/averages.csv").open(encoding="utf-8") as file:
    rows = list(csv.DictReader(file))

plt.rcParams.update({"figure.dpi": 160, "font.size": 10})

def draw(ax, names, scenario):
    for name in names:
        selected = [r for r in rows if r["algorithm"] == name and r["scenario"] == scenario]
        ax.plot([int(r["n"]) for r in selected], [float(r["average_ms"]) for r in selected],
                marker="o", label=name)
    ax.set_xlabel("Input length")
    ax.set_ylabel("Average time (ms)")
    ax.grid(alpha=0.25)
    ax.legend()

fig, ax = plt.subplots(figsize=(7, 4))
draw(ax, ["LCS", "Edit Distance"], "random")
ax.set_title("Dynamic programming: two strings of equal length")
fig.tight_layout()
fig.savefig(ROOT / "results/dp.png")
plt.close(fig)

fig, axes = plt.subplots(1, 2, figsize=(11, 4))
for ax, scenario in zip(axes, ["present", "absent"]):
    draw(ax, ["Naive", "KMP", "Rabin-Karp"], scenario)
    ax.set_title("Pattern " + scenario + " (length 20)")
fig.tight_layout()
fig.savefig(ROOT / "results/matching.png")
plt.close(fig)

fig, ax = plt.subplots(figsize=(7, 4))
draw(ax, ["Kasai"], "random")
ax.set_title("Kasai: suffix array construction excluded")
fig.tight_layout()
fig.savefig(ROOT / "results/kasai.png")
plt.close(fig)

lines = ["| Алгоритм | n | Случай | Среднее, мс | Сравнения подготовки | Сравнения поиска | Результат* |",
         "|---|---:|---|---:|---:|---:|---:|"]
for row in rows:
    matching = row["algorithm"] in {"Naive", "KMP", "Rabin-Karp"}
    pre = row["preprocessing_comparisons"] if matching else "—"
    search = row["search_comparisons"] if matching else "—"
    lines.append(f'| {row["algorithm"]} | {row["n"]} | {row["scenario"]} | {row["average_ms"]} | {pre} | {search} | {row["result"]} |')
report = ROOT / "README.md"
content = report.read_text(encoding="utf-8")
start, end = "<!-- RESULTS START -->", "<!-- RESULTS END -->"
before, rest = content.split(start)
_, after = rest.split(end)
report.write_text(before + start + "\n\n" + "\n".join(lines) + "\n\n" + end + after, encoding="utf-8")
print("Updated three PNG plots and README result table")
