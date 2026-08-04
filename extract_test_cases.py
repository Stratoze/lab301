"""
extract_test_cases.py — Inventories every unit test for SRS section 6.1.

Scans backend JUnit5 tests and frontend Vitest tests, extracts all test-case
names, then:
  1. Prints a Markdown table to the console.
  2. Writes test_cases_report.html  ->  open in a browser, Ctrl+A, copy,
     paste into Word (table formatting is preserved).

Usage:  python extract_test_cases.py
"""

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent
BACKEND_TEST_DIR = PROJECT_ROOT / "Lottery-App" / "backend" / "checker" / "src" / "test" / "java"
FRONTEND_SRC_DIR = PROJECT_ROOT / "Lottery-App" / "frontend" / "src"
OUTPUT_HTML = PROJECT_ROOT / "test_cases_report.html"

CATEGORY_ORDER = {
    "Controller (Mockito)": 0,
    "Service (Mockito)": 1,
    "Repository (H2 in-memory, @DataJpaTest)": 2,
    "Smoke (Spring Context)": 3,
}


def backend_category(path: Path) -> str:
    parts = path.parts
    if "controller" in parts:
        return "Controller (Mockito)"
    if "service" in parts:
        return "Service (Mockito)"
    if "repository" in parts:
        return "Repository (H2 in-memory, @DataJpaTest)"
    return "Smoke (Spring Context)"


def extract_backend_cases(file: Path) -> list[str]:
    """Capture the method name following each @Test / @ParameterizedTest."""
    cases: list[str] = []
    pending = False
    for line in file.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if re.match(r"@(Test|ParameterizedTest)\b", stripped):
            pending = True
            continue
        if pending:
            match = re.search(r"\bvoid\s+(\w+)\s*\(", stripped)
            if match:
                cases.append(match.group(1))
                pending = False
            elif not stripped.startswith("@"):
                pending = False  # signature never appeared -> reset
    return cases


def extract_frontend_cases(file: Path) -> list[str]:
    """Capture it('...') / test('...') titles."""
    text = file.read_text(encoding="utf-8")
    return re.findall(r"\b(?:it|test)\(\s*['\"`]([^'\"`]+)['\"`]", text)


def collect_backend_rows() -> list[tuple[str, str, str, str]]:
    rows = []
    for file in sorted(BACKEND_TEST_DIR.rglob("*Test*.java")):
        class_name = file.stem
        tested = class_name.removesuffix("Test") or class_name
        cases = extract_backend_cases(file)
        if not cases:
            continue
        rows.append((class_name, tested, ", ".join(cases), backend_category(file)))
    rows.sort(key=lambda r: (CATEGORY_ORDER.get(r[3], 9), r[0]))
    return rows


def collect_frontend_rows() -> list[tuple[str, str, str, str]]:
    rows = []
    for file in sorted(FRONTEND_SRC_DIR.rglob("*.test.tsx")):
        component = file.stem.replace(".test", "")
        cases = extract_frontend_cases(file)
        if not cases:
            continue
        rows.append(
            (file.name, component, ", ".join(cases), "Component (Vitest + Testing Library)")
        )
    return rows


def render_markdown(title: str, headers: list[str], rows: list[tuple[str, str, str, str]]) -> str:
    lines = [f"### {title}", "", "| " + " | ".join(["STT"] + headers) + " |",
             "|" + "---|" * (len(headers) + 1)]
    for i, (test_cls, target, cases, kind) in enumerate(rows, start=1):
        lines.append(f"| {i} | {test_cls} | {target} | {cases} | {kind} |")
    return "\n".join(lines)


def render_html(backend_rows, frontend_rows) -> str:
    def table(rows: list[tuple[str, str, str, str]]) -> str:
        body = []
        for i, (test_cls, target, cases, kind) in enumerate(rows, start=1):
            body.append(
                f"<tr><td>{i}</td><td>{test_cls}</td><td>{target}</td>"
                f"<td>{cases}</td><td>{kind}</td></tr>"
            )
        header = ("<tr><th>STT</th><th>Lớp Test</th><th>Lớp được kiểm thử</th>"
                  "<th>Các hàm/test case chính</th><th>Loại</th></tr>")
        return (f'<table border="1" cellpadding="6" cellspacing="0">{header}'
                + "".join(body) + "</table>")

    return f"""<!DOCTYPE html>
<html><head><meta charset="utf-8"><title>SRS 6.1 Test Inventory</title></head>
<body style="font-family: Calibri, Arial, sans-serif;">
<h2>6.1.1 Backend (Java – Spring Boot)</h2>
{table(backend_rows)}
<h2>6.1.2 Frontend (React + TypeScript – Vitest)</h2>
{table(frontend_rows)}
</body></html>
"""


def main() -> None:
    backend_rows = collect_backend_rows()
    frontend_rows = collect_frontend_rows()

    print(render_markdown("6.1.1 Backend", ["Lớp Test", "Lớp được kiểm thử",
                                            "Các hàm/test case chính", "Loại"], backend_rows))
    print()
    print(render_markdown("6.1.2 Frontend", ["File Test", "Component/Page",
                                             "Các test case chính", "Loại"], frontend_rows))

    OUTPUT_HTML.write_text(render_html(backend_rows, frontend_rows), encoding="utf-8")
    print(f"\nHTML written to: {OUTPUT_HTML}")
    print("Open it in a browser -> Ctrl+A -> Copy -> Paste into Word.")


if __name__ == "__main__":
    main()
