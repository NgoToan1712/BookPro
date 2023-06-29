package com.pro.bookpro.activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pro.bookpro.R;
import com.pro.bookpro.adapter.MoreImageAdapter;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.database.BookDatabase;
import com.pro.bookpro.databinding.ActivityFoodDetailBinding;
import com.pro.bookpro.event.ReloadListCartEvent;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.utils.GlideUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class BookDetailActivity extends BaseActivity {

    private ActivityFoodDetailBinding mActivityFoodDetailBinding;
    private Book mBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityFoodDetailBinding = ActivityFoodDetailBinding.inflate(getLayoutInflater());
        setContentView(mActivityFoodDetailBinding.getRoot());

        getDataIntent();
        initToolbar();
        setDataFoodDetail();
        initListener();
    }

    private void getDataIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mBook = (Book) bundle.get(Constant.KEY_INTENT_FOOD_OBJECT);
        }
    }

    private void initToolbar() {
        mActivityFoodDetailBinding.toolbar.imgBack.setVisibility(View.VISIBLE);
        mActivityFoodDetailBinding.toolbar.imgCart.setVisibility(View.VISIBLE);
        mActivityFoodDetailBinding.toolbar.tvTitle.setText(getString(R.string.food_detail_title));

        mActivityFoodDetailBinding.toolbar.imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void setDataFoodDetail() {
        if (mBook == null) {
            return;
        }

        GlideUtils.loadUrlBanner(mBook.getBanner(), mActivityFoodDetailBinding.imageFood);
        if (mBook.getSale() <= 0) {
            mActivityFoodDetailBinding.tvSaleOff.setVisibility(View.GONE);
            mActivityFoodDetailBinding.tvPrice.setVisibility(View.GONE);

            String strPrice = mBook.getPrice() + Constant.CURRENCY;
            mActivityFoodDetailBinding.tvPriceSale.setText(strPrice);
        } else {
            mActivityFoodDetailBinding.tvSaleOff.setVisibility(View.VISIBLE);
            mActivityFoodDetailBinding.tvPrice.setVisibility(View.VISIBLE);

            String strSale = "Giảm " + mBook.getSale() + "%";
            mActivityFoodDetailBinding.tvSaleOff.setText(strSale);

            String strPriceOld = mBook.getPrice() + Constant.CURRENCY;
            mActivityFoodDetailBinding.tvPrice.setText(strPriceOld);
            mActivityFoodDetailBinding.tvPrice.setPaintFlags(mActivityFoodDetailBinding.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            String strRealPrice = mBook.getRealPrice() + Constant.CURRENCY;
            mActivityFoodDetailBinding.tvPriceSale.setText(strRealPrice);
        }
        mActivityFoodDetailBinding.tvFoodName.setText(mBook.getName());
        mActivityFoodDetailBinding.tvFoodDescription.setText(mBook.getDescription());
        mActivityFoodDetailBinding.tvAmount.setText(mBook.getAmount() - mBook.getSellNumber() + "");

        displayListMoreImages();

        setStatusButtonAddToCart();
    }

    private void displayListMoreImages() {
        if (mBook.getImages() == null || mBook.getImages().isEmpty()) {
            mActivityFoodDetailBinding.tvMoreImageLabel.setVisibility(View.GONE);
            return;
        }
        mActivityFoodDetailBinding.tvMoreImageLabel.setVisibility(View.GONE);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mActivityFoodDetailBinding.rcvImages.setLayoutManager(gridLayoutManager);

        MoreImageAdapter moreImageAdapter = new MoreImageAdapter(mBook.getImages());
        mActivityFoodDetailBinding.rcvImages.setAdapter(moreImageAdapter);
    }

    private void setStatusButtonAddToCart() {
        if (isFoodInCart()) {
            mActivityFoodDetailBinding.tvAddToCart.setBackgroundResource(R.drawable.bg_gray_shape_corner_6);
            mActivityFoodDetailBinding.tvAddToCart.setText(getString(R.string.added_to_cart));
            mActivityFoodDetailBinding.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
            mActivityFoodDetailBinding.toolbar.imgCart.setVisibility(View.GONE);
        } else {
            mActivityFoodDetailBinding.tvAddToCart.setBackgroundResource(R.drawable.bg_green_shape_corner_6);
            mActivityFoodDetailBinding.tvAddToCart.setText(getString(R.string.add_to_cart));
            mActivityFoodDetailBinding.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.white));
            mActivityFoodDetailBinding.toolbar.imgCart.setVisibility(View.VISIBLE);
        }
    }

    private boolean isFoodInCart() {
        List<Book> list = BookDatabase.getInstance(this).foodDAO().checkFoodInCart(mBook.getId());
        return list != null && !list.isEmpty();
    }

    private void initListener() {
        mActivityFoodDetailBinding.tvAddToCart.setOnClickListener(v -> onClickAddToCart());
        mActivityFoodDetailBinding.toolbar.imgCart.setOnClickListener(v -> onClickAddToCart());
    }

    public void onClickAddToCart() {
        if (mBook.getAmount() - mBook.getSellNumber() == 0) {
            showAlertDialog("Sản phẩm đã hết hàng");
            return;
        }

        if (isFoodInCart()) {
            return;
        }

        //mActivityFoodDetailBinding.tvAmount.setText((mBook.getAmount() - mBook.getSellNumber()) + "");
       // setSellNumber();

        int totalPrice = mBook.getRealPrice();

        mBook.setCount(1);
        mBook.setTotalPrice(totalPrice);

        BookDatabase.getInstance(BookDetailActivity.this).foodDAO().insertFood(mBook);
        setStatusButtonAddToCart();
        EventBus.getDefault().post(new ReloadListCartEvent());
    }

}