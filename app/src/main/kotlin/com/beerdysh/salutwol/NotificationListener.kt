package com.beerdysh.salutwol

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.util.concurrent.Executors

/**
 * Слушает системные уведомления. Когда прилетает уведомление от Салюта
 * ("История сценариев" -> "Включи компьютер" / "Выключи компьютер"),
 * распознаёт текст и отправляет соответствующую команду.
 *
 * Чтобы это заработало, пользователь должен один раз вручную выдать
 * приложению доступ: Настройки -> Уведомления -> Доступ к уведомлениям -> SalutWoL.
 */
class NotificationListener : NotificationListenerService() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("SalutWoL", "Listener CONNECTED — сервис успешно подключился к системе")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("SalutWoL", "Listener DISCONNECTED — сервис отключился")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val prefs = Prefs(applicationContext)

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val fullText = "$title $text".trim()

        // Логируем АБСОЛЮТНО ВСЕ уведомления, без фильтра, чтобы видеть реальные данные.
        Log.d("SalutWoL", "notification: package=${sbn.packageName} title=\"$title\" text=\"$text\"")

        // Если задан конкретный пакет-источник — фильтруем по нему.
        if (prefs.sourcePackage.isNotBlank() && sbn.packageName != prefs.sourcePackage) {
            Log.d("SalutWoL", "  -> пропущено: не совпадает с sourcePackage='${prefs.sourcePackage}'")
            return
        }

        if (fullText.isBlank()) {
            Log.d("SalutWoL", "  -> пропущено: пустой текст")
            return
        }

        val lower = fullText.lowercase()
        val onPhrase = prefs.onPhrase.lowercase()
        val offPhrase = prefs.offPhrase.lowercase()

        when {
            onPhrase.isNotBlank() && lower.contains(onPhrase) -> {
                Log.d("SalutWoL", "  -> СОВПАДЕНИЕ: ВКЛЮЧИТЬ")
                EventLog.add("Уведомление: \"$fullText\" -> команда ВКЛЮЧИТЬ")
                sendWakeAsync(prefs)
            }
            offPhrase.isNotBlank() && lower.contains(offPhrase) -> {
                Log.d("SalutWoL", "  -> СОВПАДЕНИЕ: ВЫКЛЮЧИТЬ")
                EventLog.add("Уведомление: \"$fullText\" -> команда ВЫКЛЮЧИТЬ")
                sendShutdownAsync(prefs)
            }
            else -> {
                Log.d("SalutWoL", "  -> не совпало ни с одной фразой (on='$onPhrase' off='$offPhrase')")
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
            Log.d("SalutWoL", "sendWake result: $msg")
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
            Log.d("SalutWoL", "sendShutdown result: $msg")
            EventLog.add(msg)
        }
    }
}
