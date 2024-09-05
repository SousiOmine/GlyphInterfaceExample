package com.sousiomine.battery2a;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
import com.nothing.ketchum.GlyphManager;

public class BackgroundWorker extends Worker {
    GlyphManager gManager;

    public BackgroundWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

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
    }

    @NonNull
    @Override
    public Result doWork() {
        GlyphFrame.Builder builder = gManager.getGlyphFrameBuilder();
        GlyphFrame frame = builder.buildChannelC().build();
        try {
            gManager.displayProgress(frame, updateBatteryPercentage());
            return Result.success();
        } catch (GlyphException e) {
            return Result.failure();
        }
    }

    @Override
    public void onStopped() {

        super.onStopped();
        gManager.turnOff();

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
}
