import { ArboriumConfig, Grammar } from './types.js';
export declare const defaultConfig: Required<ArboriumConfig>;
/** Highlight source code */
export declare function highlight(language: string, source: string, configOverrides?: ArboriumConfig): Promise<string>;
/** Load a grammar for direct use */
export declare function loadGrammar(language: string, configOverrides?: ArboriumConfig): Promise<Grammar | null>;
/** Get current config, optionally merging with overrides */
export declare function getConfig(overrides?: Partial<ArboriumConfig>): Required<ArboriumConfig>;
/** Set/merge config */
export declare function setConfig(newConfig: Partial<ArboriumConfig>): void;
/** Check if a language is available */
export declare function isLanguageAvailable(language: string, configOverrides?: ArboriumConfig): Promise<boolean>;
/** Get list of available languages */
export declare function getAvailableLanguages(configOverrides?: ArboriumConfig): Promise<string[]>;
