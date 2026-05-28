package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.databinding.ActivitySettingsBinding;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Toast;
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("الإعدادات");
        }
        // تحميل SettingsFragment الأصلي
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
