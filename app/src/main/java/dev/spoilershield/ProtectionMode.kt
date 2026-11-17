package dev.spoilershield

enum class ProtectionMode(val title: String, val explanation: String) {
 TOPIC("All GTA VI content", "Cover recognizable posts mentioning GTA VI, GTA 6 or Grand Theft Auto VI."),
 SPOILERS("Potential spoilers only", "Use your phrases plus expanded story, ending, leak and gameplay warnings. Ordinary GTA VI news stays visible."),
 APPS("Block apps completely", "Prevent access to selected apps until you turn protection off.");
 companion object {
  fun fromStored(value: String?) = entries.firstOrNull { it.name == value } ?: SPOILERS
 }
}
object ProtectionPolicy {
 fun matches(mode: ProtectionMode, text: String) = when (mode) {
  ProtectionMode.TOPIC -> TopicMatcher.matches(text)
  ProtectionMode.SPOILERS -> KeywordMatcher.matches(text)
  ProtectionMode.APPS -> false
 }
 fun blocksApp(enabled: Boolean, mode: ProtectionMode, selected: Set<String>, packageName: String) =
  enabled && mode == ProtectionMode.APPS && packageName in selected
}
object TopicMatcher {
 fun matches(text: String) = GameTitleMatcher.matches(text)
}
