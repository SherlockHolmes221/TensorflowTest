package gdut.bsx.tensorflowtraining.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import gdut.bsx.tensorflowtraining.R;

public class ScoreActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_score);

        initEvent();
    }

    private void initEvent() {

        findViewById(R.id.act_score_finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScoreActivity.this,BeginActivity.class));
                finish();
            }
        });
    }
}
