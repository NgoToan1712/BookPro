package com.pro.bookpro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pro.bookpro.databinding.ItemFoodPopularBinding;
import com.pro.bookpro.listener.IOnClickFoodItemListener;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.utils.GlideUtils;

import java.util.List;

public class BookPopularAdapter extends RecyclerView.Adapter<BookPopularAdapter.FoodPopularViewHolder> {

    private final List<Book> mListBooks;
    public final IOnClickFoodItemListener iOnClickFoodItemListener;

    public BookPopularAdapter(List<Book> mListBooks, IOnClickFoodItemListener iOnClickFoodItemListener) {
        this.mListBooks = mListBooks;
        this.iOnClickFoodItemListener = iOnClickFoodItemListener;
    }

    @NonNull
    @Override
    public FoodPopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodPopularBinding itemFoodPopularBinding = ItemFoodPopularBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodPopularViewHolder(itemFoodPopularBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodPopularViewHolder holder, int position) {
        Book book = mListBooks.get(position);
        if (book == null) {
            return;
        }
        GlideUtils.loadUrlBanner(book.getBanner(), holder.mItemFoodPopularBinding.imageFood);
        if (book.getSale() <= 0) {
            holder.mItemFoodPopularBinding.tvSaleOff.setVisibility(View.GONE);
        } else {
            holder.mItemFoodPopularBinding.tvSaleOff.setVisibility(View.VISIBLE);
            String strSale = "Giảm " + book.getSale() + "%";
            holder.mItemFoodPopularBinding.tvSaleOff.setText(strSale);
        }
        holder.mItemFoodPopularBinding.layoutItem.setOnClickListener(v -> iOnClickFoodItemListener.onClickItemFood(book));
    }

    @Override
    public int getItemCount() {
        if (mListBooks != null) {
            return mListBooks.size();
        }
        return 0;
    }

    public static class FoodPopularViewHolder extends RecyclerView.ViewHolder {

        private final ItemFoodPopularBinding mItemFoodPopularBinding;

        public FoodPopularViewHolder(@NonNull ItemFoodPopularBinding itemFoodPopularBinding) {
            super(itemFoodPopularBinding.getRoot());
            this.mItemFoodPopularBinding = itemFoodPopularBinding;
        }
    }
}
