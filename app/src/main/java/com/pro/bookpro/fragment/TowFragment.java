package com.pro.bookpro.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pro.bookpro.ControllerApplication;
import com.pro.bookpro.R;
import com.pro.bookpro.activity.BookDetailActivity;
import com.pro.bookpro.adapter.BookGridAdapter;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.databinding.FragmentTowBinding;
import com.pro.bookpro.model.Book;

import java.util.ArrayList;
import java.util.List;


public class TowFragment extends Fragment {
    private FragmentTowBinding binding;
    private List<Book> mListBook;

    public TowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void getListFoodFromFirebase(String key) {
        if (getActivity() == null) {
            return;
        }
        ControllerApplication.get(getActivity()).getFoodDatabaseReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mListBook = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Book book = dataSnapshot.getValue(Book.class);
                    if (book == null) {
                        return;
                    }
                    if (book.getType() == 2){
                        mListBook.add(0, book);
                    }
                }
                displayListFoodSuggest();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                GlobalFunction.showToastMessage(getActivity(), getString(R.string.msg_get_date_error));
            }
        });
    }

    private void goToFoodDetail(@NonNull Book book) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, book);
        GlobalFunction.startActivity(getActivity(), BookDetailActivity.class, bundle);
    }

    private void displayListFoodSuggest() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        binding.rcvFood.setLayoutManager(gridLayoutManager);

        BookGridAdapter mBookGridAdapter = new BookGridAdapter(mListBook, this::goToFoodDetail);
        binding.rcvFood.setAdapter(mBookGridAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTowBinding.inflate(inflater, container, false);
        getListFoodFromFirebase("");
        return binding.getRoot();
    }
}