package com.posmix.mixtuvgag.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.posmix.mixtuvgag.fragments.CategoriesFragment;
import com.posmix.mixtuvgag.fragments.UnitsFragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;
public class SettingsPagerAdapter extends FragmentStateAdapter {
    public SettingsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new CategoriesFragment();
        else return new UnitsFragment();
    }
    
    @Override
    public int getItemCount() { return 2; }
}
