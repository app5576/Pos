package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.fragments.SalesFragment;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Toast;
public class SalesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SalesFragment())
                .commit();
        }
    }
    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
