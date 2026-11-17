package dev.spoilershield
import org.junit.Assert.*
import org.junit.Test
class ExpandedMatcherTest {
 private val aliases = listOf("GTA VI", "GTA 6", "GTA6", "GTAVI", "G.T.A. 6", "G T A V I", "GTA Six", "Grand Theft Auto VI", "GrandTheftAuto6", "Grand Theft Auto Six", "ＧＴＡ ６", "GTA Ⅵ", "GTA\u200B6", "GTA-6")
 @Test fun everyAliasMatchesTopic() { aliases.forEach { assertTrue(it, TopicMatcher.matches("$it will be my last game")) } }
 @Test fun everyAliasMatchesSpoilerCues() {
  aliases.forEach { alias ->
   SpoilerRules.categories.values.flatten().forEach { cue -> assertTrue("$alias $cue", KeywordMatcher.matches("$alias $cue")) }
  }
 }
 @Test fun compactHashtags() {
  listOf("#GTA6Ending", "#GTAVIENDING", "#GTA6Spoilers", "#GrandTheftAuto6Gameplay", "#GTAVISecretEnding").forEach {
   assertTrue(it, TopicMatcher.matches(it)); assertTrue(it, KeywordMatcher.matches(it))
  }
 }
 @Test fun benignTitlesRemainVisibleInSpoilerMode() {
  listOf("GTA 6 will be my last game", "GTA6 release date", "G.T.A. 6 price", "Grand Theft Auto VI hardware requirements", "GTA VI official trailer").forEach {
   assertTrue(it, TopicMatcher.matches(it)); assertFalse(it, KeywordMatcher.matches(it))
  }
 }
 @Test fun relatedGameBoundary() {
  listOf("GTA V", "GTA VII", "GTAVII", "GTA 60", "GTA Vice City", "notgta6", "Grand Theft Auto IV").forEach { assertFalse(it, TopicMatcher.matches(it)) }
 }
 @Test fun contextRequiredForAddedCues() {
  listOf("Minecraft gameplay", "movie plot", "phone leaks", "ordinary walkthrough", "chapter one").forEach { assertFalse(it, KeywordMatcher.matches(it)) }
  assertTrue(KeywordMatcher.matches("Lucia betrayed")); assertTrue(KeywordMatcher.matches("GTA 6 ki kahani"))
 }
 @Test fun originalPhrasesStayEnabled() { KeywordMatcher.phrases.forEach { assertTrue(it, KeywordMatcher.matches(it)) } }
}
