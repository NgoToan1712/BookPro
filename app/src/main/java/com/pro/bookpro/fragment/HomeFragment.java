package com.pro.bookpro.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pro.bookpro.ControllerApplication;
import com.pro.bookpro.R;
import com.pro.bookpro.activity.CategoryProductActivity;
import com.pro.bookpro.activity.BookDetailActivity;
import com.pro.bookpro.activity.MainActivity;
import com.pro.bookpro.adapter.BookGridAdapter;
import com.pro.bookpro.adapter.BookPopularAdapter;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.databinding.FragmentHomeBinding;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.model.User;
import com.pro.bookpro.prefs.DataStoreManager;
import com.pro.bookpro.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment {

    private FragmentHomeBinding mFragmentHomeBinding;

    private List<Book> mListBook;
    private List<Book> mListBookPopular;

    private final Handler mHandlerBanner = new Handler();
    private final Runnable mRunnableBanner = new Runnable() {
        @Override
        public void run() {
            if (mListBookPopular == null || mListBookPopular.isEmpty()) {
                return;
            }
            if (mFragmentHomeBinding.viewpager2.getCurrentItem() == mListBookPopular.size() - 1) {
                mFragmentHomeBinding.viewpager2.setCurrentItem(0);
                return;
            }
            mFragmentHomeBinding.viewpager2.setCurrentItem(mFragmentHomeBinding.viewpager2.getCurrentItem() + 1);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false);
        getListFoodFromFirebase("");
        initListener();
        loadDataUser();
        return mFragmentHomeBinding.getRoot();
    }

    @Override
    protected void initToolbar() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setToolBar(true, getString(R.string.home));
        }
    }

    private void initListener() {
        mFragmentHomeBinding.edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKey = s.toString().trim();
                if (strKey.equals("") || strKey.length() == 0) {
                    if (mListBook != null) mListBook.clear();
                    getListFoodFromFirebase("");
                }
            }
        });

        mFragmentHomeBinding.cvNewproduct.setOnClickListener(view -> goToCategoryActivity());
        mFragmentHomeBinding.cvSpecialproduct.setOnClickListener(view -> goToCategoryActivity());
        mFragmentHomeBinding.cvCombo1.setOnClickListener(view -> goToCategoryActivity());
        mFragmentHomeBinding.cvCombo2.setOnClickListener(view -> goToCategoryActivity());

        mFragmentHomeBinding.imgSearch.setOnClickListener(view -> searchFood());

        mFragmentHomeBinding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood();
                return true;
            }
            return false;
        });
    }

    private void goToCategoryActivity() {
        Intent intent = new Intent(getActivity(), CategoryProductActivity.class);
        startActivity(intent);
    }

    private void displayListFoodPopular() {
        BookPopularAdapter mBookPopularAdapter = new BookPopularAdapter(getListFoodPopular(), this::goToFoodDetail);
        mFragmentHomeBinding.viewpager2.setAdapter(mBookPopularAdapter);
        mFragmentHomeBinding.indicator3.setViewPager(mFragmentHomeBinding.viewpager2);

        mFragmentHomeBinding.viewpager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mHandlerBanner.removeCallbacks(mRunnableBanner);
                mHandlerBanner.postDelayed(mRunnableBanner, 3000);
            }
        });
    }

    private void displayListFoodSuggest() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mFragmentHomeBinding.rcvFood.setLayoutManager(gridLayoutManager);

        BookGridAdapter mBookGridAdapter = new BookGridAdapter(mListBook, this::goToFoodDetail);
        mFragmentHomeBinding.rcvFood.setAdapter(mBookGridAdapter);
    }

    private List<Book> getListFoodPopular() {
        mListBookPopular = new ArrayList<>();
        if (mListBook == null || mListBook.isEmpty()) {
            return mListBookPopular;
        }
        for (Book book : mListBook) {
            if (book.isPopular()) {
                mListBookPopular.add(book);
            }
        }
        return mListBookPopular;
    }

    private void getListFoodFromFirebase(String key) {
        if (getActivity() == null) {
            return;
        }

        if (key.isEmpty()){
            mFragmentHomeBinding.llRow1.setVisibility(View.VISIBLE);
            mFragmentHomeBinding.llRow2.setVisibility(View.VISIBLE);
        }else{
            mFragmentHomeBinding.llRow1.setVisibility(View.GONE);
            mFragmentHomeBinding.llRow2.setVisibility(View.GONE);
        }

        ControllerApplication.get(getActivity()).getFoodDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mFragmentHomeBinding.layoutContent.setVisibility(View.VISIBLE);

                mListBook = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Book book = dataSnapshot.getValue(Book.class);
                    if (book == null) {
                        return;
                    }

                    if (StringUtil.isEmpty(key)) {
                        mListBook.add(0, book);
                    } else {
                        if (GlobalFunction.getTextSearch(book.getName()).toLowerCase().trim()
                                .contains(GlobalFunction.getTextSearch(key).toLowerCase().trim())) {
                            mListBook.add(0, book);
                        }
                    }
                }
                displayListFoodPopular();
                displayListFoodSuggest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                GlobalFunction.showToastMessage(getActivity(), getString(R.string.msg_get_date_error));
            }
        });
    }

    private void searchFood() {
        String strKey = mFragmentHomeBinding.edtSearchName.getText().toString().trim();
        if (mListBook != null) mListBook.clear();
        getListFoodFromFirebase(strKey);
        GlobalFunction.hideSoftKeyboard(getActivity());
    }

    private void goToFoodDetail(@NonNull Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, book);
        GlobalFunction.startActivity(getActivity(), BookDetailActivity.class, bundle);
    }

    private void loadDataUser() {
        FirebaseDatabase.getInstance(Constant.FIREBASE_URL).getReference().child("User")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user.getId().equals(DataStoreManager.getUser().getId())) {
                                setLocation(user);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void setLocation(User user) {
        mFragmentHomeBinding.tvLocation.setText(user.getAddress());
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandlerBanner.removeCallbacks(mRunnableBanner);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandlerBanner.postDelayed(mRunnableBanner, 3000);
    }
}
