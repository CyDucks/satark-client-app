package org.cyducks.satark.registration.worker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.cyducks.satark.util.ListenableWorkerAdapter;
import org.cyducks.satark.util.NetworkResultCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserRegistrationWorker extends ListenableWorkerAdapter {
    private static final String TAG = "MYAPP";
    private final FirebaseFirestore dbInstance;
    private final FirebaseStorage storage;

    public UserRegistrationWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        dbInstance = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    @Override
    public void doAsyncBackgroundTask(NetworkResultCallback callback) {
        FirebaseFirestore dbInstance = FirebaseFirestore.getInstance();

        try {
            String role = getInputData().getString("role");
            String userId = getInputData().getString("userId");
            String aadhaarNumber = getInputData().getString("aadhaar_number");
            String aadhaarUriString = getInputData().getString("aadhaarUriString");

            if(role == null || userId == null) {
                for (String key :
                        getInputData().getKeyValueMap().keySet()) {
                    Log.d(TAG, "userRegistrationWorker: " + key);
                }
                throw new IllegalArgumentException("userId or role not provided.");
            }
            String collection = "";
            Map<String, Object> inputData = new HashMap<>();
            switch (role) {
                case "moderator":
                    collection = "moderators";
                    if(aadhaarNumber == null || aadhaarUriString == null) {
                        throw new IllegalArgumentException("aadhaar number or uri not provided");
                    }
                    inputData.put("user_id", userId);
                    inputData.put("aadhaar_number", aadhaarNumber);
                    
                    StorageReference rootRef = storage.getReference();
                    StorageReference aadhaarRef = rootRef.child(String.format("users/%s/%s/docs/aadhaar_card.pdf", role, userId));
                    
                    aadhaarRef.putFile(Uri.parse(aadhaarUriString))
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()) {
                                    String ref = task.getResult().getStorage().toString();
                                    
                                    inputData.put("aadhaar_storage_ref", ref);
                                    upsert(callback, "moderators", inputData);

                                } else {
                                    Log.d(TAG, "UserRegistrationWorker: " + task.getException());
                                    callback.onFailure(task.getException());
                                }
                            });
                    
                    break;
                case "civilian":
                    collection = "civilians";
                    inputData.put("user_id", userId);

                    upsert(callback, "civilians", inputData);
                    break;
                default:
                    throw new IllegalArgumentException("provided role is invalid");
            }

        }catch (Exception e) {
            callback.onFailure(e);
        }

    }

    private void upsert(NetworkResultCallback callback, String finalCollection, Map<String, Object> inputData) {
        String uid = UUID.randomUUID().toString();

        dbInstance.collection(finalCollection)
                .document(uid)
                .set(inputData)
                .addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful())  {
                        callback.onSuccess(new Data.Builder().putString("uid", uid).build());
                    }
                    else{
                        callback.onFailure(task1.getException());
                    }
                });
    }
}
