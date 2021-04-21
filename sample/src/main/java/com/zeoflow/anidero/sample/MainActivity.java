package com.zeoflow.anidero.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.zeoflow.anidero.AnideroAnimationView;
import com.zeoflow.app.Activity;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AnideroAnimationView zAnideroAnimationView = findViewById(R.id.zAnideroAnimationView);

        zAnideroAnimationView.setAnimation("animation.json");
        zAnideroAnimationView.playAnimation();
        zAnideroAnimationView.setSpeed(1f);

    }
}
