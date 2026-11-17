package dev.spoilershield

/** Reveal state belongs to a visible item and survives routine accessibility updates. */
class CoverSession {
 private val revealed = mutableSetOf<String>()
 fun isRevealed(key: String) = key in revealed
 fun toggle(key: String) { if (!revealed.add(key)) revealed.remove(key) }
 fun retain(keys: Set<String>) { revealed.retainAll(keys) }
 fun reset() { revealed.clear() }
}
