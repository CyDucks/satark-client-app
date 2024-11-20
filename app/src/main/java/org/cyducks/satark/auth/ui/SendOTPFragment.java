package org.cyducks.satark.auth.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.cyducks.satark.R;
import org.cyducks.satark.databinding.FragmentSendOtpBinding;
import org.cyducks.satark.util.FirebaseAuthUtil;

import java.util.concurrent.TimeUnit;

public class SendOTPFragment extends Fragment {

    private static final String TAG = "SendOTPFragment";
    FragmentSendOtpBinding viewBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentSendOtpBinding.inflate(getLayoutInflater());

        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // Simulate successful verification
                FirebaseAuthUtil.signInWithPhoneNumber(phoneAuthCredential);
                Toast.makeText(requireActivity(), "Verification Done (Bypassed OTP)", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(viewBinding.getRoot()).navigate(R.id.loginFragment);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(requireActivity(), "Invalid Phone number or request", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onVerificationFailed: " + e.getMessage());
                viewBinding.sendOtpProgress.setVisibility(View.GONE);
                Navigation.findNavController(viewBinding.getRoot()).popBackStack();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);

                // Bypass OTP entry by creating a dummy PhoneAuthCredential
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, "123456");
                FirebaseAuthUtil.signInWithPhoneNumber(credential);

                // Pass the data to the next fragment
                Bundle bundle = new Bundle();
                bundle.putString("verificationId", verificationId);

                String name = getArguments().getString("name");
                String email = getArguments().getString("email");
                String password = getArguments().getString("password");

                if (name == null || email == null || password == null) {
                    Toast.makeText(requireActivity(), "Credentials not received in send OTP fragment", Toast.LENGTH_SHORT).show();
                    return;
                }

                bundle.putString("name", name);
                bundle.putString("email", email);
                bundle.putString("password", password);
                bundle.putString("phone", viewBinding.mobileNo.getText().toString());

                Toast.makeText(requireActivity(), "OTP Bypassed. Navigating to verification...", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(viewBinding.getRoot())
                        .navigate(R.id.action_sendOTPFragment_to_OTPVerificationFragment, bundle);
            }
        };

        viewBinding.sendOtp.setOnClickListener(v -> {
            if (viewBinding.mobileNo.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "Enter the phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null && user.getPhoneNumber() != null && user.getPhoneNumber().equals(viewBinding.mobileNo.getText().toString())) {
                Toast.makeText(requireActivity(), "A user with this phone number already exists!", Toast.LENGTH_SHORT).show();
                return;
            }

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                    .setPhoneNumber("+91" + viewBinding.mobileNo.getText().toString())
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setCallbacks(callbacks)
                    .setActivity(requireActivity())
                    .build();

            viewBinding.sendOtpProgress.setVisibility(View.VISIBLE);
            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        return viewBinding.getRoot();
    }
}
