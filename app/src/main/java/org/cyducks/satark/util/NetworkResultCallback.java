package org.cyducks.satark.util;

import androidx.work.Data;

public interface NetworkResultCallback {
    void onSuccess(Data data);
    void onFailure(Exception exception);
}
