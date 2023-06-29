package com.pro.bookpro.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.pro.bookpro.ControllerApplication;
import com.pro.bookpro.R;
import com.pro.bookpro.activity.MainActivity;
import com.pro.bookpro.adapter.CartAdapter;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.database.BookDatabase;
import com.pro.bookpro.databinding.FragmentCartBinding;
import com.pro.bookpro.event.ReloadListCartEvent;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.model.Order;
import com.pro.bookpro.model.User;
import com.pro.bookpro.prefs.DataStoreManager;
import com.pro.bookpro.utils.StringUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CartFragment extends BaseFragment {

    private FragmentCartBinding mFragmentCartBinding;
    private CartAdapter mCartAdapter;
    private List<Book> mListBookCart;
    private int mAmount;
    private String name, address, phone;

    private static PayPalConfiguration configuration = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId("ARlUHaIMQG5x1buWvuKQjhPt8lmrvgTkrpM0I5ROlFQRUWuXwFLgvQGRuH7aUbJtAAedM9ease4uvTRO");
    final int postcode = 7171;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFragmentCartBinding = FragmentCartBinding.inflate(inflater, container, false);
        name = "";
        address = "";
        phone = "";
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        displayListFoodInCart();
        mFragmentCartBinding.tvOrderCart.setOnClickListener(v -> onClickOrderCart());
        loadDataCart();
        return mFragmentCartBinding.getRoot();
    }

    private void requestPayment(int amount) {
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(amount)), "USD", "Pay", PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(getContext(), PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, configuration);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent, postcode);
        // ((Activity)context).startActivityForResult();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == postcode) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation paymentConfirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (paymentConfirmation != null) {
                    saveOrder();
                }
            }
        }
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
        name = user.getName();
        address = user.getAddress();
        phone = user.getPhone();
    }

    @Override
    protected void initToolbar() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setToolBar(false, getString(R.string.cart));
        }
    }

    private void displayListFoodInCart() {
        if (getActivity() == null) {
            return;
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mFragmentCartBinding.rcvFoodCart.setLayoutManager(linearLayoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mFragmentCartBinding.rcvFoodCart.addItemDecoration(itemDecoration);

        initDataFoodCart();
    }

    private void initDataFoodCart() {
        mListBookCart = new ArrayList<>();
        mListBookCart = BookDatabase.getInstance(getActivity()).foodDAO().getListFoodCart();
        if (mListBookCart == null || mListBookCart.isEmpty()) {
            return;
        }

        mCartAdapter = new CartAdapter(mListBookCart, new CartAdapter.IClickListener() {
            @Override
            public void clickDeteteFood(Book book, int position) {
                deleteFoodFromCart(book, position);
            }

            @Override
            public void updateItemFood(Book book, int position) {
                BookDatabase.getInstance(getActivity()).foodDAO().updateFood(book);
                mCartAdapter.notifyItemChanged(position);

                calculateTotalPrice();
            }

            @Override
            public void showMessage(String mess) {
                showMessages(mess);
            }
        });
        mFragmentCartBinding.rcvFoodCart.setAdapter(mCartAdapter);

        calculateTotalPrice();
    }

    private void showMessages(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clearCart() {
        if (mListBookCart != null) {
            mListBookCart.clear();
        }
        mCartAdapter.notifyDataSetChanged();
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        List<Book> listBookCart = BookDatabase.getInstance(getActivity()).foodDAO().getListFoodCart();
        if (listBookCart == null || listBookCart.isEmpty()) {
            String strZero = 0 + Constant.CURRENCY;
            mFragmentCartBinding.tvTotalPrice.setText(strZero);
            mAmount = 0;
            return;
        }

        int totalPrice = 0;
        for (Book book : listBookCart) {
            totalPrice = totalPrice + book.getTotalPrice();
        }

        String strTotalPrice = totalPrice + Constant.CURRENCY;
        mFragmentCartBinding.tvTotalPrice.setText(strTotalPrice);
        mAmount = totalPrice;
    }

    private void deleteFoodFromCart(Book book, int position) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.confirm_delete_food))
                .setMessage(getString(R.string.message_delete_food))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    BookDatabase.getInstance(getActivity()).foodDAO().deleteFood(book);
                    mListBookCart.remove(position);
                    mCartAdapter.notifyItemRemoved(position);

                    calculateTotalPrice();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    @SuppressLint("InflateParams") View viewDialog;
    BottomSheetDialog bottomSheetDialog;
    TextView edtNameOrder;
    TextView edtPhoneOrder;
    TextView edtAddressOrder;
    public void onClickOrderCart() {
        if (getActivity() == null) {
            return;
        }

        if (mListBookCart == null || mListBookCart.isEmpty()) {
            return;
        }

        viewDialog = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_order, null);

        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(viewDialog);
        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        // init ui
        TextView tvFoodsOrder = viewDialog.findViewById(R.id.tv_foods_order);
        TextView tvPriceOrder = viewDialog.findViewById(R.id.tv_price_order);
        edtNameOrder = viewDialog.findViewById(R.id.edt_name_order);
        edtPhoneOrder = viewDialog.findViewById(R.id.edt_phone_order);
        edtAddressOrder = viewDialog.findViewById(R.id.edt_address_order);
        TextView tvCancelOrder = viewDialog.findViewById(R.id.tv_cancel_order);
        TextView tvCreateOrder = viewDialog.findViewById(R.id.tv_create_order);

        edtNameOrder.setText(name);

        edtPhoneOrder.setText(phone);
        edtAddressOrder.setText(address);

        // Set data
        tvFoodsOrder.setText(getStringListFoodsOrder(true));
        tvPriceOrder.setText(mFragmentCartBinding.tvTotalPrice.getText().toString());

        // Set listener
        tvCancelOrder.setOnClickListener(v -> bottomSheetDialog.dismiss());

        tvCreateOrder.setOnClickListener(v -> {
            requestPayment(mAmount);
        });

        bottomSheetDialog.show();
    }

    private void saveOrder(){
        String strName = edtNameOrder.getText().toString().trim();
        String strPhone = edtPhoneOrder.getText().toString().trim();
        String strAddress = edtAddressOrder.getText().toString().trim();

        if (StringUtil.isEmpty(strName) || StringUtil.isEmpty(strPhone) || StringUtil.isEmpty(strAddress)) {
            GlobalFunction.showToastMessage(getActivity(), getString(R.string.message_enter_infor_order));
        } else {
            long id = System.currentTimeMillis();
            String strEmail = DataStoreManager.getUser().getEmail();
            Order order = new Order(id, strName, strEmail, strPhone, strAddress,
                    mAmount, getStringListFoodsOrder(false), Constant.TYPE_PAYMENT_CASH, false);
            ControllerApplication.get(getActivity()).getBookingDatabaseReference()
                    .child(String.valueOf(id))
                    .setValue(order, (error1, ref1) -> {
                        GlobalFunction.showToastMessage(getActivity(), getString(R.string.msg_order_success));
                        GlobalFunction.hideSoftKeyboard(getActivity());
                        bottomSheetDialog.dismiss();

                        BookDatabase.getInstance(getActivity()).foodDAO().deleteAllFood();
                        clearCart();
                    });
        }
    }

    private String getStringListFoodsOrder(boolean isOrder) {
        if (mListBookCart == null || mListBookCart.isEmpty()) {
            return "";
        }
        String result = "";
        for (Book book : mListBookCart) {
            if (StringUtil.isEmpty(result)) {
                result = "- " + book.getName() + " (" + book.getRealPrice() + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + book.getCount();
            } else {
                result = result + "\n" + "- " + book.getName() + " (" + book.getRealPrice() + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + book.getCount();
            }
            if (isOrder){
                setSellNumber(book);
            }
        }
        return result;
    }

    private void setSellNumber(Book book) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = ref.child("book");

        usersRef.child(book.getId() + "").child("sellNumber").setValue(book.getSellNumber() + 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReloadListCartEvent event) {
        displayListFoodInCart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
