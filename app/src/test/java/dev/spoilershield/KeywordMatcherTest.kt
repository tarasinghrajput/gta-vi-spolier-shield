package dev.spoilershield
import org.junit.Assert.*
import org.junit.Test
class KeywordMatcherTest {
 @Test fun allRequestedPhrases() { KeywordMatcher.phrases.forEach { assertTrue(it, KeywordMatcher.matches(it)); assertTrue(it, KeywordMatcher.matches(it.lowercase())) } }
 @Test fun formattingAndAliases() { listOf("GTA 6: FINAL MISSION", "grand theft auto vi plot twist", "#GTAVIEnding", "Jason—dies", "WHO\nDIES?", "GTA VI ending explained!").forEach { assertTrue(it, KeywordMatcher.matches(it)) } }
 @Test fun unrelated() { listOf("", "GTA VI trailer", "GTA 6 release date", "Jason interview", "Lucia interview", "who diesel", "secret endingsomething", "GTA VII final mission").forEach { assertFalse(it, KeywordMatcher.matches(it)) } }
 @Test fun standalonePhrasesAreIntentionallyBroad() { assertTrue(KeywordMatcher.matches("Another game's SECRET ENDING")) }
}
