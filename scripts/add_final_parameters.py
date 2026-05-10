#!/usr/bin/env python3
"""
Adiciona `final` a parâmetros de métodos e construtores em ficheiros .java sob src/.

Evita:
  - linhas com class/interface/enum/record no mesmo início de declaração;
  - inicializadores de campo (há '=' na cabeça antes do '(' do declarador);
  - cabeçalhos record (linha contém `record` após o acesso).

Só processa declarações com modificador public/private/protected (não toca chamadas
como `new X(...)`, `throw`, `switch`, nem interfaces sem esses modificadores —
esses casos usam `final` em parâmetros aplicado manualmente ou em portas).
"""
from __future__ import annotations

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1] / "src"

ACCESS_RE = re.compile(
    r"^\s*(?:(?:@\w[\w.]*(?:\([^)]*\))?)\s+)*(?:public|private|protected)\b"
)



def end_of_annotation_list(s: str, start: int) -> int:
    i = start
    ln = len(s)
    while i < ln:
        while i < ln and s[i].isspace():
            i += 1
        if i >= ln or s[i] != "@":
            return i
        i += 1
        while i < ln and (s[i].isalnum() or s[i] in "_$."):
            i += 1
        while i < ln and s[i].isspace():
            i += 1
        if i < ln and s[i] == "(":
            depth = 1
            i += 1
            while i < ln and depth > 0:
                ch = s[i]
                if ch == "(":
                    depth += 1
                elif ch == ")":
                    depth -= 1
                elif ch == '"':
                    i += 1
                    while i < ln:
                        if s[i] == "\\":
                            i += 2
                            continue
                        if s[i] == '"':
                            break
                        i += 1
                    i += 1
                    continue
                i += 1


def contains_top_level_equals(head: str) -> bool:
    angle = 0
    i = 0
    in_string: str | None = None
    while i < len(head):
        c = head[i]
        if in_string:
            if c == "\\" and in_string == '"':
                i += 2
                continue
            if c == in_string:
                in_string = None
            i += 1
            continue
        if c in '"\'':
            in_string = c
            i += 1
            continue
        if c == "<":
            angle += 1
        elif c == ">" and angle > 0:
            angle -= 1
        elif c == "=" and angle == 0:
            return True
        i += 1
    return False


def find_declaration_open_paren(block: str) -> int:
    """Índice do '(' que abre a lista de parâmetros do método/construtor, ou -1."""
    if re.search(r"\b@interface\b", block):
        return -1
    if re.search(r"^\s*(?:public\s+)?record\b", block):
        return -1

    mo_acc = ACCESS_RE.search(block)
    if not mo_acc:
        return -1
    start_scan = mo_acc.end()
    head_prefix_len = mo_acc.end()

    angle = 0
    i = start_scan
    in_string: str | None = None
    while i < len(block):
        c = block[i]
        if in_string:
            if c == "\\" and in_string == '"':
                i += 2
                continue
            if c == in_string:
                in_string = None
            i += 1
            continue
        if c in '"\'':
            in_string = c
            i += 1
            continue
        if c == "<":
            angle += 1
        elif c == ">" and angle > 0:
            angle -= 1
        elif c == "(" and angle == 0:
            head = block[head_prefix_len:i]
            if contains_top_level_equals(head):
                return -1
            return i
        i += 1
    return -1


def ensure_final_on_parameter(param: str) -> str:
    p = param.strip()
    if not p:
        return param
    ap_end = end_of_annotation_list(p, 0)
    head = p[:ap_end].rstrip()
    rest = p[ap_end:].lstrip()
    if rest.startswith("final ") or rest.startswith("var "):
        return param
    insert = (head + " final ") if head else "final "
    aligned = insert + rest
    if param and param[0].isspace():
        return param[: len(param) - len(param.lstrip())] + aligned
    return aligned


def split_parameter_list(inner: str) -> list[tuple[int, int]]:
    segments: list[tuple[int, int]] = []
    start = 0
    par = angle = 0
    i = 0
    in_string: str | None = None

    while i < len(inner):
        c = inner[i]
        if in_string:
            if c == "\\" and in_string == '"':
                i += 2
                continue
            if c == in_string:
                in_string = None
            i += 1
            continue
        if c in '"\'':
            in_string = c
            i += 1
            continue

        if c == "(":
            par += 1
        elif c == ")":
            par -= 1
        elif par == 0:
            if c == "<":
                angle += 1
            elif c == ">":
                if angle > 0:
                    angle -= 1
                i += 1
                continue
            elif c == "," and angle == 0:
                segments.append((start, i))
                start = i + 1
                i += 1
                continue
        i += 1

    segments.append((start, len(inner)))
    return segments


def add_final_inside_params(param_block: str) -> str:
    spans = split_parameter_list(param_block)
    parts: list[str] = []
    last = 0
    for a, b in spans:
        parts.append(param_block[last:a])
        chunk = param_block[a:b]
        if chunk.strip():
            parts.append(ensure_final_on_parameter(chunk))
        else:
            parts.append(chunk)
        last = b
    parts.append(param_block[last:])
    return "".join(parts)


def match_paren(s: str, open_idx: int) -> int:
    assert s[open_idx] == "("
    depth = 0
    i = open_idx
    in_string: str | None = None
    while i < len(s):
        c = s[i]
        if in_string:
            if c == "\\" and in_string == '"':
                i += 2
                continue
            if c == in_string:
                in_string = None
            i += 1
            continue
        if c in '"\'':
            in_string = c
            i += 1
            continue
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def is_record_compact_ctor(block: str, inner: str) -> bool:
    inner = inner.strip()
    if not inner:
        return False
    first = inner.split(",", 1)[0]
    return "=" in first


def transform_declaration_block(block: str) -> str:
    oi = find_declaration_open_paren(block)
    if oi < 0:
        return block
    ci = match_paren(block, oi)
    if ci < 0:
        return block
    inner = block[oi + 1 : ci]
    if is_record_compact_ctor(block, inner):
        return block
    new_inner = add_final_inside_params(inner)
    if new_inner == inner:
        return block
    return block[: oi + 1] + new_inner + block[ci:]


def iter_blocks(lines: list[str]) -> list[str | tuple[int, int]]:
    i = 0
    n = len(lines)
    out: list[str | tuple[int, int]] = []
    while i < n:
        line = lines[i]
        stripped = line.lstrip()
        if stripped.startswith(("//", "*", "package ", "import ")):
            out.append(line)
            i += 1
            continue
        if not ACCESS_RE.search(line):
            out.append(line)
            i += 1
            continue
        if re.search(r"\b(?:class|interface|enum|record)\b", line):
            out.append(line)
            i += 1
            continue

        j = i
        block = lines[j]
        oi = find_declaration_open_paren(block)
        if oi < 0:
            out.append(line)
            i += 1
            continue
        ci = match_paren(block, oi)
        while ci < 0 and j + 1 < n:
            j += 1
            block += lines[j]
            oi = find_declaration_open_paren(block)
            ci = match_paren(block, oi) if oi >= 0 else -1
        if ci < 0:
            out.append(block)
            i = j + 1
            continue
        out.append((i, j))
        i = j + 1
    return out


def process_file(path: pathlib.Path) -> bool:
    raw = path.read_text(encoding="utf-8")
    lines = raw.splitlines(keepends=True)
    seq = iter_blocks(lines)
    out_chunks: list[str] = []
    changed = False
    for item in seq:
        if isinstance(item, str):
            out_chunks.append(item)
            continue
        a, b = item
        block = "".join(lines[a : b + 1])
        nb = transform_declaration_block(block)
        if nb != block:
            changed = True
        out_chunks.append(nb)
    new_text = "".join(out_chunks)
    if changed:
        path.write_text(new_text, encoding="utf-8")
    return changed


def main() -> int:
    files = sorted(ROOT.rglob("*.java"))
    n = 0
    for f in files:
        try:
            if process_file(f):
                n += 1
                print(f"updated: {f.relative_to(ROOT.parent)}")
        except Exception as e:
            print(f"ERROR {f}: {e}", file=sys.stderr)
            return 1
    print(f"Total files modified: {n}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
