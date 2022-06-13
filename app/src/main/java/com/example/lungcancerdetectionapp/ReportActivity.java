package com.example.lungcancerdetectionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ReportActivity extends AppCompatActivity {

    TextView output, outputSym, outputTreat, titleTxt, symTitle, treatTitle;
    ImageView noCancerImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        output = findViewById(R.id.output);
        outputSym = findViewById(R.id.outputSym);
        outputTreat = findViewById(R.id.outputTreat);
        noCancerImg = findViewById(R.id.noCancerImg);
        titleTxt = findViewById(R.id.titleTxt);
        symTitle = findViewById(R.id.symTitle);
        treatTitle = findViewById(R.id.treatTitle);

        String predictedClass = getIntent().getStringExtra("predictedClass");

        output.setText(predictedClass);

        if(predictedClass.equals("Adenocarcinoma")) {
            outputSym.setText(R.string.ade_sym);
            outputTreat.setText(R.string.ade_tre);
        }else if(predictedClass.equals("Squamous cell carcinoma")){
            outputSym.setText(R.string.squ_sym);
            outputTreat.setText(R.string.squ_tre);
        }else{
            outputSym.setVisibility(View.GONE);
            titleTxt.setVisibility(View.GONE);
            outputTreat.setVisibility(View.GONE);
            symTitle.setVisibility(View.GONE);
            treatTitle.setVisibility(View.GONE);
            noCancerImg.setVisibility(View.VISIBLE);
        }



    }
}