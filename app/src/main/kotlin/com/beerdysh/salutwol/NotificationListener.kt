package com.beerdysh.salutwol

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.concurrent.Executors

/**
 * Слушает системные уведомления. Когда прилетает уведомление от Салюта
 * ("История сценариев" -> "Включи компьютер" / "Выключи компьютер"),
 * распознаёт текст и отправляет соответствующую команду.
 */
class NotificationListener : NotificationListenerService() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val prefs = Prefs(applicationContext)

        if (prefs.sourcePackage.isNotBlank() && sbn.packageName != prefs.sourcePackage) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val fullText = "$title $text".trim()

        if (fullText.isBlank()) return

        val lower = fullText.lowercase()
        val onPhrase = prefs.onPhrase.lowercase()
        val offPhrase = prefs.offPhrase.lowercase()

        when {
            onPhrase.isNotBlank() && lower.contains(onPhrase) -> {
                EventLog.add("Уведомление: \"$fullText\" -> команда ВКЛЮЧИТЬ")
                sendWakeAsync(prefs)
            }
            offPhrase.isNotBlank() && lower.contains(offPhrase) -> {
                EventLog.add("Уведомление: \"$fullText\" -> команда ВЫКЛЮЧИТЬ")
                sendShutdownAsync(prefs)
            }
        }
    }

    private fun sendWakeAsync(prefs: Prefs) {
        executor.execute {
            val result = WolSender.sendWake(prefs.macAddress, prefs.broadcastIp, prefs.wolPort)
            val msg = when (result) {
                is WolSender.Result.Success -> result.message
                is WolSender.Result.Failure -> result.message
            }
            EventLog.add(msg)
        }
    }

    private fun sendShutdownAsync(prefs: Prefs) {
        executor.execute {
            val result = WolSender.sendShutdown(prefs.shutdownUrl)
            val msg = when (result) {
                is WolSender.Result.Success -> result.message
                is WolSender.Result.Failure -> result.message
            }
            EventLog.add(msg)
        }
    }
}
