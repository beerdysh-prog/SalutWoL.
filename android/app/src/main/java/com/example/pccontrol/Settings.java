package com.example.pccontrol;

import android.content.Context;
import android.content.SharedPreferences;

public final class Settings {
    private static final String PREFS = "pc_power";
    private static final String MAC = "mac";
    private static final String BROADCAST = "broadcast";
    private static final String PC_IP = "pc_ip";
    private static final String SECRET = "secret";
    private static final String ENABLE_ON = "enable_on";
    private static final String ENABLE_OFF = "enable_off";

    private final SharedPreferences p;

    public Settings(Context context) { p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE); }
    public String mac() { return p.getString(MAC, "04:7C:16:78:9B:CE"); }
    public String broadcast() { return p.getString(BROADCAST, "192.168.0.255"); }
    public String pcIp() { return p.getString(PC_IP, "192.168.0.137"); }
    public String secret() { return p.getString(SECRET, "PCNP-7f8c2a4d9b1e6f3a8c5d0e7b2a9f4c6d"); }
    public boolean onEnabled() { return p.getBoolean(ENABLE_ON, true); }
    public boolean offEnabled() { return p.getBoolean(ENABLE_OFF, true); }
    public void save(String mac, String broadcast, String pcIp, String secret, boolean on, boolean off) {
        p.edit().putString(MAC, mac).putString(BROADCAST, broadcast).putString(PC_IP, pcIp)
                .putString(SECRET, secret).putBoolean(ENABLE_ON, on).putBoolean(ENABLE_OFF, off).apply();
    }
}
