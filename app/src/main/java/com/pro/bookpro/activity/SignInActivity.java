package com.pro.bookpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.pro.bookpro.R;
import com.pro.bookpro.constant.Constant;
import com.pro.bookpro.constant.GlobalFunction;
import com.pro.bookpro.databinding.ActivitySignInBinding;
import com.pro.bookpro.model.User;
import com.pro.bookpro.prefs.DataStoreManager;
import com.pro.bookpro.utils.StringUtil;

public class SignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleSignInClient googleSignInClient;
    public static int REQUEST_CODE_LOGIN_GOOGLE = 3;
    public static int CHECK_AUTH_PROVIDER_LOGIN = 0;
    private ActivitySignInBinding mActivitySignInBinding;

    static GoogleApiClient apiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivitySignInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(mActivitySignInBinding.getRoot());

        mActivitySignInBinding.rdbUser.setChecked(true);

        mActivitySignInBinding.layoutSignUp.setOnClickListener(
                v -> GlobalFunction.startActivity(SignInActivity.this, SignUpActivity.class));

        mActivitySignInBinding.btnSignIn.setOnClickListener(v -> onClickValidateSignIn());
        mActivitySignInBinding.tvForgotPassword.setOnClickListener(v -> onClickForgotPassword());

        initGoogleSignInClient();

        mActivitySignInBinding.btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginGoogle(apiClient);
            }
        });
    }

    private void initGoogleSignInClient() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void LoginGoogle(GoogleApiClient apiClient){
        CHECK_AUTH_PROVIDER_LOGIN = 1;
        signInWithGoogle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN_GOOGLE){
            if (resultCode == RESULT_OK){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            createUser(task.getResult().getUser().getUid(),task.getResult().getUser().getDisplayName(),task.getResult().getUser().getEmail());
                        }
                    }
                });
    }

    private void createUser(String uid, String displayName, String email) {
        User userObject = new User(uid,
                displayName,
                "",
                "",
                email,
                "no",
                false);
        FirebaseDatabase.getInstance(Constant.FIREBASE_URL).getReference().child("User").child(uid).setValue(userObject);
        DataStoreManager.setUser(userObject);
        GlobalFunction.gotoMainActivity(this);
        finishAffinity();
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_LOGIN_GOOGLE);
    }

    private void onClickForgotPassword() {
        GlobalFunction.startActivity(this, ForgotPasswordActivity.class);
    }

    private void onClickValidateSignIn() {
        String strEmail = mActivitySignInBinding.edtEmail.getText().toString().trim();
        String strPassword = mActivitySignInBinding.edtPassword.getText().toString().trim();
        if (StringUtil.isEmpty(strEmail)) {
            Toast.makeText(SignInActivity.this, getString(R.string.msg_email_require), Toast.LENGTH_SHORT).show();
        } else if (StringUtil.isEmpty(strPassword)) {
            Toast.makeText(SignInActivity.this, getString(R.string.msg_password_require), Toast.LENGTH_SHORT).show();
        } else if (!StringUtil.isValidEmail(strEmail)) {
            Toast.makeText(SignInActivity.this, getString(R.string.msg_email_invalid), Toast.LENGTH_SHORT).show();
        } else {

                signInUser(strEmail, strPassword);
        }
    }

    private void signInUser(String email, String password) {
        showProgressDialog(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgressDialog(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            User userObject = new User(user.getEmail(), password);
                            userObject.setId(user.getUid());
                            if (user.getEmail() != null && user.getEmail().contains(Constant.ADMIN_EMAIL_FORMAT)) {
                                userObject.setAdmin(true);
                            }
                            DataStoreManager.setUser(userObject);
                            GlobalFunction.gotoMainActivity(this);
                            finishAffinity();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, getString(R.string.msg_sign_in_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}