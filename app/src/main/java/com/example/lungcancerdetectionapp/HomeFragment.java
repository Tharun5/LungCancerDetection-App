package com.example.lungcancerdetectionapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lungcancerdetectionapp.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HomeFragment extends Fragment {

    private Button select, predict;
    private ImageView imgView;
    private Bitmap img;
    private ProgressBar progressBar;
    String outputClass;
    private TextView outputTxt;
    int flag=0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imgView = view.findViewById(R.id.img_view);
        select = view.findViewById(R.id.select_btn);
        predict = view.findViewById(R.id.predict_btn);
        progressBar = view.findViewById(R.id.progressBar);
        outputTxt = view.findViewById(R.id.outputClass);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag=1;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //img = Bitmap.createScaledBitmap(img, 350, 350, true);
                if(flag==1){
                    progressBar.setVisibility(View.VISIBLE);

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            tf();
                            progressBar.setVisibility(View.GONE);
                            Intent i = new Intent(getActivity().getApplicationContext(), ReportActivity.class);
                            i.putExtra("predictedClass",outputClass);
                            startActivity(i);
                            //outputTxt.setText(outputClass);

                        }
                    }, 100);

                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Please Upload the CT-Image",Toast.LENGTH_SHORT).show();
                }


            }
        });



        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100)
        {
            imgView.setImageURI(data.getData());

            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void tf(){
        try {
            Model model = Model.newInstance(getActivity().getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 350, 350, 3}, DataType.FLOAT32);

            TensorImage tensorImage = new TensorImage( DataType.FLOAT32);
            tensorImage.load(img);


            //ByteBuffer byteBuffer = tensorImage.getBuffer();

            ByteBuffer byteBuffer = convertBitmapToByteBuffer(img);


            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();


            // Releases model resources if no longer used.
            model.close();

            Log.d("Details",outputFeature0.toString());

            Log.d("Output:",outputFeature0.getFloatArray()[0] + "\n"+outputFeature0.getFloatArray()[1]+ "\n"+outputFeature0.getFloatArray()[2]);

            String classes_dir[] = {"Adenocarcinoma","No Cancer Detected","Squamous cell carcinoma"};

            float[] temp = outputFeature0.getFloatArray();

            int idx = argmax(temp);

            outputClass = classes_dir[idx];


            // output.setText(classes_dir[idx]+"\n\n"+outputFeature0.getFloatArray()[0] + "\n"+outputFeature0.getFloatArray()[1]+ "\n"+outputFeature0.getFloatArray()[2]);


        } catch (IOException e) {
            // TODO Handle the exception
        }
    }


    public static int argmax(float[] array) {
        float max = array[0];
        int re = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                re = i;
            }
        }
        return re;
    }


    private ByteBuffer convertBitmapToByteBuffer(Bitmap bp) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(Float.BYTES*350*350*3);
        imgData.order(ByteOrder.nativeOrder());
        Bitmap bitmap = Bitmap.createScaledBitmap(bp,350, 350,true);
        int [] intValues = new int[350*350];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        // Convert the image to floating point.
        int pixel = 0;

        for (int i = 0; i < 350; ++i) {
            for (int j = 0; j < 350; ++j) {
                final int val = intValues[pixel++];

                imgData.putFloat(((val>> 16) & 0xFF) / 255.f);
                imgData.putFloat(((val>> 8) & 0xFF) / 255.f);
                imgData.putFloat((val & 0xFF) / 255.f);
            }
        }
        return imgData;
    }


}