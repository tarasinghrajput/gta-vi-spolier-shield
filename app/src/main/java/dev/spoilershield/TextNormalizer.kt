package dev.spoilershield

import java.text.Normalizer
import java.util.Locale

object TextNormalizer {
 private val formatting = Regex("\\p{Cf}")
 private val camelWords = Regex("([a-z])([A-Z])")
 private val acronymWords = Regex("([A-Z])([A-Z][a-z])")
 private val punctuation = Regex("[^\\p{L}\\p{N}]+")
 private val spaces = Regex("\\s+")
 fun normalize(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFKC)
  .replace(formatting, "")
  .replace(camelWords, "$1 $2")
  .replace(acronymWords, "$1 $2")
  .lowercase(Locale.ROOT)
  .replace(punctuation, " ")
  .trim().replace(spaces, " ")
 fun canonical(text: String): String = GameTitleMatcher.alias.replace(normalize(text), "gta vi ").trim().replace(spaces, " ")
 fun phrasePattern(phrase: String): Regex = Regex("(?<![\\p{L}\\p{N}])" + canonical(phrase).split(" ").joinToString("\\s*") { Regex.escape(it) } + "(?![\\p{L}\\p{N}])")
}
object GameTitleMatcher {
 // Known suffixes permit compact hashtags without treating GTAVII or Vice City as GTA VI.
 private const val suffix = "(?:endings?|spoilers?|story|leaks?|gameplay|trailers?|news|walkthrough|playthrough|final|secret|true|alternate|allendings|plot|shorts|updates?|release|videos?)"
 val alias = Regex("(?<![\\p{L}\\p{N}])(?:g\\s*t\\s*a|grand\\s*theft\\s*auto)\\s*(?:6|v\\s*i|six)(?:(?![\\p{L}\\p{N}])|(?=$suffix))")
 fun matches(text: String) = alias.containsMatchIn(TextNormalizer.normalize(text))
}
