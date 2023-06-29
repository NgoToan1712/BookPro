package com.pro.bookpro.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pro.bookpro.model.Book;

import java.util.List;

@Dao
public interface BookDAO {

    @Insert
    void insertFood(Book book);

    @Query("SELECT * FROM Book")
    List<Book> getListFoodCart();

    @Query("SELECT * FROM Book WHERE id=:id")
    List<Book> checkFoodInCart(long id);

    @Delete
    void deleteFood(Book book);

    @Update
    void updateFood(Book book);

    @Query("DELETE from Book")
    void deleteAllFood();
}
