package dev.spoilershield

/** Frequent content events never postpone a queued scan indefinitely. */
class ScanTiming {
 private var lastScan = -1000L
 private var scrollUntil = 0L
 fun scrolled(now: Long) { scrollUntil = now + 140 }
 fun started(now: Long) { lastScan = now }
 fun waitMillis(now: Long, fullApp: Boolean = false): Long =
  if (fullApp) 0 else maxOf(0, lastScan + 100 - now, scrollUntil - now)
 fun reset() { lastScan = -1000; scrollUntil = 0 }
}
