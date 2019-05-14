package gdut.bsx.tensorflowtraining.activity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import gdut.bsx.tensorflowtraining.R;

public class BeginActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_begin);

        initEvent();
    }

    private void initEvent() {

        findViewById(R.id.act_begin_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BeginActivity.this,CameraActivity.class));
            }
        });


        findViewById(R.id.act_begin_start).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(BeginActivity.this,ScoreActivity.class));
                return true;
            }
        });
    }


}
