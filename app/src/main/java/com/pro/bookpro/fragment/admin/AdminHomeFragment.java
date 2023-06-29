package com.pro.bookpro.fragment.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.pro.bookpro.ControllerApplication;
import com.pro.bookpro.R;
import com.pro.bookpro.activity.AddBookActivity;
import com.pro.bookpro.activity.AdminMainActivity;
import com.pro.bookpro.adapter.AdminBookAdapter;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.databinding.FragmentAdminHomeBinding;
import com.pro.bookpro.fragment.BaseFragment;
import com.pro.bookpro.listener.IOnManagerFoodListener;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends BaseFragment {

    private FragmentAdminHomeBinding mFragmentAdminHomeBinding;
    private List<Book> mListBook;
    private AdminBookAdapter mAdminFoodAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentAdminHomeBinding = FragmentAdminHomeBinding.inflate(inflater, container, false);

        initView();
        initListener();
        getListFood("");
        return mFragmentAdminHomeBinding.getRoot();
    }

    @Override
    protected void initToolbar() {
        if (getActivity() != null) {
            ((AdminMainActivity) getActivity()).setToolBar(getString(R.string.home));
        }
    }

    private void initView() {
        if (getActivity() == null) {
            return;
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mFragmentAdminHomeBinding.rcvFood.setLayoutManager(linearLayoutManager);
        mListBook = new ArrayList<>();
        mAdminFoodAdapter = new AdminBookAdapter(mListBook, new IOnManagerFoodListener() {
            @Override
            public void onClickUpdateFood(Book book) {
                onClickEditFood(book);
            }

            @Override
            public void onClickDeleteFood(Book book) {
                deleteFoodItem(book);
            }
        });
        mFragmentAdminHomeBinding.rcvFood.setAdapter(mAdminFoodAdapter);
    }

    private void initListener() {
        mFragmentAdminHomeBinding.btnAddFood.setOnClickListener(v -> onClickAddFood());

        mFragmentAdminHomeBinding.imgSearch.setOnClickListener(view1 -> searchFood());

        mFragmentAdminHomeBinding.edtSearchName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood();
                return true;
            }
            return false;
        });

        mFragmentAdminHomeBinding.edtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strKey = s.toString().trim();
                if (strKey.equals("") || strKey.length() == 0) {
                    searchFood();
                }
            }
        });
    }

    private void onClickAddFood() {
        GlobalFunction.startActivity(getActivity(), AddBookActivity.class);
    }

    private void onClickEditFood(Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, book);
        GlobalFunction.startActivity(getActivity(), AddBookActivity.class, bundle);
    }

    private void deleteFoodItem(Book book) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok), (dialogInterface, i) -> {
                    if (getActivity() == null) {
                        return;
                    }
                    ControllerApplication.get(getActivity()).getFoodDatabaseReference()
                            .child(String.valueOf(book.getId())).removeValue((error, ref) ->
                            Toast.makeText(getActivity(),
                                    getString(R.string.msg_delete_movie_successfully), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void searchFood() {
        String strKey = mFragmentAdminHomeBinding.edtSearchName.getText().toString().trim();
        if (mListBook != null) {
            mListBook.clear();
        } else {
            mListBook = new ArrayList<>();
        }
        getListFood(strKey);
        GlobalFunction.hideSoftKeyboard(getActivity());
    }

    public void getListFood(String keyword) {
        if (getActivity() == null) {
            return;
        }
        ControllerApplication.get(getActivity()).getFoodDatabaseReference()
                .addChildEventListener(new ChildEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                        Book book = dataSnapshot.getValue(Book.class);
                        if (book == null || mListBook == null || mAdminFoodAdapter == null) {
                            return;
                        }
                        if (StringUtil.isEmpty(keyword)) {
                            mListBook.add(0, book);
                        } else {
                            if (GlobalFunction.getTextSearch(book.getName()).toLowerCase().trim()
                                    .contains(GlobalFunction.getTextSearch(keyword).toLowerCase().trim())) {
                                mListBook.add(0, book);
                            }
                        }
                        mAdminFoodAdapter.notifyDataSetChanged();
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                        Book book = dataSnapshot.getValue(Book.class);
                        if (book == null || mListBook == null
                                || mListBook.isEmpty() || mAdminFoodAdapter == null) {
                            return;
                        }
                        for (int i = 0; i < mListBook.size(); i++) {
                            if (book.getId() == mListBook.get(i).getId()) {
                                mListBook.set(i, book);
                                break;
                            }
                        }
                        mAdminFoodAdapter.notifyDataSetChanged();
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        Book book = dataSnapshot.getValue(Book.class);
                        if (book == null || mListBook == null
                                || mListBook.isEmpty() || mAdminFoodAdapter == null) {
                            return;
                        }
                        for (Book bookObject : mListBook) {
                            if (book.getId() == bookObject.getId()) {
                                mListBook.remove(bookObject);
                                break;
                            }
                        }
                        mAdminFoodAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
}
