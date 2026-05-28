package com.posmix.mixtuvgag.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.posmix.mixtuvgag.R;
import com.posmix.mixtuvgag.adapters.ProductUnitsAdapter;
import com.posmix.mixtuvgag.adapters.ProductsAdapter;
import com.posmix.mixtuvgag.database.AppDatabase;
import com.posmix.mixtuvgag.databinding.ActivityInventoryBinding;
import com.posmix.mixtuvgag.models.Category;
import com.posmix.mixtuvgag.models.Product;
import com.posmix.mixtuvgag.models.ProductUnit;
import com.posmix.mixtuvgag.models.Unit;
import com.posmix.mixtuvgag.viewmodels.InventoryViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
public class InventoryActivity extends AppCompatActivity {

    private ActivityInventoryBinding binding;
    private InventoryViewModel viewModel;
    private ProductsAdapter productsAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private String currentFilter = "all";
    private String currentSearch = "";
    private AppDatabase db;
    private List<Category> categoriesList = new ArrayList<>();
    private List<Unit> unitsList = new ArrayList<>();
    private List<ProductUnit> productUnitsList = new ArrayList<>();
    private ProductUnitsAdapter productUnitsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            binding = ActivityInventoryBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "خطأ في تحميل الواجهة: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        try {
            db = AppDatabase.getInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "خطأ في قاعدة البيانات: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("إدارة المخزون");
        }
        
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        
        // Setup RecyclerView
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productsAdapter = new ProductsAdapter(new ProductsAdapter.ProductClickListener() {
            @Override public void onProductClick(Product product) { showProductDialog(product); }
            @Override public void onDeleteClick(Product product) { handleDeleteProduct(product); }
            @Override public void onEditSellPriceClick(Product product) {
                // New: Handle direct edit sell price click
                showProductDialog(product); // Reusing the same dialog for simplicity
            }
        });
        binding.rvProducts.setAdapter(productsAdapter);
        
        // تحميل البيانات
        loadCategoriesAndUnits();
        
        // مراقبة المنتجات
        viewModel.getProducts().observe(this, products -> {
            allProducts = products != null ? products : new ArrayList<>();
            updateSummaryCards(allProducts);
            applyFilters();
            if (binding.swipeRefreshLayout.isRefreshing()) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
        
        // بحث
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        
        // فلاتر
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) currentFilter = "all";
            else if (id == R.id.chipAvailable) currentFilter = "available";
            else if (id == R.id.chipLowStock) currentFilter = "low";
            else if (id == R.id.chipOutOfStock) currentFilter = "out";
            applyFilters();
        });
        
        // زر إضافة منتج
        binding.fabAddProduct.setOnClickListener(v -> showProductDialog(null));
        
        // تحديث بالسحب
        binding.swipeRefreshLayout.setOnRefreshListener(() -> 
            viewModel.getProducts().observe(this, products -> {
                allProducts = products != null ? products : new ArrayList<>();
                updateSummaryCards(allProducts);
                applyFilters();
                binding.swipeRefreshLayout.setRefreshing(false);
            })
        );
    }
    
    private void loadCategoriesAndUnits() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Category> cats = db.categoryDao().getAllSync();
                List<Unit> units = db.unitDao().getAllSync();
                
                runOnUiThread(() -> {
                    categoriesList.clear();
                    categoriesList.addAll(cats != null ? cats : new ArrayList<>());
                    unitsList.clear();
                    unitsList.addAll(units != null ? units : new ArrayList<>());
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "خطأ في تحميل البيانات: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
    
    private void updateSummaryCards(List<Product> products) {
        int total = products.size();
        int lowStock = 0, outOfStock = 0;
        for (Product p : products) {
            if (p.getStockQuantity() == 0) outOfStock++;
            else if (p.isLowStock()) lowStock++;
        }
        binding.tvTotalProducts.setText(String.valueOf(total));
        binding.tvLowStockCount.setText(String.valueOf(lowStock));
        binding.tvOutOfStockCount.setText(String.valueOf(outOfStock));
    }
    
    private void applyFilters() {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            boolean passFilter;
            switch (currentFilter) {
                case "available": passFilter = p.getStockQuantity() > 0 && !p.isLowStock(); break;
                case "low": passFilter = p.isLowStock(); break;
                case "out": passFilter = p.getStockQuantity() == 0; break;
                default: passFilter = true;
            }
            boolean passSearch = currentSearch.isEmpty()
                    || p.getName().toLowerCase().contains(currentSearch.toLowerCase())
                    || (p.getBarcode() != null && p.getBarcode().contains(currentSearch));
            if (passFilter && passSearch) filtered.add(p);
        }
        productsAdapter.submitList(filtered);
    }
    
    private void showProductDialog(Product existingProduct) {
        // التأكد من تحميل البيانات أولاً
        if (categoriesList.isEmpty() || unitsList.isEmpty()) {
            Toast.makeText(this, "جاري تحميل البيانات، حاول مرة أخرى", Toast.LENGTH_SHORT).show();
            loadCategoriesAndUnits();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_product_advanced, null);
        
        EditText etProductName = view.findViewById(R.id.et_product_name);
        EditText etBarcode = view.findViewById(R.id.et_product_barcode);
        EditText etQuantity = view.findViewById(R.id.et_product_quantity);
        EditText etBuyPrice = view.findViewById(R.id.et_product_buy_price);
        EditText etSellPrice = view.findViewById(R.id.et_product_sell_price);
        EditText etMinStock = view.findViewById(R.id.et_product_min_stock);
        EditText etNotes = view.findViewById(R.id.et_product_notes);
        EditText etTaxPercentage = view.findViewById(R.id.et_product_tax_percentage);
        
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);
        Spinner spinnerBaseUnit = view.findViewById(R.id.spinner_base_unit);
        
        // إعداد قائمة المجموعات
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // إعداد قائمة الوحدات الأساسية
        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitsList);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBaseUnit.setAdapter(unitAdapter);
        
        // زر إضافة مجموعة جديدة
        Button btnAddCategory = view.findViewById(R.id.btn_add_category);
        if (btnAddCategory != null) {
            btnAddCategory.setOnClickListener(v -> {
                showAddCategoryDialog(categoryAdapter, spinnerCategory);
            });
        }
        
        // قسم الوحدات الأخرى
        RecyclerView rvProductUnits = view.findViewById(R.id.rv_product_units);
        if (rvProductUnits != null) {
            rvProductUnits.setLayoutManager(new LinearLayoutManager(this));
            productUnitsAdapter = new ProductUnitsAdapter();
            productUnitsAdapter.setOnItemClickListener(new ProductUnitsAdapter.OnItemClickListener() {
                @Override
                public void onEdit(ProductUnit productUnit) {
                    showAddProductUnitDialog(existingProduct != null ? existingProduct.getId() : 0, productUnit);
                }
                @Override
                public void onDelete(ProductUnit productUnit) {
                    new AlertDialog.Builder(InventoryActivity.this)
                        .setTitle("تأكيد الحذف")
                        .setMessage("هل أنت متأكد من حذف هذه الوحدة؟")
                        .setPositiveButton("حذف", (d, w) -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                db.productUnitDao().delete(productUnit);
                                productUnitsList.remove(productUnit);
                                runOnUiThread(() -> productUnitsAdapter.submitList(new ArrayList<>(productUnitsList)));
                            });
                        })
                        .setNegativeButton("إلغاء", null)
                        .show();
                }
            });
            rvProductUnits.setAdapter(productUnitsAdapter);
        }
        
        FloatingActionButton fabAddUnit = view.findViewById(R.id.fab_add_product_unit);
        
        boolean isEdit = existingProduct != null;
        if (isEdit) {
            etProductName.setText(existingProduct.getName());
            etBarcode.setText(existingProduct.getBarcode() != null ? existingProduct.getBarcode() : "");
            etQuantity.setText(String.valueOf(existingProduct.getStockQuantity()));
            etBuyPrice.setText(String.valueOf(existingProduct.getBuyPrice()));
            etSellPrice.setText(String.valueOf(existingProduct.getSellPrice()));
            etMinStock.setText(String.valueOf(existingProduct.getMinStockAlert()));
            etNotes.setText(existingProduct.getNotes() != null ? existingProduct.getNotes() : "");
            etTaxPercentage.setText(String.valueOf(existingProduct.getTaxPercentage()));
            
            // تحديد المجموعة
            for (int i = 0; i < categoriesList.size(); i++) {
                if (categoriesList.get(i).getId() == existingProduct.getCategoryId()) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            // تحديد الوحدة الأساسية
            for (int i = 0; i < unitsList.size(); i++) {
                if (unitsList.get(i).getId() == existingProduct.getBaseUnitId()) {
                    spinnerBaseUnit.setSelection(i);
                    break;
                }
            }
            
            // تحميل الوحدات الأخرى للمنتج
            Executors.newSingleThreadExecutor().execute(() -> {
                productUnitsList = db.productUnitDao().getForProduct(existingProduct.getId());
                runOnUiThread(() -> {
                    if (productUnitsAdapter != null) {
                        productUnitsAdapter.submitList(new ArrayList<>(productUnitsList));
                    }
                });
            });
            
            builder.setTitle("تعديل المنتج");
            
            if (fabAddUnit != null) {
                fabAddUnit.setOnClickListener(v -> showAddProductUnitDialog(existingProduct.getId(), null));
            }
        } else {
            builder.setTitle("إضافة منتج جديد");
            productUnitsList.clear();
            if (productUnitsAdapter != null) {
                productUnitsAdapter.submitList(productUnitsList);
            }
            if (fabAddUnit != null) {
                fabAddUnit.setOnClickListener(v -> Toast.makeText(this, "احفظ المنتج أولاً ثم أضف الوحدات", Toast.LENGTH_SHORT).show());
            }
        }
        
        builder.setView(view)
            .setPositiveButton(isEdit ? "تحديث" : "حفظ", (dialog, which) -> {
                String name = etProductName.getText().toString().trim();
                String buyPriceStr = etBuyPrice.getText().toString().trim();
                String sellPriceStr = etSellPrice.getText().toString().trim();
                
                if (name.isEmpty() || buyPriceStr.isEmpty() || sellPriceStr.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال اسم المنتج، سعر الشراء، وسعر البيع", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
                    Unit selectedUnit = (Unit) spinnerBaseUnit.getSelectedItem();
                    
                    int quantity = etQuantity.getText().toString().isEmpty() ? 0 : Integer.parseInt(etQuantity.getText().toString());
                    double buyPrice = Double.parseDouble(buyPriceStr);
                    double sellPrice = Double.parseDouble(sellPriceStr);
                    int minStock = etMinStock.getText().toString().isEmpty() ? 5 : Integer.parseInt(etMinStock.getText().toString());
                    double tax = etTaxPercentage.getText().toString().isEmpty() ? 0 : Double.parseDouble(etTaxPercentage.getText().toString());
                    
                    Product product;
                    if (isEdit) {
                        product = existingProduct;
                        product.setName(name);
                        product.setBarcode(etBarcode.getText().toString().trim());
                        product.setStockQuantity(quantity);
                        product.setBuyPrice(buyPrice);
                        product.setSellPrice(sellPrice);
                        product.setMinStockAlert(minStock);
                        product.setNotes(etNotes.getText().toString().trim());
                        product.setTaxPercentage(tax);
                        product.setCategoryId(selectedCategory != null ? selectedCategory.getId() : 0);
                        product.setCategoryName(selectedCategory != null ? selectedCategory.getName() : "");
                        product.setBaseUnitId(selectedUnit != null ? selectedUnit.getId() : 0);
                        product.setBaseUnitName(selectedUnit != null ? selectedUnit.getName() : "");
                        viewModel.update(product);
                        Toast.makeText(this, "✅ تم تحديث المنتج", Toast.LENGTH_SHORT).show();
                    } else {
                        product = new Product();
                        product.setName(name);
                        product.setBarcode(etBarcode.getText().toString().trim());
                        product.setStockQuantity(quantity);
                        product.setBuyPrice(buyPrice);
                        product.setSellPrice(sellPrice);
                        product.setMinStockAlert(minStock);
                        product.setNotes(etNotes.getText().toString().trim());
                        product.setTaxPercentage(tax);
                        product.setCategoryId(selectedCategory != null ? selectedCategory.getId() : 0);
                        product.setCategoryName(selectedCategory != null ? selectedCategory.getName() : "");
                        product.setBaseUnitId(selectedUnit != null ? selectedUnit.getId() : 0);
                        product.setBaseUnitName(selectedUnit != null ? selectedUnit.getName() : "");
                        long id = viewModel.insertAndGetId(product);
                        product.setId((int) id);
                        Toast.makeText(this, "✅ تم إضافة المنتج بنجاح", Toast.LENGTH_SHORT).show();
                        
                        // حفظ الوحدات الأخرى للمنتج الجديد
                        for (ProductUnit pu : productUnitsList) {
                            pu.setProductId((int) id);
                            Executors.newSingleThreadExecutor().execute(() -> db.productUnitDao().insert(pu));
                        }
                    }
                    binding.swipeRefreshLayout.setRefreshing(true);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "الرجاء إدخال أرقام صالحة", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    private void showAddCategoryDialog(ArrayAdapter<Category> adapter, Spinner spinner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_category, null);
        EditText etName = view.findViewById(R.id.et_category_name);
        EditText etDescription = view.findViewById(R.id.et_category_description);
        
        builder.setTitle("إضافة مجموعة جديدة")
            .setView(view)
            .setPositiveButton("إضافة", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال اسم المجموعة", Toast.LENGTH_SHORT).show();
                    return;
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    Category newCat = new Category();
                    newCat.setName(name);
                    newCat.setDescription(etDescription.getText().toString().trim());
                    long id = db.categoryDao().insert(newCat);
                    newCat.setId((int) id);
                    runOnUiThread(() -> {
                        categoriesList.add(newCat);
                        adapter.notifyDataSetChanged();
                        spinner.setSelection(categoriesList.size() - 1);
                        Toast.makeText(this, "تمت إضافة المجموعة", Toast.LENGTH_SHORT).show();
                    });
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    private void showAddProductUnitDialog(int productId, ProductUnit existingUnit) {
        if (unitsList.isEmpty()) {
            Toast.makeText(this, "لا توجد وحدات متاحة، قم بإضافة وحدات أولاً من الإعدادات", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_product_unit, null);
        
        Spinner spinnerUnit = view.findViewById(R.id.spinner_unit);
        EditText etQuantity = view.findViewById(R.id.et_unit_quantity);
        EditText etBarcode = view.findViewById(R.id.et_unit_barcode);
        EditText etSellPrice = view.findViewById(R.id.et_unit_sell_price);
        
        ArrayAdapter<Unit> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitsList);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
        
        boolean isEdit = existingUnit != null;
        if (isEdit) {
            for (int i = 0; i < unitsList.size(); i++) {
                if (unitsList.get(i).getId() == existingUnit.getUnitId()) {
                    spinnerUnit.setSelection(i);
                    break;
                }
            }
            etQuantity.setText(String.valueOf(existingUnit.getQuantity()));
            etBarcode.setText(existingUnit.getBarcode() != null ? existingUnit.getBarcode() : "");
            etSellPrice.setText(String.valueOf(existingUnit.getSellPrice()));
            builder.setTitle("تعديل وحدة المنتج");
        } else {
            builder.setTitle("إضافة وحدة جديدة للمنتج");
        }
        
        builder.setView(view)
            .setPositiveButton(isEdit ? "تحديث" : "إضافة", (dialog, which) -> {
                String quantityStr = etQuantity.getText().toString().trim();
                String sellPriceStr = etSellPrice.getText().toString().trim();
                if (quantityStr.isEmpty() || sellPriceStr.isEmpty()) {
                    Toast.makeText(this, "الرجاء إدخال عدد الوحدات وسعر البيع", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Unit selectedUnit = (Unit) spinnerUnit.getSelectedItem();
                if (selectedUnit == null) {
                    Toast.makeText(this, "الرجاء اختيار الوحدة", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                ProductUnit pu = isEdit ? existingUnit : new ProductUnit();
                pu.setProductId(productId);
                pu.setUnitId(selectedUnit.getId());
                pu.setUnitName(selectedUnit.getName());
                pu.setQuantity(Integer.parseInt(quantityStr));
                pu.setBarcode(etBarcode.getText().toString().trim());
                pu.setSellPrice(Double.parseDouble(sellPriceStr));
                
                Executors.newSingleThreadExecutor().execute(() -> {
                    if (isEdit) {
                        db.productUnitDao().update(pu);
                    } else {
                        long id = db.productUnitDao().insert(pu);
                        pu.setId((int) id);
                        productUnitsList.add(pu);
                    }
                    runOnUiThread(() -> {
                        if (productUnitsAdapter != null) {
                            productUnitsAdapter.submitList(new ArrayList<>(productUnitsList));
                        }
                        Toast.makeText(this, isEdit ? "تم التحديث" : "تمت الإضافة", Toast.LENGTH_SHORT).show();
                    });
                });
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    private void handleDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
            .setTitle("تأكيد الحذف")
            .setMessage("هل أنت متأكد أنك تريد حذف المنتج '" + product.getName() + "'؟")
            .setPositiveButton("حذف", (dialog, which) -> {
                viewModel.delete(product);
                Toast.makeText(this, "تم حذف المنتج", Toast.LENGTH_SHORT).show();
                binding.swipeRefreshLayout.setRefreshing(true);
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}