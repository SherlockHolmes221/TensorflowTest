package gdut.bsx.tensorflowtraining.activity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import gdut.bsx.tensorflowtraining.R;
import gdut.bsx.tensorflowtraining.utils.Configure;

public class BeginActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_begin);

        initEvent();
    }

    private void initEvent() {


        findViewById(R.id.action1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configure.setCurrentMode(1);
                startActivity(new Intent(BeginActivity.this,TestActivity.class));
            }
        });


        findViewById(R.id.action2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configure.setCurrentMode(2);
                startActivity(new Intent(BeginActivity.this,TestActivity.class));
            }
        });

        findViewById(R.id.action3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configure.setCurrentMode(3);
                startActivity(new Intent(BeginActivity.this,TestActivity.class));
            }
        });

        findViewById(R.id.score).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BeginActivity.this,CameraActivity.class));
            }
        });
    }


}
