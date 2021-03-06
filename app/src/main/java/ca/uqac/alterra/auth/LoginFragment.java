package ca.uqac.alterra.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import ca.uqac.alterra.R;
import ca.uqac.alterra.database.AlterraAuth;
import ca.uqac.alterra.database.AlterraCloud;
import ca.uqac.alterra.types.AlterraUser;
import ca.uqac.alterra.database.exceptions.AlterraAuthException;
import ca.uqac.alterra.database.exceptions.AlterraAuthUserCollisionException;


public class LoginFragment extends Fragment {

    private TextInputEditText mEmailEditText;
    private TextInputLayout mEmailTextInput;
    private TextInputEditText mPasswordEditText;
    private TextInputLayout mPasswordTextInput;

    private LoginListener mListener;

    private AlterraAuth mAuth;

    static LoginFragment newInstance() {
        
        Bundle args = new Bundle();
        
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = AlterraCloud.getAuthInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mPasswordTextInput = view.findViewById(R.id.passwordTextInput);
        mPasswordEditText = view.findViewById(R.id.passwordEditText);
        mPasswordEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                verifyFields();
                return true;
            }
            return false; // pass on to other listeners.
        });

        mEmailTextInput = view.findViewById(R.id.emailTextInput);
        mEmailEditText = view.findViewById(R.id.emailEditText);

        setTextWatcher(mEmailEditText,mEmailTextInput);
        setTextWatcher(mPasswordEditText,mPasswordTextInput);

        MaterialButton loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> verifyFields());

        MaterialButton registerButton = view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> mListener.onRegisterRequested());



        Button googleButton = view.findViewById(R.id.googleButton);
        googleButton.setOnClickListener((v) -> mAuth.logInWithGoogle(this, new AlterraAuth.AlterraAuthListener() {
            @Override
            public void onSuccess(AlterraUser user) {
                AlterraCloud.getDatabaseInstance().registerAlterraUser(user.getUID(),user.getEmail(),null);
                mListener.onLoginSuccessful();
            }

            @Override
            public void onFailure(AlterraAuthException e) {
                if (e instanceof AlterraAuthUserCollisionException){
                    Toast.makeText(getContext(), R.string.auth_exception_user_collision_long,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),R.string.auth_login_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        }));

        Button facebookButton = view.findViewById(R.id.facebookButton);
        facebookButton.setOnClickListener((v) -> mAuth.logInWithFacebook(this, new AlterraAuth.AlterraAuthListener() {
            @Override
            public void onSuccess(AlterraUser user) {
                AlterraCloud.getDatabaseInstance().registerAlterraUser(user.getUID(),user.getEmail(),null);
                mListener.onLoginSuccessful();
            }

            @Override
            public void onFailure(AlterraAuthException e) {
                if (e instanceof AlterraAuthUserCollisionException){
                    Toast.makeText(getContext(), R.string.auth_exception_user_collision_long,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), R.string.auth_login_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        }));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //DEBUG: For testing purposes, signOut User on application startup
        //mAuth.signOut();
        //LoginManager.getInstance().logOut();
        //mGoogleSignInClient.revokeAccess();

        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser()!=null){
            mListener.onLoginSuccessful();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mAuth.getCallback().onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

    }

    void setLoginListener(LoginListener listener) { mListener = listener; }


    private void verifyFields(){
        String email = Objects.requireNonNull(mEmailEditText.getText()).toString();
        String password = Objects.requireNonNull(mPasswordEditText.getText()).toString();

        boolean isValid = true;

        if(email.length() <1){
            mEmailTextInput.setError(getString(R.string.auth_enter_email));
            isValid = false;
        }

        if(password.length() <1){
            mPasswordTextInput.setError(getString(R.string.auth_enter_password));
            isValid = false;
        }

        if (isValid)
            mAuth.logInWithPassword(email, password, new AlterraAuth.AlterraAuthListener() {
                @Override
                public void onSuccess(AlterraUser user) {
                    mListener.onLoginSuccessful();
                }

                @Override
                public void onFailure(AlterraAuthException e) {
                    Context context = getContext();
                    if (context != null) {
                        new MaterialAlertDialogBuilder(context, R.style.DialogStyle)
                                .setTitle(R.string.auth_login_failed)
                                .setPositiveButton(R.string.auth_generic_positive_answer, null)
                                .show();
                    }
                }
            });
    }



    private void setTextWatcher(TextInputEditText editText, TextInputLayout inputLayout){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                inputLayout.setErrorEnabled(false);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mEmailTextInput.setErrorEnabled(false);
        mPasswordTextInput.setErrorEnabled(false);
    }

    public interface LoginListener {
        void onLoginSuccessful();
        void onRegisterRequested();
    }

}
