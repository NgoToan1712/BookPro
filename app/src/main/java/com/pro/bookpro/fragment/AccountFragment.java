package com.pro.bookpro.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pro.bookpro.R;
import com.pro.bookpro.activity.ChangePasswordActivity;
import com.pro.bookpro.activity.MainActivity;
import com.pro.bookpro.activity.OrderHistoryActivity;
import com.pro.bookpro.activity.SignInActivity;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.database.BookDatabase;
import com.pro.bookpro.databinding.FragmentAccountBinding;
import com.pro.bookpro.model.User;
import com.pro.bookpro.prefs.DataStoreManager;

public class AccountFragment extends BaseFragment {

    FragmentAccountBinding fragmentAccountBinding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentAccountBinding = FragmentAccountBinding.inflate(inflater, container, false);

        fragmentAccountBinding.tvEmail.setText(DataStoreManager.getUser().getEmail());

        fragmentAccountBinding.layoutSignOut.setOnClickListener(v -> onClickSignOut());
        fragmentAccountBinding.layoutChangePassword.setOnClickListener(v -> onClickChangePassword());
        fragmentAccountBinding.layoutOrderHistory.setOnClickListener(v -> onClickOrderHistory());

        loadDataCart();
        return fragmentAccountBinding.getRoot();
    }

    @Override
    protected void initToolbar() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setToolBar(false, getString(R.string.account));
        }
    }

    private void onClickOrderHistory() {
        GlobalFunction.startActivity(getActivity(), OrderHistoryActivity.class);
    }

    private void onClickChangePassword() {
        GlobalFunction.startActivity(getActivity(), ChangePasswordActivity.class);
    }

    private void loadDataCart() {
        FirebaseDatabase.getInstance(Constant.FIREBASE_URL).getReference().child("User")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (user.getId().equals(DataStoreManager.getUser().getId())) {
                                setDataUser(user);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void setDataUser(User user) {
        fragmentAccountBinding.tvName.setText(user.getName());
        fragmentAccountBinding.tvAddress.setText(user.getAddress());
        fragmentAccountBinding.tvPhone.setText(user.getPhone());
    }


    private void onClickSignOut() {
        if (getActivity() == null) {
            return;
        }
        BookDatabase.getInstance(getActivity()).foodDAO().deleteAllFood();
        FirebaseAuth.getInstance().signOut();
        DataStoreManager.setUser(null);
        GlobalFunction.startActivity(getActivity(), SignInActivity.class);
        getActivity().finishAffinity();
    }
}
