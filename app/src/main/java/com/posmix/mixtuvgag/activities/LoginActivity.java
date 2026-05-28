package com.posmix.mixtuvgag.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.utils.SessionManager;

import androidx.annotation.NonNull;
import android.view.View;
public class LoginActivity extends AppCompatActivity {
    
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        sessionManager = new SessionManager(this);
        
        // التحقق من وجود جلسة تسجيل دخول سابقة
        if (sessionManager.isLoggedIn()) {
            // إذا كان المستخدم مسجلاً دخول مسبقاً، انتقل مباشرة إلى ElementsActivity
            navigateToMain();
            return;
        }
        
        // تهيئة العناصر
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        
        // إعداد مستمع زر تسجيل الدخول
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "الرجاء إدخال اسم المستخدم وكلمة المرور", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // التحقق من بيانات تسجيل الدخول (admin/1234)
            if (username.equals("admin") && password.equals("1234")) {
                // حفظ حالة تسجيل الدخول
                sessionManager.setLogin(true, username);
                Toast.makeText(LoginActivity.this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                Toast.makeText(LoginActivity.this, "اسم المستخدم أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, ElementsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
