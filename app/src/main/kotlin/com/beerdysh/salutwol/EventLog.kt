package com.beerdysh.salutwol

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Простой журнал событий для отображения в UI (что и когда сработало).
 * Хранится только в памяти процесса — этого достаточно для отладки.
 */
object EventLog {

    data class Entry(val timestamp: Long, val message: String)

    private val entries = mutableListOf<Entry>()
    private val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    @Synchronized
    fun add(message: String) {
        entries.add(0, Entry(System.currentTimeMillis(), message))
        while (entries.size > 200) {
            entries.removeAt(entries.size - 1)
        }
    }

    @Synchronized
    fun asText(): String {
        if (entries.isEmpty()) return "Пока пусто"
        return entries.joinToString("\n") { e ->
            "${format.format(Date(e.timestamp))}  ${e.message}"
        }
    }

    @Synchronized
    fun clear() = entries.clear()
}
