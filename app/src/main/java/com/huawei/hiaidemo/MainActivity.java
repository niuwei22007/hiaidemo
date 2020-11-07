package com.huawei.hiaidemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiaidemo.bean.ModelInfo;
import com.huawei.hiaidemo.utils.ModelManager;

import java.io.File;
import java.util.ArrayList;

import static com.huawei.hiaidemo.utils.Constant.GALLERY_REQUEST_CODE;
import static com.huawei.hiaidemo.utils.Constant.IMAGE_CAPTURE_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    protected ModelInfo demoModelInfo = new ModelInfo();

    // Used to load the 'hiaijni' library on application startup.
    static {
        System.loadLibrary("hiaijni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        Button btnBuildModel = findViewById(R.id.btnBuildModel);
        Button btnInference = findViewById(R.id.btnInference);
        btnBuildModel.setOnClickListener(this);
        btnInference.setOnClickListener(this);
        initModels();
    }

    private void modelCompatibilityProcess() {
        //load hiaijni.so
        boolean isSoLoadSuccess = ModelManager.loadJNISo();
        if (isSoLoadSuccess) {
            Toast.makeText(this, "load hiaijni.so success.", Toast.LENGTH_SHORT).show();
            ModelManager.createOmModel(demoModelInfo.getModelSaveDir() + demoModelInfo.getOfflineModel(), getAssets());
        } else {
            Toast.makeText(this, "load hiaijni.so fail.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A native method that is implemented by the 'hiaijni' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    protected void initModels() {
        File dir = getDir("models", Context.MODE_PRIVATE);
        String path = dir.getAbsolutePath() + File.separator;
        demoModelInfo.setModelSaveDir(path);
        demoModelInfo.setOfflineModel("hiai.om");
        demoModelInfo.setOfflineModelName("hiai");
        demoModelInfo.setOnlineModelLabel("labels_caffe.txt");

        demoModelInfo.setInput_Number(1);
        demoModelInfo.setInput_N(1);
        demoModelInfo.setInput_C(3);
        demoModelInfo.setInput_H(227);
        demoModelInfo.setInput_W(227);
        demoModelInfo.setOutput_Number(1);
        demoModelInfo.setOutput_N(1);
        demoModelInfo.setOutput_C(1000);
        demoModelInfo.setOutput_H(1);
        demoModelInfo.setOutput_W(1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnBuildModel:
                modelCompatibilityProcess();
                break;
            case R.id.btnInference:
                checkCameraPermission();
                checkStoragePermission();
                ModelManager.loadModelSync(demoModelInfo);
                runModel(demoModelInfo, productData());
                break;
            default:
                break;
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    GALLERY_REQUEST_CODE);
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    IMAGE_CAPTURE_REQUEST_CODE);
        }
    }

    private ArrayList<byte[]> productData() {
        return new ArrayList<>(demoModelInfo.getInput_C() * demoModelInfo.getInput_H() * demoModelInfo.getInput_W());
    }

    private void runModel(ModelInfo modelInfo, ArrayList<byte[]> inputData) {
        long start = System.currentTimeMillis();
        ArrayList<float[]> outputDataList = ModelManager.runModelSync(modelInfo, inputData);

        if (outputDataList == null) {
            Log.e(TAG, "Sync runModel outputdata is null");
            return;
        }

        long end = System.currentTimeMillis();
        float inferenceTime = end - start;
        Log.w(TAG, "inferecn time: " + inferenceTime);
        for (float[] outputData : outputDataList) {
            Log.i(TAG, "runModel outputdata length : " + outputData.length);
        }
    }
}