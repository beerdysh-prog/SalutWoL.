package com.beerdysh.salutwol

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Отправка magic-пакета Wake-on-LAN и HTTP-команды на выключение.
 * Оба метода — блокирующие, вызывать их нужно из фонового потока.
 */
object WolSender {

    sealed class Result {
        data class Success(val message: String) : Result()
        data class Failure(val message: String) : Result()
    }

    private val MAC_REGEX = Regex("^([0-9A-Fa-f]{2}[:\\-]?){5}[0-9A-Fa-f]{2}$")

    fun sendWake(mac: String, broadcastIp: String, port: Int = 9): Result {
        val macClean = mac.trim()
        require(MAC_REGEX.matches(macClean)) { "Некорректный MAC-адрес: $mac" }

        val macBytes = macClean
            .replace(":", "")
            .replace("-", "")
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()

        val packetBytes = ByteArray(6 + 16 * 6)
        for (i in 0 until 6) packetBytes[i] = 0xFF.toByte()
        for (i in 0 until 16) {
            System.arraycopy(macBytes, 0, packetBytes, 6 + i * 6, 6)
        }

        return try {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                val address = InetAddress.getByName(broadcastIp)
                val packet = DatagramPacket(packetBytes, packetBytes.size, address, port)
                socket.send(packet)
            }
            Result.Success("WoL пакет отправлен на $macClean через $broadcastIp:$port")
        } catch (e: IOException) {
            Result.Failure("Ошибка отправки WoL: ${e.message}")
        }
    }

    fun sendShutdown(url: String): Result {
        require(url.isNotBlank()) { "Не задан адрес сервера выключения" }
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doOutput = false
            val code = connection.responseCode
            connection.disconnect()
            if (code in 200..299) {
                Result.Success("Команда выключения отправлена ($code)")
            } else {
                Result.Failure("Сервер ответил кодом $code")
            }
        } catch (e: IOException) {
            Result.Failure("Ошибка отправки команды выключения: ${e.message}")
        }
    }
}
