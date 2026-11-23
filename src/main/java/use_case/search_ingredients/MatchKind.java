package use_case.search_ingredients;

/**
 * How a suggestion matched what the user typed.
 */
public enum MatchKind {
    /** The query is the ingredient's name. */
    EXACT,
    /** The ingredient's name, or a word in it, starts with the query. */
    PREFIX,
    /** The query matched one of the ingredient's aliases (another name, or a similar item). */
    ALIAS,
    /** The query is a misspelling of the ingredient's name or an alias. */
    TYPO,
    /** Nothing matched closely; the ingredient shares a word with the query. */
    SIMILAR
}
