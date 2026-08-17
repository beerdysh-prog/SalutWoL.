package com.beerdysh.salutwol

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var macField: EditText
    private lateinit var broadcastField: EditText
    private lateinit var portField: EditText
    private lateinit var shutdownUrlField: EditText
    private lateinit var sourcePackageField: EditText
    private lateinit var onPhraseField: EditText
    private lateinit var offPhraseField: EditText
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(applicationContext)
        setContentView(buildUi())
        refreshLog()
    }

    override fun onResume() {
        super.onResume()
        refreshLog()
    }

    private fun pad(view: LinearLayout, dp: Int = 24) {
        val d = (dp * resources.displayMetrics.density).toInt()
        view.setPadding(d, d, d, d)
    }

    private fun label(text: String): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.textSize = 14f
        tv.setPadding(0, 24, 0, 4)
        return tv
    }

    private fun buildUi(): ScrollView {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        pad(root)

        val title = TextView(this)
        title.text = "SalutWoL"
        title.textSize = 22f
        title.gravity = Gravity.START
        root.addView(title)

        val subtitle = TextView(this)
        subtitle.text = "Реагирует на уведомления Салюта и включает/выключает ПК"
        subtitle.textSize = 13f
        subtitle.setPadding(0, 4, 0, 0)
        root.addView(subtitle)

        root.addView(label("1. Доступ к уведомлениям"))
        val grantButton = Button(this)
        grantButton.text = "Открыть настройки доступа к уведомлениям"
        grantButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        root.addView(grantButton)

        root.addView(label("2. Параметры включения (Wake-on-LAN)"))
        macField = EditText(this)
        macField.hint = "MAC-адрес ПК, напр. AA:BB:CC:DD:EE:FF"
        macField.setText(prefs.macAddress)
        root.addView(macField)

        broadcastField = EditText(this)
        broadcastField.hint = "Broadcast IP, напр. 192.168.1.255"
        broadcastField.setText(prefs.broadcastIp)
        root.addView(broadcastField)

        portField = EditText(this)
        portField.hint = "Порт WoL (обычно 9)"
        portField.setText(prefs.wolPort.toString())
        root.addView(portField)

        root.addView(label("3. Параметры выключения"))
        shutdownUrlField = EditText(this)
        shutdownUrlField.hint = "URL агента на ПК, напр. http://192.168.1.100:8765/shutdown"
        shutdownUrlField.setText(prefs.shutdownUrl)
        root.addView(shutdownUrlField)
        val shutdownHint = TextView(this)
        shutdownHint.text = "На самом ПК должен работать небольшой сервер/агент, " +
            "принимающий этот запрос и выполняющий выключение (см. README проекта)."
        shutdownHint.textSize = 12f
        shutdownHint.setPadding(0, 4, 0, 0)
        root.addView(shutdownHint)

        root.addView(label("4. Источник уведомлений (необязательно)"))
        sourcePackageField = EditText(this)
        sourcePackageField.hint = "Пакет приложения Салют (пусто = любое приложение)"
        sourcePackageField.setText(prefs.sourcePackage)
        root.addView(sourcePackageField)

        onPhraseField = EditText(this)
        onPhraseField.hint = "Фраза для включения"
        onPhraseField.setText(prefs.onPhrase)
        root.addView(onPhraseField)

        offPhraseField = EditText(this)
        offPhraseField.hint = "Фраза для выключения"
        offPhraseField.setText(prefs.offPhrase)
        root.addView(offPhraseField)

        val saveButton = Button(this)
        saveButton.text = "Сохранить настройки"
        saveButton.setOnClickListener { saveSettings() }
        root.addView(saveButton)

        root.addView(label("5. Проверка вручную"))
        val testWakeButton = Button(this)
        testWakeButton.text = "Отправить WoL сейчас"
        testWakeButton.setOnClickListener { saveSettings(); sendWakeManually() }
        root.addView(testWakeButton)

        val testShutdownButton = Button(this)
        testShutdownButton.text = "Отправить команду выключения сейчас"
        testShutdownButton.setOnClickListener { saveSettings(); sendShutdownManually() }
        root.addView(testShutdownButton)

        root.addView(label("Журнал событий"))
        logView = TextView(this)
        logView.textSize = 12f
        logView.setPadding(0, 4, 0, 24)
        root.addView(logView)

        val refreshButton = Button(this)
        refreshButton.text = "Обновить журнал"
        refreshButton.setOnClickListener { refreshLog() }
        root.addView(refreshButton)

        val scroll = ScrollView(this)
        scroll.addView(root)
        return scroll
    }

    private fun saveSettings() {
        prefs.macAddress = macField.text.toString().trim()
        prefs.broadcastIp = broadcastField.text.toString().trim().ifBlank { "255.255.255.255" }
        prefs.wolPort = portField.text.toString().trim().toIntOrNull() ?: 9
        prefs.shutdownUrl = shutdownUrlField.text.toString().trim()
        prefs.sourcePackage = sourcePackageField.text.toString().trim()
        prefs.onPhrase = onPhraseField.text.toString().trim()
        prefs.offPhrase = offPhraseField.text.toString().trim()
        Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show()
    }

    private fun sendWakeManually() {
        executor.execute {
            val result = WolSender.sendWake(prefs.macAddress, prefs.broadcastIp, prefs.wolPort)
            val msg = when (result) {
                is WolSender.Result.Success -> result.message
                is WolSender.Result.Failure -> result.message
            }
            EventLog.add(msg)
            runOnUiThread { refreshLog() }
        }
    }

    private fun sendShutdownManually() {
        executor.execute {
            val result = WolSender.sendShutdown(prefs.shutdownUrl)
            val msg = when (result) {
                is WolSender.Result.Success -> result.message
                is WolSender.Result.Failure -> result.message
            }
            EventLog.add(msg)
            runOnUiThread { refreshLog() }
        }
    }

    private fun refreshLog() {
        logView.text = EventLog.asText()
    }
}
