package dev.spoilershield

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Telephony

object InstalledApps {
 data class App(val packageName: String, val label: String, val suggested: Boolean)
 private val social = setOf(
  "com.instagram.android", "com.instagram.lite", "com.google.android.youtube",
  "com.facebook.katana", "com.facebook.lite", "com.facebook.orca", "com.twitter.android",
  "com.reddit.frontpage", "com.snapchat.android", "com.zhiliaoapp.musically", "com.ss.android.ugc.trill",
  "com.pinterest", "com.discord", "org.telegram.messenger", "com.whatsapp", "com.whatsapp.w4b",
  "com.linkedin.android", "com.instagram.barcelona", "xyz.blueskyweb.app", "com.tumblr"
 )
 @Suppress("DEPRECATION")
 fun load(context: Context): List<App> {
  val pm = context.packageManager
  val home = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0).map { it.activityInfo.packageName }
  val dialers = pm.queryIntentActivities(Intent(Intent.ACTION_DIAL), 0).map { it.activityInfo.packageName }
  val excluded = home.toSet() + dialers + setOfNotNull(context.packageName, "com.android.settings", "com.android.systemui", "com.android.permissioncontroller", "com.google.android.permissioncontroller", Telephony.Sms.getDefaultSmsPackage(context))
  return pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
   .filter { it.activityInfo.packageName !in excluded }
   .filter { it.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.activityInfo.packageName in social }
   .map { App(it.activityInfo.packageName, it.loadLabel(pm).toString(), it.activityInfo.packageName in social) }
   .distinctBy { it.packageName }.sortedWith(compareByDescending<App> { it.suggested }.thenBy { it.label.lowercase() })
 }
}
