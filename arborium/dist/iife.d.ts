import { ArboriumConfig } from './types.js';
/** Highlight all code blocks on the page */
export declare function highlightAll(configOverrides?: Partial<ArboriumConfig>): Promise<void>;
/** Highlight a specific element */
export declare function highlightElement(element: HTMLElement, language?: string, configOverrides?: Partial<ArboriumConfig>): Promise<void>;
