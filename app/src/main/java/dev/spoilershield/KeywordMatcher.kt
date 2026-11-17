package dev.spoilershield

object KeywordMatcher {
 val phrases = listOf(
  "GTA VI ENDING", "GTA VI ENDINGS", "GTA VI STORY SPOILERS", "GTA VI PLOT TWIST",
  "GTA VI FINAL MISSION", "GTA VI LAST MISSION", "GTA VI SECRET ENDING", "GTA VI TRUE ENDING",
  "GTA VI ALTERNATE ENDING", "GTA VI ENDING EXPLAINED", "GTA VI ALL ENDINGS", "GTA VI STORY LEAK",
  "GTA VI FINAL CUTSCENE", "GTA VI DEATH SCENE", "GTA VI BETRAYAL", "JASON DIES", "LUCIA DIES",
  "JASON DEATH", "LUCIA DEATH", "JASON KILLED", "LUCIA KILLED", "JASON ENDING", "LUCIA ENDING",
  "JASON BETRAYAL", "LUCIA BETRAYAL", "JASON AND LUCIA ENDING", "FINAL CUTSCENE", "SECRET ENDING",
  "TRUE ENDING", "ENDING LEAKED", "ENDING EXPLAINED", "DEATH SCENE", "WHO DIES", "ALL ENDINGS"
 )
 private val requestedPattern = Regex(phrases.joinToString("|") { TextNormalizer.phrasePattern(it).pattern })
 private val riskPattern = Regex(SpoilerRules.categories.values.flatten().distinct().joinToString("|") { TextNormalizer.phrasePattern(it).pattern })
 private val characterContext = Regex("(?<![\\p{L}\\p{N}])(?:jason|lucia)(?![\\p{L}\\p{N}])")
 fun matches(text: String): Boolean {
  val normalized = TextNormalizer.canonical(text)
  if (requestedPattern.containsMatchIn(normalized)) return true
  val related = GameTitleMatcher.matches(text) || characterContext.containsMatchIn(normalized)
  return related && riskPattern.containsMatchIn(normalized)
 }
}
