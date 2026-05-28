package com.posmix.mixtuvgag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.utils.SessionManager;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Toast;
import com.posmix.mixtuvgag.utils.CurrencyHelper;
public class SplashActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CurrencyHelper.init(this);
        setContentView(R.layout.activity_splash);
        
        SessionManager sessionManager = new SessionManager(this);
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> destinationClass;
            
            // التحقق من حالة تسجيل الدخول
            if (sessionManager.isLoggedIn()) {
                destinationClass = ElementsActivity.class;
            } else {
                destinationClass = LoginActivity.class;
            }
            
            Intent intent = new Intent(SplashActivity.this, destinationClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }
}
