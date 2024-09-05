package com.sousiomine.battery2a;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
import com.nothing.ketchum.GlyphManager;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    GlyphManager gManager;

    Handler handler = new Handler();
    Runnable batterySyncRunnable;

    TextView textView;
    Switch toggleSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        GlyphManager.Callback callback = new GlyphManager.Callback() {
            @Override
            public void onServiceConnected(ComponentName componentName) {
                if (Common.is20111()) gManager.register(Common.DEVICE_20111);
                if (Common.is22111()) gManager.register(Common.DEVICE_22111);
                if (Common.is23111()) gManager.register(Common.DEVICE_23111);
                try {
                    gManager.openSession();
                } catch(GlyphException e) {
                    Log.e(gManager.toString(), e.getMessage());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                try {
                    gManager.closeSession();
                } catch (GlyphException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        gManager = GlyphManager.getInstance(getApplicationContext());
        gManager.init(callback);

        textView = findViewById(R.id.textView);
        toggleSwitch = findViewById(R.id.toggleSwitch);
        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startGlyph();
            } else {
                stopGlyph();
            }
        });

        batterySyncRunnable = new Runnable() {
            @Override
            public void run() {
                GlyphFrame.Builder builder = gManager.getGlyphFrameBuilder();
                GlyphFrame frame = builder.buildChannelC().build();
                try {
                    gManager.displayProgress(frame, updateBatteryPercentage());
                } catch (GlyphException e) {

                }

                handler.postDelayed(this, 1000);
            }
        };

        startGlyph();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopGlyph();
        try {
            gManager.closeSession();
        } catch (GlyphException e) {
            throw new RuntimeException(e);
        }
        gManager.unInit();
    }

    private int updateBatteryPercentage() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);
            return batteryPct;
        }
        return -1;
    }

    private void startGlyph() {
        handler.postDelayed(batterySyncRunnable, 0);
    }

    private void stopGlyph() {
        handler.removeCallbacks(batterySyncRunnable);
        gManager.turnOff();
    }
}