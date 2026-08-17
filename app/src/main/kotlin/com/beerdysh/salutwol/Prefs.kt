package com.beerdysh.salutwol

import android.content.Context
import android.content.SharedPreferences

/**
 * Хранилище настроек: MAC-адрес ПК, broadcast IP для WoL,
 * адрес и порт локального сервера для выключения,
 * а также имя пакета приложения-источника уведомлений (Салют).
 */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.getSharedPreferences("salutwol_prefs", Context.MODE_PRIVATE)

    var macAddress: String
        get() = sp.getString("mac", "") ?: ""
        set(value) = sp.edit().putString("mac", value).apply()

    var broadcastIp: String
        get() = sp.getString("broadcast_ip", "255.255.255.255") ?: "255.255.255.255"
        set(value) = sp.edit().putString("broadcast_ip", value).apply()

    var wolPort: Int
        get() = sp.getInt("wol_port", 9)
        set(value) = sp.edit().putInt("wol_port", value).apply()

    // Адрес маленького HTTP-сервера/агента на самом ПК, который получает
    // команду на выключение (см. README о том, как его поднять).
    var shutdownUrl: String
        get() = sp.getString("shutdown_url", "") ?: ""
        set(value) = sp.edit().putString("shutdown_url", value).apply()

    // Имя пакета приложения Салют, из которого слушаем уведомления.
    // Пусто = реагировать на уведомления от любого приложения.
    var sourcePackage: String
        get() = sp.getString("source_package", "") ?: ""
        set(value) = sp.edit().putString("source_package", value).apply()

    // Фразы-триггеры (регистронезависимо, по вхождению в текст уведомления).
    var onPhrase: String
        get() = sp.getString("on_phrase", "включи компьютер") ?: "включи компьютер"
        set(value) = sp.edit().putString("on_phrase", value).apply()

    var offPhrase: String
        get() = sp.getString("off_phrase", "выключи компьютер") ?: "выключи компьютер"
        set(value) = sp.edit().putString("off_phrase", value).apply()
}
