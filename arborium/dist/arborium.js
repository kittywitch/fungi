const L = [
  [/^#!.*\bpython[23]?\b/, "python"],
  [/^#!.*\bnode\b/, "javascript"],
  [/^#!.*\bdeno\b/, "typescript"],
  [/^#!.*\bbun\b/, "typescript"],
  [/^#!.*\bruby\b/, "ruby"],
  [/^#!.*\bperl\b/, "perl"],
  [/^#!.*\bphp\b/, "php"],
  [/^#!.*\bbash\b/, "bash"],
  [/^#!.*\bzsh\b/, "zsh"],
  [/^#!.*\bsh\b/, "bash"],
  [/^#!.*\blua\b/, "lua"],
  [/^#!.*\bawk\b/, "awk"]
], $ = [
  // Rust - distinctive keywords
  [/\b(fn|impl|trait|pub\s+fn|let\s+mut|&mut|->)\b/, "rust"],
  // Go - distinctive keywords
  [/\b(func|package\s+\w+|import\s+\(|go\s+func|chan\s+\w+)\b/, "go"],
  // Python - distinctive patterns
  [/\b(def\s+\w+\s*\(|import\s+\w+|from\s+\w+\s+import|class\s+\w+:)\b/, "python"],
  // TypeScript - distinctive type annotations
  [/:\s*(string|number|boolean|void)\b|\binterface\s+\w+\s*\{/, "typescript"],
  // JavaScript - distinctive patterns (after TS check)
  [/\b(const|let|var)\s+\w+\s*=|function\s+\w+\s*\(|=>\s*\{/, "javascript"],
  // Ruby - distinctive keywords
  [/\b(def\s+\w+|end\b|do\s*\|.*\||puts\s+|require\s+['"])\b/, "ruby"],
  // Java - distinctive patterns
  [/\b(public\s+class|private\s+\w+|System\.out\.println)\b/, "java"],
  // C++ - distinctive patterns
  [/\b(#include\s*<|std::|template\s*<|nullptr|cout\s*<<)\b/, "cpp"],
  // C - distinctive patterns (after C++ check)
  [/\b(#include\s*[<"]|printf\s*\(|int\s+main\s*\(|void\s+\w+\s*\()\b/, "c"],
  // C# - distinctive patterns
  [/\b(namespace\s+\w+|using\s+System|public\s+static\s+void)\b/, "c-sharp"],
  // PHP - distinctive patterns
  [/<\?php|\$\w+\s*=/, "php"],
  // Swift - distinctive patterns
  [/\b(func\s+\w+|var\s+\w+:\s*\w+|let\s+\w+:\s*\w+|@objc)\b/, "swift"],
  // Kotlin - distinctive patterns
  [/\b(fun\s+\w+|val\s+\w+|var\s+\w+:|data\s+class)\b/, "kotlin"],
  // Scala - distinctive patterns
  [/\b(def\s+\w+|val\s+\w+|var\s+\w+|object\s+\w+|case\s+class)\b/, "scala"],
  // Haskell - distinctive patterns
  [/\b(module\s+\w+|import\s+qualified|data\s+\w+\s*=|::\s*\w+\s*->)\b/, "haskell"],
  // Elixir - distinctive patterns
  [/\b(defmodule\s+\w+|def\s+\w+|defp\s+\w+|\|>)\b/, "elixir"],
  // Lua - distinctive patterns
  [/\b(local\s+\w+\s*=|function\s+\w+\.\w+|require\s*\()\b/, "lua"],
  // SQL - distinctive patterns
  [/\b(SELECT\s+.*\s+FROM|INSERT\s+INTO|CREATE\s+TABLE|ALTER\s+TABLE)\b/i, "sql"],
  // Shell/Bash - distinctive patterns
  [/\b(if\s+\[\s*|then\b|fi\b|echo\s+["']|export\s+\w+=)\b/, "bash"],
  // YAML - distinctive patterns
  [/^\s*[\w-]+:\s*[\w\-"'[{]|^---\s*$/, "yaml"],
  // JSON - distinctive patterns
  [/^\s*\{[\s\S]*"[\w-]+":\s*/, "json"],
  // TOML - distinctive patterns
  [/^\s*\[[\w.-]+\]\s*$|^\s*\w+\s*=\s*["'\d\[]/, "toml"],
  // HTML - distinctive patterns
  [/<(!DOCTYPE|html|head|body|div|span|p|a\s)/i, "html"],
  // CSS - distinctive patterns
  [/^\s*[\w.#@][\w\s,#.:>+~-]*\{[^}]*\}|@media\s|@import\s/, "css"],
  // Markdown - distinctive patterns
  [/^#{1,6}\s+\w|^\s*[-*+]\s+\w|^\s*\d+\.\s+\w|```\w*\n/, "markdown"],
  // XML - distinctive patterns
  [/<\?xml|<[\w:-]+\s+xmlns/, "xml"],
  // Dockerfile
  [/^FROM\s+\w+|^RUN\s+|^COPY\s+|^ENTRYPOINT\s+/m, "dockerfile"],
  // Nginx config
  [/\b(server\s*\{|location\s+[\/~]|proxy_pass\s+)\b/, "nginx"],
  // Zig
  [/\b(pub\s+fn|const\s+\w+\s*=|@import\(|comptime)\b/, "zig"]
];
function M(e) {
  const t = e.split(`
`)[0];
  for (const [a, r] of L)
    if (a.test(t))
      return r;
  for (const [a, r] of $)
    if (a.test(e))
      return r;
  return null;
}
function R(e) {
  const t = e.match(/\blanguage-(\w+)\b/);
  if (t) return t[1];
  const a = e.match(/\blang-(\w+)\b/);
  if (a) return a[1];
  const r = /* @__PURE__ */ new Set([
    "rust",
    "javascript",
    "typescript",
    "python",
    "ruby",
    "go",
    "java",
    "c",
    "cpp",
    "csharp",
    "php",
    "swift",
    "kotlin",
    "scala",
    "haskell",
    "elixir",
    "lua",
    "sql",
    "bash",
    "shell",
    "yaml",
    "json",
    "toml",
    "html",
    "css",
    "xml",
    "markdown",
    "dockerfile",
    "nginx",
    "zig",
    "text",
    "plaintext",
    "console",
    "sh"
  ]);
  for (const n of e.split(/\s+/))
    if (r.has(n.toLowerCase()))
      return n.toLowerCase();
  return null;
}
function H(e) {
  const t = {
    js: "javascript",
    ts: "typescript",
    py: "python",
    rb: "ruby",
    rs: "rust",
    sh: "bash",
    shell: "bash",
    yml: "yaml",
    cs: "c-sharp",
    csharp: "c-sharp",
    "c++": "cpp",
    "c#": "c-sharp",
    "f#": "fsharp",
    dockerfile: "dockerfile",
    docker: "dockerfile",
    makefile: "make",
    plaintext: "text",
    plain: "text",
    txt: "text"
  }, a = e.toLowerCase();
  return t[a] || a;
}
const _ = "2.12.4", U = [
  "ada",
  "agda",
  "asciidoc",
  "asm",
  "awk",
  "bash",
  "batch",
  "c",
  "c-sharp",
  "caddy",
  "capnp",
  "cedar",
  "cedarschema",
  "clojure",
  "cmake",
  "cobol",
  "commonlisp",
  "cpp",
  "css",
  "d",
  "dart",
  "devicetree",
  "diff",
  "dockerfile",
  "dot",
  "elisp",
  "elixir",
  "elm",
  "erlang",
  "fish",
  "fsharp",
  "gleam",
  "glsl",
  "go",
  "graphql",
  "groovy",
  "haskell",
  "hcl",
  "hlsl",
  "html",
  "idris",
  "ini",
  "java",
  "javascript",
  "jinja2",
  "jq",
  "json",
  "julia",
  "kotlin",
  "lean",
  "lua",
  "markdown",
  "matlab",
  "meson",
  "nginx",
  "ninja",
  "nix",
  "objc",
  "ocaml",
  "perl",
  "php",
  "postscript",
  "powershell",
  "prolog",
  "python",
  "query",
  "r",
  "rescript",
  "ron",
  "ruby",
  "rust",
  "scala",
  "scheme",
  "scss",
  "solidity",
  "sparql",
  "sql",
  "ssh-config",
  "starlark",
  "styx",
  "svelte",
  "swift",
  "textproto",
  "thrift",
  "tlaplus",
  "toml",
  "tsx",
  "typescript",
  "typst",
  "uiua",
  "vb",
  "verilog",
  "vhdl",
  "vim",
  "vue",
  "wit",
  "x86asm",
  "xml",
  "yaml",
  "yuri",
  "zig",
  "zsh"
], F = [
  {
    name: "attribute",
    tag: "at"
  },
  {
    name: "constant",
    tag: "co"
  },
  {
    name: "constant.builtin",
    tag: "cb",
    parentTag: "constant"
  },
  {
    name: "constructor",
    tag: "cr"
  },
  {
    name: "function.builtin",
    tag: "fb",
    parentTag: "function"
  },
  {
    name: "function",
    tag: "f"
  },
  {
    name: "function.method",
    tag: "fm",
    parentTag: "function"
  },
  {
    name: "keyword",
    tag: "k"
  },
  {
    name: "keyword.conditional",
    tag: "kc",
    parentTag: "keyword"
  },
  {
    name: "keyword.coroutine",
    tag: "ko",
    parentTag: "keyword"
  },
  {
    name: "keyword.debug",
    tag: "kd",
    parentTag: "keyword"
  },
  {
    name: "keyword.exception",
    tag: "ke",
    parentTag: "keyword"
  },
  {
    name: "keyword.function",
    tag: "kf",
    parentTag: "keyword"
  },
  {
    name: "keyword.import",
    tag: "ki",
    parentTag: "keyword"
  },
  {
    name: "keyword.operator",
    tag: "kp",
    parentTag: "keyword"
  },
  {
    name: "keyword.repeat",
    tag: "kr",
    parentTag: "keyword"
  },
  {
    name: "keyword.return",
    tag: "kt",
    parentTag: "keyword"
  },
  {
    name: "keyword.type",
    tag: "ky",
    parentTag: "keyword"
  },
  {
    name: "operator",
    tag: "o"
  },
  {
    name: "property",
    tag: "pr"
  },
  {
    name: "punctuation",
    tag: "p"
  },
  {
    name: "punctuation.bracket",
    tag: "pb",
    parentTag: "punctuation"
  },
  {
    name: "punctuation.delimiter",
    tag: "pd",
    parentTag: "punctuation"
  },
  {
    name: "punctuation.special",
    tag: "ps",
    parentTag: "punctuation"
  },
  {
    name: "string",
    tag: "s"
  },
  {
    name: "string.special",
    tag: "ss",
    parentTag: "string"
  },
  {
    name: "tag",
    tag: "tg"
  },
  {
    name: "tag.delimiter",
    tag: "td",
    parentTag: "tag"
  },
  {
    name: "tag.error",
    tag: "te",
    parentTag: "tag"
  },
  {
    name: "type",
    tag: "t"
  },
  {
    name: "type.builtin",
    tag: "tb",
    parentTag: "type"
  },
  {
    name: "type.qualifier",
    tag: "tq",
    parentTag: "type"
  },
  {
    name: "variable",
    tag: "v"
  },
  {
    name: "variable.builtin",
    tag: "vb",
    parentTag: "variable"
  },
  {
    name: "variable.parameter",
    tag: "vp",
    parentTag: "variable"
  },
  {
    name: "comment",
    tag: "c"
  },
  {
    name: "comment.documentation",
    tag: "cd",
    parentTag: "comment"
  },
  {
    name: "macro",
    tag: "m"
  },
  {
    name: "label",
    tag: "l"
  },
  {
    name: "diff.addition",
    tag: "da"
  },
  {
    name: "diff.deletion",
    tag: "dd"
  },
  {
    name: "number",
    tag: "n"
  },
  {
    name: "text.literal",
    tag: "tl"
  },
  {
    name: "text.emphasis",
    tag: "em"
  },
  {
    name: "text.strong",
    tag: "st"
  },
  {
    name: "text.uri",
    tag: "tu"
  },
  {
    name: "text.reference",
    tag: "tr"
  },
  {
    name: "string.escape",
    tag: "se",
    parentTag: "string"
  },
  {
    name: "text.title",
    tag: "tt"
  },
  {
    name: "text.strikethrough",
    tag: "tx"
  },
  {
    name: "spell",
    tag: "sp"
  },
  {
    name: "embedded",
    tag: "eb"
  },
  {
    name: "error",
    tag: "er"
  },
  {
    name: "namespace",
    tag: "ns"
  },
  {
    name: "include",
    tag: "in",
    parentTag: "keyword"
  },
  {
    name: "storageclass",
    tag: "sc",
    parentTag: "keyword"
  },
  {
    name: "repeat",
    tag: "rp",
    parentTag: "keyword"
  },
  {
    name: "conditional",
    tag: "cn",
    parentTag: "keyword"
  },
  {
    name: "exception",
    tag: "ex",
    parentTag: "keyword"
  },
  {
    name: "preproc",
    tag: "pp",
    parentTag: "keyword"
  },
  {
    name: "none",
    tag: ""
  },
  {
    name: "character",
    tag: "ch",
    parentTag: "string"
  },
  {
    name: "character.special",
    tag: "cs",
    parentTag: "string"
  },
  {
    name: "variable.member",
    tag: "vm",
    parentTag: "variable"
  },
  {
    name: "function.definition",
    tag: "fd",
    parentTag: "function"
  },
  {
    name: "type.definition",
    tag: "tf",
    parentTag: "type"
  },
  {
    name: "function.call",
    tag: "fc",
    parentTag: "function"
  },
  {
    name: "keyword.modifier",
    tag: "km",
    parentTag: "keyword"
  },
  {
    name: "keyword.directive",
    tag: "dr",
    parentTag: "keyword"
  },
  {
    name: "string.regexp",
    tag: "rx",
    parentTag: "string"
  },
  {
    name: "nospell",
    tag: ""
  },
  {
    name: "float",
    tag: "n"
  },
  {
    name: "boolean",
    tag: "cb"
  }
];
function E(e) {
  return e.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
const T = {
  manual: !1,
  theme: "one-dark",
  selector: "pre code",
  cdn: "jsdelivr",
  version: _,
  // Precise version from manifest
  pluginsUrl: "",
  // Empty means use bundled manifest
  hostUrl: "",
  // Empty means use CDN based on version
  logger: console,
  resolveJs: ({ baseUrl: e, path: t }) => import(
    /* @vite-ignore */
    `${e}/${t}`
  ),
  resolveWasm: ({ baseUrl: e, path: t }) => fetch(`${e}/${t}`)
};
let g = null, p = null, u = { ...T };
const y = /* @__PURE__ */ new Map(), h = /* @__PURE__ */ new Map(), d = new Set(U);
let l = null, m = null;
async function k(e) {
  if (e.pluginsUrl)
    return m || (m = (async () => {
      e.logger.debug(`[arborium] Loading local plugins manifest from: ${e.pluginsUrl}`);
      const t = await fetch(e.pluginsUrl);
      if (!t.ok)
        throw new Error(`Failed to load plugins.json: ${t.status}`);
      l = await t.json(), e.logger.debug(`[arborium] Loaded local manifest with ${l?.entries.length} entries`);
    })(), m);
}
function P(e, t) {
  if (l) {
    const s = l.entries.find((o) => o.language === e);
    if (s)
      return s.local_js.substring(0, s.local_js.lastIndexOf("/"));
  }
  const a = t.cdn, r = t.version;
  let n;
  return a === "jsdelivr" ? n = "https://cdn.jsdelivr.net/npm" : a === "unpkg" ? n = "https://unpkg.com" : n = a, `${n}/@arborium/${e}@${r}`;
}
async function j(e, t) {
  const a = y.get(e);
  if (a)
    return t.logger.debug(`[arborium] Grammar '${e}' found in cache`), a;
  const r = h.get(e);
  if (r)
    return t.logger.debug(`[arborium] Grammar '${e}' already loading, waiting...`), r;
  const n = q(e, t);
  h.set(e, n);
  try {
    return await n;
  } finally {
    h.delete(e);
  }
}
async function q(e, t) {
  if (await k(t), !d.has(e) && !l?.entries.some((a) => a.language === e))
    return t.logger.debug(`[arborium] Grammar '${e}' not available`), null;
  try {
    const a = P(e, t), r = t.resolveJs === T.resolveJs ? ` from ${a}/grammar.js` : "";
    t.logger.debug(`[arborium] Loading grammar '${e}'${r}`);
    const n = await t.resolveJs({
      language: e,
      baseUrl: a,
      path: "grammar.js"
    }), s = await t.resolveWasm({ language: e, baseUrl: a, path: "grammar_bg.wasm" });
    await n.default({ module_or_path: s });
    const o = n.language_id();
    o !== e && t.logger.warn(`[arborium] Language ID mismatch: expected '${e}', got '${o}'`);
    const x = n.injection_languages(), v = {
      languageId: e,
      injectionLanguages: x,
      module: n,
      // UTF-8 parsing for Rust host
      parseUtf8: (f) => {
        const c = n.create_session();
        try {
          n.set_text(c, f);
          const i = n.parse(c);
          return {
            spans: i.spans || [],
            injections: i.injections || []
          };
        } catch (i) {
          return t.logger.error("[arborium] Parse error:", i), { spans: [], injections: [] };
        } finally {
          n.free_session(c);
        }
      },
      // UTF-16 parsing for JavaScript public API
      parseUtf16: (f) => {
        const c = n.create_session();
        try {
          n.set_text(c, f);
          const i = n.parse_utf16(c);
          return {
            spans: i.spans || [],
            injections: i.injections || []
          };
        } catch (i) {
          return t.logger.error("[arborium] Parse error:", i), { spans: [], injections: [] };
        } finally {
          n.free_session(c);
        }
      }
    };
    return y.set(e, v), t.logger.debug(`[arborium] Grammar '${e}' loaded successfully`), v;
  } catch (a) {
    return t.logger.error(`[arborium] Failed to load grammar '${e}':`, a), null;
  }
}
const w = /* @__PURE__ */ new Map();
let C = 1;
function I(e) {
  window.arboriumHost = {
    /** Check if a language is available (sync) */
    isLanguageAvailable(t) {
      return d.has(t) || y.has(t);
    },
    /** Load a grammar and return a handle (async) */
    async loadGrammar(t) {
      const a = await j(t, e);
      if (!a) return 0;
      for (const [n, s] of w)
        if (s === a) return n;
      const r = C++;
      return w.set(r, a), r;
    },
    /** Parse text using a grammar handle (sync) - returns UTF-8 offsets for Rust host */
    parse(t, a) {
      const r = w.get(t);
      return r ? r.parseUtf8(a) : { spans: [], injections: [] };
    }
  };
}
function S(e) {
  if (e.hostUrl)
    return e.hostUrl;
  const t = e.cdn, a = e.version;
  let r;
  t === "jsdelivr" ? r = "https://cdn.jsdelivr.net/npm" : t === "unpkg" ? r = "https://unpkg.com" : r = t;
  const n = a === "latest" ? "" : `@${a}`;
  return `${r}/@arborium/arborium${n}/dist`;
}
async function A(e) {
  return g || p || (p = (async () => {
    I(e);
    const t = S(e), a = `${t}/arborium_host.js`, r = `${t}/arborium_host_bg.wasm`;
    e.logger.debug(`[arborium] Loading host from ${a}`);
    try {
      const n = await import(
        /* @vite-ignore */
        a
      );
      return await n.default(r), g = {
        highlight: n.highlight,
        isLanguageAvailable: n.isLanguageAvailable
      }, e.logger.debug("[arborium] Host loaded successfully"), g;
    } catch (n) {
      return e.logger.error("[arborium] Failed to load host:", n), null;
    }
  })(), p);
}
async function G(e, t, a) {
  const r = b(a), n = await A(r);
  if (n)
    try {
      return n.highlight(e, t);
    } catch (s) {
      r.logger.error("[arborium] Host highlight failed:", s);
    }
  return E(t);
}
async function z(e, t) {
  const a = b(t), r = await j(e, a);
  if (!r) return null;
  const { module: n } = r;
  return {
    languageId: () => r.languageId,
    injectionLanguages: () => r.injectionLanguages,
    highlight: async (s) => G(e, s, t),
    // Public API returns UTF-16 offsets for JavaScript compatibility
    parse: (s) => r.parseUtf16(s),
    createSession: () => {
      const s = n.create_session();
      return {
        setText: (o) => n.set_text(s, o),
        // Session.parse() returns UTF-16 offsets for JavaScript compatibility
        parse: () => {
          try {
            const o = n.parse_utf16(s);
            return {
              spans: o.spans || [],
              injections: o.injections || []
            };
          } catch (o) {
            return a.logger.error("[arborium] Session parse error:", o), { spans: [], injections: [] };
          }
        },
        cancel: () => n.cancel(s),
        free: () => n.free_session(s)
      };
    },
    dispose: () => {
    }
  };
}
function b(e) {
  return e ? { ...u, ...e } : { ...u };
}
function N(e) {
  u = { ...u, ...e };
}
async function O(e, t) {
  const a = b(t);
  return await k(a), d.has(e) || (l?.entries.some((r) => r.language === e) ?? !1);
}
async function B(e) {
  const t = b(e);
  return await k(t), l ? l.entries.map((a) => a.language) : Array.from(d);
}
export {
  U as availableLanguages,
  M as detectLanguage,
  R as extractLanguageFromClass,
  B as getAvailableLanguages,
  b as getConfig,
  G as highlight,
  F as highlights,
  O as isLanguageAvailable,
  z as loadGrammar,
  H as normalizeLanguage,
  _ as pluginVersion,
  N as setConfig
};
//# sourceMappingURL=arborium.js.map
