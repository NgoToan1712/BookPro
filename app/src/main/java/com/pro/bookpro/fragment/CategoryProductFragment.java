package com.pro.bookpro.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.pro.bookpro.databinding.FragmentCategoryProductBinding;
import com.pro.bookpro.utils.ProductAdapter_tabLayout;

public class CategoryProductFragment extends Fragment {
    FragmentCategoryProductBinding binding;
    private TabLayout tabLayoutProduct;
    private ViewPager2 viewPagerProduct;
    private ProductAdapter_tabLayout adapter_tabLayout;

    public CategoryProductFragment() {
        // Required empty public constructor
    }

    public void tabLayout(){
        tabLayoutProduct = binding.tabProductFragment;
        viewPagerProduct = binding.viewPagerProductFragment;
        adapter_tabLayout = new ProductAdapter_tabLayout(this);
        viewPagerProduct.setAdapter(adapter_tabLayout);
        new TabLayoutMediator(tabLayoutProduct, viewPagerProduct, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0: tab.setText("GIÁO TRÌNH");
                        break;
                    case 1: tab.setText("TRUYỆN");
                        break;
                    case 2:tab.setText("VĂN HỌC NGHỆ THUẬT");
                        break;
                    case 3:tab.setText("KHÁC");
                        break;
                }
            }
        }).attach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCategoryProductBinding.inflate(inflater, container, false);
        tabLayout();
        return binding.getRoot();
    }
}