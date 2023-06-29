package com.pro.bookpro.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pro.bookpro.ControllerApplication;
import com.pro.bookpro.R;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.databinding.ActivityAddFoodBinding;
import com.pro.bookpro.model.Book;
import com.pro.bookpro.model.BookObject;
import com.pro.bookpro.model.Image;
import com.pro.bookpro.utils.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gun0912.tedimagepicker.builder.TedImagePicker;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class AddBookActivity extends BaseActivity {

    private ActivityAddFoodBinding mActivityAddFoodBinding;
    private boolean isUpdate;
    private Book mBook;
    private String[] arr = {"GIÁO TRÌNH","TRUYỆN","VĂN HỌC NGHỆ THUẬT", "KHÁC"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityAddFoodBinding = ActivityAddFoodBinding.inflate(getLayoutInflater());
        setContentView(mActivityAddFoodBinding.getRoot());

        getDataIntent();
        initToolbar();
        initView();

        mActivityAddFoodBinding.btnAddOrEdit.setOnClickListener(v -> addOrEditFood());
        mActivityAddFoodBinding.imgAddProduct.setOnClickListener(view -> showGallery());
        mActivityAddFoodBinding.imgAddProductBanner.setOnClickListener(view -> showGalleryBanner());

    }

    private void showGalleryBanner() {
        TedImagePicker.with(this)
                .showCameraTile(false)
                .dropDownAlbum()
                .start(new Function1<Uri, Unit>() {
                    @Override
                    public Unit invoke(Uri uri) {
                        mActivityAddFoodBinding.edtImageBanner.setText(uri.toString());
                        setImageProductBanner(uri);
                        //uploadImage(uri);
                        return null;
                    }
                });
    }

    private void showGallery() {
        TedImagePicker.with(this)
                .showCameraTile(false)
                .dropDownAlbum()
                .start(new Function1<Uri, Unit>() {
                    @Override
                    public Unit invoke(Uri uri) {
                        mActivityAddFoodBinding.edtImage.setText(uri.toString());
                        setImageProduct(uri);
                        //uploadImage(uri);
                        return null;
                    }
                });
    }

    private void setImageProduct(Uri uri) {
        Glide.with(this).load(uri).into(mActivityAddFoodBinding.imgAddProduct);
    }

    private void setImageProductBanner(Uri uri) {
        Glide.with(this).load(uri).into(mActivityAddFoodBinding.imgAddProductBanner);
    }

    private Observable<String> uploadImageToFirestore(String imageUrl) {
        return Observable.create(emitter -> {
            if (imageUrl.contains("https")){
                emitter.onNext(imageUrl);
                emitter.onComplete();
                return;
            }
            String urlFile = new Date().getTime() + ".png";
            final StorageReference ref = FirebaseStorage.getInstance()
                    .getReference("/image_product").child(urlFile);
            UploadTask uploadTask = ref.putFile(Uri.parse(imageUrl));
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        emitter.onNext(downloadUri.toString());
                        emitter.onComplete();
                    } else {
                        Log.d("asdasdasdas", task.getException().toString());
                    }
                }
            });
        });
    }


    public void uploadImage(Uri uri) {
        String urlFile = new Date().getTime() + ".png";
        final StorageReference ref = FirebaseStorage.getInstance()
                .getReference("/image_product").child(urlFile);
        UploadTask uploadTask = ref.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    Log.d("asdasdasdas", task.getException().toString());
                }
            }
        });
    }

    private void getDataIntent() {
        Bundle bundleReceived = getIntent().getExtras();
        if (bundleReceived != null) {
            isUpdate = true;
            mBook = (Book) bundleReceived.get(Constant.KEY_INTENT_FOOD_OBJECT);
        }
    }

    private void initToolbar() {
        mActivityAddFoodBinding.toolbar.imgBack.setVisibility(View.VISIBLE);
        mActivityAddFoodBinding.toolbar.imgCart.setVisibility(View.GONE);

        mActivityAddFoodBinding.toolbar.imgBack.setOnClickListener(v -> onBackPressed());
    }

    private void initView() {
        if (isUpdate) {
            mActivityAddFoodBinding.toolbar.tvTitle.setText(getString(R.string.edit_food));
            mActivityAddFoodBinding.btnAddOrEdit.setText(getString(R.string.action_edit));
            setImageProduct(Uri.parse(mBook.getImage()));
            setImageProductBanner(Uri.parse(mBook.getBanner()));

            mActivityAddFoodBinding.edtName.setText(mBook.getName());
            mActivityAddFoodBinding.edtDescription.setText(mBook.getDescription());
            mActivityAddFoodBinding.edtPrice.setText(String.valueOf(mBook.getPrice()));
            mActivityAddFoodBinding.edtDiscount.setText(String.valueOf(mBook.getSale()));
            mActivityAddFoodBinding.edtImage.setText(mBook.getImage());
            mActivityAddFoodBinding.edtImageBanner.setText(mBook.getBanner());
            mActivityAddFoodBinding.chbPopular.setChecked(mBook.isPopular());
            mActivityAddFoodBinding.edtOtherImage.setText(getTextOtherImages());
        } else {
            mActivityAddFoodBinding.toolbar.tvTitle.setText(getString(R.string.add_food));
            mActivityAddFoodBinding.btnAddOrEdit.setText(getString(R.string.action_add));
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item,arr);
        mActivityAddFoodBinding.spnType.setAdapter(adapter);
    }

    private String getTextOtherImages() {
        String result = "";
        if (mBook == null || mBook.getImages() == null || mBook.getImages().isEmpty()) {
            return result;
        }
        for (Image image : mBook.getImages()) {
            if (StringUtil.isEmpty(result)) {
                result = result + image.getUrl();
            } else {
                result = result + ";" + image.getUrl();
            }
        }
        return result;
    }

    @SuppressLint("CheckResult")
    private void addOrEditFood() {
        String strName = mActivityAddFoodBinding.edtName.getText().toString().trim();
        String strDescription = mActivityAddFoodBinding.edtDescription.getText().toString().trim();
        String strPrice = mActivityAddFoodBinding.edtPrice.getText().toString().trim();
        String strDiscount = mActivityAddFoodBinding.edtDiscount.getText().toString().trim();
        String amount = mActivityAddFoodBinding.edtAmount.getText().toString().trim();
        mActivityAddFoodBinding.edtDiscount.setText(strDiscount);
        String strImage = mActivityAddFoodBinding.edtImage.getText().toString().trim();
        String strImageBanner = mActivityAddFoodBinding.edtImageBanner.getText().toString().trim();
        boolean isPopular = mActivityAddFoodBinding.chbPopular.isChecked();
        String strOtherImages = mActivityAddFoodBinding.edtOtherImage.getText().toString().trim();
        List<Image> listImages = new ArrayList<>();
        if (!StringUtil.isEmpty(strOtherImages)) {
            String[] temp = strOtherImages.split(";");
            for (String strUrl : temp) {
                Image image = new Image(strUrl);
                listImages.add(image);
            }
        }

        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strDescription)) {
            Toast.makeText(this, getString(R.string.msg_description_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(amount)) {
            Toast.makeText(this, getString(R.string.msg_amount_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strDiscount)) {
            Toast.makeText(this, getString(R.string.msg_discount_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        if (StringUtil.isEmpty(strImageBanner)) {
            Toast.makeText(this, getString(R.string.msg_image_banner_food_require), Toast.LENGTH_SHORT).show();
            return;
        }

        String[] imageUrls = new String[]{strImage, strImageBanner};

        int type = 1;
        if (mActivityAddFoodBinding.spnType.getSelectedItem().toString().equals("GIÁO TRÌNH")){
             type = 1;
        }else if(mActivityAddFoodBinding.spnType.getSelectedItem().toString().equals("TRUYỆN")){
            type = 2;

        }else if(mActivityAddFoodBinding.spnType.getSelectedItem().toString().equals("VĂN HỌC NGHỆ THUẬT")){
            type = 3;

        }else{
            type = 4;
        }

        showProgressDialog(true);
        int finalType = type;
        Observable.fromArray(imageUrls)
                .flatMap(url -> uploadImageToFirestore(url).subscribeOn(Schedulers.io()))
                .toList()
                .subscribe(urls -> {
                    addOrEditProductToServer(strName, strDescription, strPrice, urls.get(0), urls.get(1),amount ,isPopular, finalType);
                }, throwable -> {
                    // Xử lý khi có lỗi xảy ra
                    throwable.printStackTrace();
                });
    }

    private void addOrEditProductToServer(String strName,
                                          String strDescription,
                                          String strPrice,
                                          String strImage,
                                          String strImageBanner,
                                          String amount,
                                          boolean isPopular,
                                          int type) {
        String strDiscount = mActivityAddFoodBinding.edtDiscount.getText().toString().trim();

        // Update food
        if (isUpdate) {
            showProgressDialog(true);
            Map<String, Object> map = new HashMap<>();
            map.put("name", strName);
            map.put("description", strDescription);
            map.put("price", Integer.parseInt(strPrice));
            map.put("sale", Integer.parseInt(strDiscount));
            map.put("amount", Integer.parseInt(amount));
            map.put("image", strImage);
            map.put("banner", strImageBanner);
            map.put("popular", isPopular);
            map.put("type", type);

            ControllerApplication.get(this).getFoodDatabaseReference()
                    .child(String.valueOf(mBook.getId())).updateChildren(map, (error, ref) -> {
                        showProgressDialog(false);
                        Toast.makeText(AddBookActivity.this,
                                getString(R.string.msg_edit_food_success), Toast.LENGTH_SHORT).show();
                        GlobalFunction.hideSoftKeyboard(this);
                    });
            return;
        }

        // Add food
        showProgressDialog(true);
        long foodId = System.currentTimeMillis();
        BookObject food = new BookObject(foodId, strName, strDescription, Integer.parseInt(strPrice),
                Integer.parseInt(strDiscount), strImage, strImageBanner, isPopular, type);
        food.setAmount(Integer.parseInt(amount));

        ControllerApplication.get(this).getFoodDatabaseReference()
                .child(String.valueOf(foodId)).setValue(food, (error, ref) -> {
                    showProgressDialog(false);
                    mActivityAddFoodBinding.edtName.setText("");
                    mActivityAddFoodBinding.edtDescription.setText("");
                    mActivityAddFoodBinding.edtPrice.setText("");
                    mActivityAddFoodBinding.edtDiscount.setText("");
                    mActivityAddFoodBinding.edtImage.setText("");
                    mActivityAddFoodBinding.edtImageBanner.setText("");
                    mActivityAddFoodBinding.chbPopular.setChecked(false);
                    mActivityAddFoodBinding.edtOtherImage.setText("");
                    GlobalFunction.hideSoftKeyboard(this);
                    Toast.makeText(this, getString(R.string.msg_add_food_success), Toast.LENGTH_SHORT).show();
                });

    }
}