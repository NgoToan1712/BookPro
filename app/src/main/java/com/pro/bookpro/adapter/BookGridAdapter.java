package com.pro.bookpro.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.databinding.ItemFoodGridBinding;
import com.pro.bookpro.listener.IOnClickFoodItemListener;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.utils.GlideUtils;

import java.util.List;

public class BookGridAdapter extends RecyclerView.Adapter<BookGridAdapter.FoodGridViewHolder> {

    private final List<Book> mListBooks;
    public final IOnClickFoodItemListener iOnClickFoodItemListener;

    public BookGridAdapter(List<Book> mListBooks, IOnClickFoodItemListener iOnClickFoodItemListener) {
        this.mListBooks = mListBooks;
        this.iOnClickFoodItemListener = iOnClickFoodItemListener;
    }

    @NonNull
    @Override
    public FoodGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodGridBinding itemFoodGridBinding = ItemFoodGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodGridViewHolder(itemFoodGridBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodGridViewHolder holder, int position) {
        Book book = mListBooks.get(position);
        if (book == null) {
            return;
        }
        GlideUtils.loadUrl(book.getImage(), holder.mItemFoodGridBinding.imgFood);
        if (book.getSale() <= 0) {
            holder.mItemFoodGridBinding.tvSaleOff.setVisibility(View.GONE);
            holder.mItemFoodGridBinding.tvPrice.setVisibility(View.GONE);

            String strPrice = book.getPrice() + Constant.CURRENCY;
            holder.mItemFoodGridBinding.tvPriceSale.setText(strPrice);
        } else {
            holder.mItemFoodGridBinding.tvSaleOff.setVisibility(View.VISIBLE);
            holder.mItemFoodGridBinding.tvPrice.setVisibility(View.VISIBLE);

            String strSale = "Giảm " + book.getSale() + "%";
            holder.mItemFoodGridBinding.tvSaleOff.setText(strSale);

            String strOldPrice = book.getPrice() + Constant.CURRENCY;
            holder.mItemFoodGridBinding.tvPrice.setText(strOldPrice);
            holder.mItemFoodGridBinding.tvPrice.setPaintFlags(holder.mItemFoodGridBinding.tvPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            String strRealPrice = book.getRealPrice() + Constant.CURRENCY;
            holder.mItemFoodGridBinding.tvPriceSale.setText(strRealPrice);
        }
        holder.mItemFoodGridBinding.tvFoodName.setText(book.getName());

        holder.mItemFoodGridBinding.layoutItem.setOnClickListener(v -> iOnClickFoodItemListener.onClickItemFood(book));
    }

    @Override
    public int getItemCount() {
        return null == mListBooks ? 0 : mListBooks.size();
    }

    public static class FoodGridViewHolder extends RecyclerView.ViewHolder {

        private final ItemFoodGridBinding mItemFoodGridBinding;

        public FoodGridViewHolder(ItemFoodGridBinding itemFoodGridBinding) {
            super(itemFoodGridBinding.getRoot());
            this.mItemFoodGridBinding = itemFoodGridBinding;
        }
    }
}
