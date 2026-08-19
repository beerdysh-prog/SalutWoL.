package com.example.pccontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private EditText mac, broadcast, pcIp, secret;
    private CheckBox on, off;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        Settings store = new Settings(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 36, 36, 36);

        TextView title = new TextView(this);
        title.setText("PC Notification Power");
        title.setTextSize(24);
        root.addView(title, wrap());

        TextView info = new TextView(this);
        info.setText("Реагирует на уведомления:\nИстория сценариев → Включи компьютер / Выключи компьютер");
        info.setPadding(0, 18, 0, 24);
        root.addView(info, wrap());

        mac = field("MAC ПК, например 00:11:22:33:44:55", store.mac());
        broadcast = field("Broadcast, например 192.168.1.255", store.broadcast());
        pcIp = field("IP ПК, например 192.168.1.100", store.pcIp());
        secret = field("Секретный ключ", store.secret());
        root.addView(mac); root.addView(broadcast); root.addView(pcIp); root.addView(secret);

        on = new CheckBox(this); on.setText("Включать ПК по уведомлению"); on.setChecked(store.onEnabled()); root.addView(on);
        off = new CheckBox(this); off.setText("Выключать ПК по уведомлению"); off.setChecked(store.offEnabled()); root.addView(off);

        Button save = new Button(this); save.setText("Сохранить");
        save.setOnClickListener(v -> {
            store.save(mac.getText().toString().trim(), broadcast.getText().toString().trim(), pcIp.getText().toString().trim(), secret.getText().toString().trim(), on.isChecked(), off.isChecked());
            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show();
        });
        root.addView(save, wrap());

        Button access = new Button(this); access.setText("Разрешить доступ к уведомлениям");
        access.setOnClickListener(v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
        root.addView(access, wrap());

        setContentView(root);
    }

    private EditText field(String hint, String value) {
        EditText e = new EditText(this); e.setHint(hint); e.setText(value); e.setSingleLine(true);
        e.setPadding(0, 8, 0, 8); return e;
    }
    private LinearLayout.LayoutParams wrap() { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
}
