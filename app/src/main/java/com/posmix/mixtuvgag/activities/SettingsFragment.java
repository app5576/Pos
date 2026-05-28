package com.posmix.mixtuvgag.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.utils.ExcelHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_CREATE_BACKUP = 101;
    private static final int REQUEST_CODE_OPEN_BACKUP = 102;
    private static final int REQUEST_CODE_IMPORT_EXCEL = 103;
    private static final int REQUEST_CODE_EXPORT_EXCEL = 104;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final String DATABASE_NAME = "micropos_db";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // المجموعات
        Preference manageCategories = findPreference("manage_categories");
        if (manageCategories != null) {
            manageCategories.setOnPreferenceClickListener(preference -> {
                try {
                    Intent intent = new Intent(getActivity(), CategoriesManagementActivity.class);
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
        }

        // الوحدات
        Preference manageUnits = findPreference("manage_units");
        if (manageUnits != null) {
            manageUnits.setOnPreferenceClickListener(preference -> {
                try {
                    Intent intent = new Intent(getActivity(), UnitsManagementActivity.class);
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            });
        }

        // نسخ احتياطي
        Preference backupData = findPreference("backup_data");
        if (backupData != null) {
            backupData.setOnPreferenceClickListener(preference -> {
                createBackupFile();
                return true;
            });
        }

        // استعادة
        Preference restoreData = findPreference("restore_data");
        if (restoreData != null) {
            restoreData.setOnPreferenceClickListener(preference -> {
                openBackupFile();
                return true;
            });
        }

        // استيراد Excel
        Preference importExcel = findPreference("import_excel");
        if (importExcel != null) {
            importExcel.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "جاري فتح الملفات...", Toast.LENGTH_SHORT).show();
                openFileForImportExcel();
                return true;
            });
        }

        // تصدير Excel
        Preference exportExcel = findPreference("export_excel");
        if (exportExcel != null) {
            exportExcel.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getContext(), "جاري تجهيز التصدير...", Toast.LENGTH_SHORT).show();
                createExportExcelFile();
                return true;
            });
        }
    }

    // ============ دوال Excel ============

    private void openFileForImportExcel() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            String[] mimeTypes = {"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, REQUEST_CODE_IMPORT_EXCEL);
        } catch (Exception e) {
            Toast.makeText(getContext(), "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void createExportExcelFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/vnd.ms-excel");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "products_" + sdf.format(new Date()) + ".xls";
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            startActivityForResult(intent, REQUEST_CODE_EXPORT_EXCEL);
        } catch (Exception e) {
            Toast.makeText(getContext(), "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void importProductsFromExcel(Uri uri) {
        Context context = getContext();
        if (context == null) return;

        new AlertDialog.Builder(context)
            .setTitle("تاكيد الاستيراد")
            .setMessage("سيتم اضافة المنتجات من ملف Excel")
            .setPositiveButton("بدء", (dialog, which) -> {
                Toast.makeText(context, "جاري الاستيراد...", Toast.LENGTH_SHORT).show();
                ExcelHelper.importProductsFromExcel(context, uri, new ExcelHelper.ImportCallback() {
                    @Override
                    public void onComplete(int successCount, int failCount, String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                new AlertDialog.Builder(context)
                                    .setTitle("نتيجة")
                                    .setMessage(message)
                                    .setPositiveButton("موافق", null)
                                    .show();
                            });
                        }
                    }
                    @Override
                    public void onError(String errorMessage) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
            })
            .setNegativeButton("الغاء", null)
            .show();
    }

    private void exportProductsToExcel(Uri uri) {
        Context context = getContext();
        if (context == null) return;
        
        Toast.makeText(context, "جاري التصدير...", Toast.LENGTH_SHORT).show();
        
        ExcelHelper.exportProductsToExcel(context, uri, new ExcelHelper.ExportCallback() {
            @Override
            public void onComplete(int totalProducts, String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    });
                }
            }
            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    // ============ نسخ احتياطي ============

    private void createBackupFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-sqlite3");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        intent.putExtra(Intent.EXTRA_TITLE, "backup_" + sdf.format(new Date()) + ".db");
        startActivityForResult(intent, REQUEST_CODE_CREATE_BACKUP);
    }

    private void openBackupFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-sqlite3");
        startActivityForResult(intent, REQUEST_CODE_OPEN_BACKUP);
    }

    private void performBackup(Uri uri) {
        Context context = getContext();
        if (context == null) return;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File dbFile = context.getDatabasePath(DATABASE_NAME);
                InputStream is = new FileInputStream(dbFile);
                OutputStream os = context.getContentResolver().openOutputStream(uri);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
                is.close();
                os.close();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(context, "تم النسخ الاحتياطي", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(context, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void showRestoreConfirmationDialog(Uri uri) {
        Context context = getContext();
        if (context == null) return;
        
        new AlertDialog.Builder(context)
            .setTitle("تاكيد الاستعادة")
            .setMessage("سيتم استعادة البيانات. هل انت متاكد؟")
            .setPositiveButton("استعادة", (dialog, which) -> performRestore(uri))
            .setNegativeButton("الغاء", null)
            .show();
    }

    private void performRestore(Uri uri) {
        Context context = getContext();
        if (context == null) return;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase.getInstance(context).getWritableDatabase().close();
                AppDatabase.resetInstance();
                File dbFile = context.getDatabasePath(DATABASE_NAME);
                if (dbFile.exists()) dbFile.delete();
                InputStream is = context.getContentResolver().openInputStream(uri);
                OutputStream os = new FileOutputStream(dbFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
                is.close();
                os.close();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(context, "تمت الاستعادة", Toast.LENGTH_LONG).show();
                        Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        getActivity().finishAffinity();
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(context, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_CODE_CREATE_BACKUP) {
                performBackup(uri);
            } else if (requestCode == REQUEST_CODE_OPEN_BACKUP) {
                showRestoreConfirmationDialog(uri);
            } else if (requestCode == REQUEST_CODE_IMPORT_EXCEL) {
                importProductsFromExcel(uri);
            } else if (requestCode == REQUEST_CODE_EXPORT_EXCEL) {
                exportProductsToExcel(uri);
            }
        }
    }
}