package com.pro.bookpro.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.databinding.ItemCartBinding;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.utils.GlideUtils;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<Book> mListBooks;
    private final IClickListener iClickListener;

    public interface IClickListener {
        void clickDeteteFood(Book book, int position);

        void updateItemFood(Book book, int position);

        void showMessage(String mess);

    }

    public CartAdapter(List<Book> mListBooks, IClickListener iClickListener) {
        this.mListBooks = mListBooks;
        this.iClickListener = iClickListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding itemCartBinding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(itemCartBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Book book = mListBooks.get(position);
        if (book == null) {
            return;
        }
        GlideUtils.loadUrl(book.getImage(), holder.mItemCartBinding.imgFoodCart);
        holder.mItemCartBinding.tvFoodNameCart.setText(book.getName());


        String strFoodPriceCart = book.getPrice() + Constant.CURRENCY;
        if (book.getSale() > 0) {
            strFoodPriceCart = book.getRealPrice() + Constant.CURRENCY;
        }
        holder.mItemCartBinding.tvFoodPriceCart.setText(strFoodPriceCart);
        holder.mItemCartBinding.tvCount.setText(String.valueOf(book.getCount()));

        holder.mItemCartBinding.tvSubtract.setOnClickListener(v -> {
            String strCount = holder.mItemCartBinding.tvCount.getText().toString();
            int count = Integer.parseInt(strCount);
            if (count <= 1) {
                return;
            }
            int newCount = count - 1;
            holder.mItemCartBinding.tvCount.setText(String.valueOf(newCount));

            int totalPrice = book.getRealPrice() * newCount;
            book.setCount(newCount);
            book.setTotalPrice(totalPrice);

            iClickListener.updateItemFood(book, holder.getAdapterPosition());
        });

        holder.mItemCartBinding.tvAdd.setOnClickListener(v -> {

            int newCount = Integer.parseInt(holder.mItemCartBinding.tvCount.getText().toString()) + 1;
            if (book.getAmount() - book.getSellNumber() - newCount < 0){
                iClickListener.showMessage("Sản phẩm đã vượt quá số lượng trong kho");
                return;
            }
            holder.mItemCartBinding.tvCount.setText(String.valueOf(newCount));

            int totalPrice = book.getRealPrice() * newCount;
            book.setCount(newCount);
            book.setTotalPrice(totalPrice);

            iClickListener.updateItemFood(book, holder.getAdapterPosition());
        });

        holder.mItemCartBinding.tvDelete.setOnClickListener(v
                -> iClickListener.clickDeteteFood(book, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return null == mListBooks ? 0 : mListBooks.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        private final ItemCartBinding mItemCartBinding;

        public CartViewHolder(ItemCartBinding itemCartBinding) {
            super(itemCartBinding.getRoot());
            this.mItemCartBinding = itemCartBinding;
        }
    }
}
