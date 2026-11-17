package dev.spoilershield

/** Generic warning language, not assertions about the game's actual plot. */
object SpoilerRules {
 val categories = linkedMapOf(
  "Endings and finale" to listOf("ending", "endings", "finale", "epilogue", "endgame", "end game", "post credits", "post credit", "after credits", "after credit", "mid credits", "ending explained", "ending reaction", "alternate ending", "alternative ending", "good ending", "bad ending", "secret ending", "true ending", "100 percent ending"),
  "Story and reveals" to listOf("spoiler", "spoilers", "spoiling", "story explained", "story breakdown", "story summary", "story recap", "full story", "complete story", "entire story", "story reveal", "story mode", "plot", "plot twist", "big reveal", "identity revealed", "true identity", "hidden identity", "villain", "traitor", "betrayal", "betrays", "betrayed", "betray"),
  "Character outcomes" to listOf("dies", "died", "death", "dead", "killed", "kills", "murder", "murdered", "funeral", "sacrifice", "sacrifices", "survives", "survived", "fate", "who dies", "kill or save", "save or kill"),
  "Missions and footage" to listOf("mission", "missions", "final mission", "last mission", "final boss", "boss fight", "cutscene", "cutscenes", "cut scene", "cut scenes", "cinematic", "cinematics", "walkthrough", "walk through", "playthrough", "play through", "full game", "complete game", "entire game", "game movie", "full campaign", "complete campaign", "gameplay", "game play", "prologue", "chapter", "chapters", "lets play", "let s play", "no commentary"),
  "Leaks and early access footage" to listOf("leak", "leaks", "leaked", "leaking", "datamine", "datamined", "data mine", "data mined", "unreleased footage", "early footage", "early gameplay", "story footage"),
  "Romanized Hindi warning cues" to listOf("ki maut", "ka ant", "ka end", "mar gaya", "mar gayi", "mar gya", "mar gyi", "mar jayega", "mar jayegi", "dhokha", "asli villain", "aakhri mission", "akhri mission", "kahani", "poori story")
 )
}
