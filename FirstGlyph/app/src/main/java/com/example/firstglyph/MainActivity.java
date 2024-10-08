package com.example.firstglyph;

import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nothing.ketchum.Common;
import com.nothing.ketchum.Glyph;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
import com.nothing.ketchum.GlyphManager;

public class MainActivity extends AppCompatActivity{

    GlyphManager gManager = null;

    Button button1;
    Button button2;
    Button button3;

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

        GlyphManager.Callback gcallback = new GlyphManager.Callback() {
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
        gManager.init(gcallback);


        button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///ここが光らせる処理
                GlyphFrame.Builder builder = gManager.getGlyphFrameBuilder();
                GlyphFrame frame = builder.buildChannelA().build();
                gManager.toggle(frame);
            }
        });

        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlyphFrame.Builder builder = gManager.getGlyphFrameBuilder();
                GlyphFrame frame = builder.buildChannelC().build();
                try {
                    //たぶん0~100で長さ調整できる
                    gManager.displayProgress(frame, 50);
                } catch (GlyphException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GlyphFrame.Builder builder = gManager.getGlyphFrameBuilder();
                GlyphFrame frame = builder.buildChannelB().buildInterval(1000).buildCycles(3).buildPeriod(3000).build();
                gManager.animate(frame);
            }
        });

    }

    @Override
    public void onDestroy()
    {
        try {
            gManager.closeSession();
        } catch (GlyphException e) {
            throw new RuntimeException(e);
        }
        gManager.unInit();
        super.onDestroy();
    }

}