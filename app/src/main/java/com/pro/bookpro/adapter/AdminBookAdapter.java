package com.pro.bookpro.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.databinding.ItemAdminFoodBinding;
import com.pro.bookpro.listener.IOnManagerFoodListener;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.utils.GlideUtils;

import java.util.List;

public class AdminBookAdapter extends RecyclerView.Adapter<AdminBookAdapter.AdminFoodViewHolder> {

    private final List<Book> mListBooks;
    public final IOnManagerFoodListener iOnManagerFoodListener;

    public AdminBookAdapter(List<Book> mListBooks, IOnManagerFoodListener listener) {
        this.mListBooks = mListBooks;
        this.iOnManagerFoodListener = listener;
    }

    @NonNull
    @Override
    public AdminFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminFoodBinding itemAdminFoodBinding = ItemAdminFoodBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminFoodViewHolder(itemAdminFoodBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminFoodViewHolder holder, int position) {
        Book book = mListBooks.get(position);
        if (book == null) {
            return;
        }
        GlideUtils.loadUrl(book.getImage(), holder.mItemAdminFoodBinding.imgFood);
        if (book.getSale() <= 0) {
            holder.mItemAdminFoodBinding.tvSaleOff.setVisibility(View.GONE);
            holder.mItemAdminFoodBinding.tvPrice.setVisibility(View.GONE);

            String strPrice = book.getPrice() + Constant.CURRENCY;
            holder.mItemAdminFoodBinding.tvPriceSale.setText(strPrice);
        } else {
            holder.mItemAdminFoodBinding.tvSaleOff.setVisibility(View.VISIBLE);
            holder.mItemAdminFoodBinding.tvPrice.setVisibility(View.VISIBLE);

            String strSale = "Giảm " + book.getSale() + "%";
            holder.mItemAdminFoodBinding.tvSaleOff.setText(strSale);

            String strOldPrice = book.getPrice() + Constant.CURRENCY;
            holder.mItemAdminFoodBinding.tvPrice.setText(strOldPrice);
            holder.mItemAdminFoodBinding.tvPrice.setPaintFlags(holder.mItemAdminFoodBinding.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            String strRealPrice = book.getRealPrice() + Constant.CURRENCY;
            holder.mItemAdminFoodBinding.tvPriceSale.setText(strRealPrice);
        }

        holder.mItemAdminFoodBinding.tvSellAmount.setText(String.valueOf(book.getSellNumber()));
        holder.mItemAdminFoodBinding.tvFoodName.setText(book.getName());
        holder.mItemAdminFoodBinding.tvFoodDescription.setText(book.getDescription());

        if (book.isPopular()) {
            holder.mItemAdminFoodBinding.tvPopular.setText("Có");
        } else {
            holder.mItemAdminFoodBinding.tvPopular.setText("Không");
        }

        holder.mItemAdminFoodBinding.imgEdit.setOnClickListener(v -> iOnManagerFoodListener.onClickUpdateFood(book));
        holder.mItemAdminFoodBinding.imgDelete.setOnClickListener(v -> iOnManagerFoodListener.onClickDeleteFood(book));
    }

    @Override
    public int getItemCount() {
        return null == mListBooks ? 0 : mListBooks.size();
    }

    public static class AdminFoodViewHolder extends RecyclerView.ViewHolder {

        private final ItemAdminFoodBinding mItemAdminFoodBinding;

        public AdminFoodViewHolder(ItemAdminFoodBinding itemAdminFoodBinding) {
            super(itemAdminFoodBinding.getRoot());
            this.mItemAdminFoodBinding = itemAdminFoodBinding;
        }
    }
}
