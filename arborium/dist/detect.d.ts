/**
 * Simple language detection heuristics.
 * Not meant to be comprehensive - just catches common cases.
 */
/**
 * Detect the language of a code snippet.
 * Returns null if detection fails.
 */
export declare function detectLanguage(source: string): string | null;
/**
 * Extract language from class name.
 * Supports multiple patterns:
 * - "language-rust" -> "rust" (standard)
 * - "lang-rust" -> "rust" (common alternative)
 * - "rust" -> "rust" (docs.rs style, bare language name)
 */
export declare function extractLanguageFromClass(className: string): string | null;
/**
 * Normalize language identifier (handle aliases)
 */
export declare function normalizeLanguage(lang: string): string;
