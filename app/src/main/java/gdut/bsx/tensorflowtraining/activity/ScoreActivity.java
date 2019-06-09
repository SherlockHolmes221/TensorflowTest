package gdut.bsx.tensorflowtraining.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import gdut.bsx.tensorflowtraining.R;

public class ScoreActivity extends AppCompatActivity{
    private int score  = 0;
    private TextView scoreTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_score);

        Intent intent = getIntent();
        score = (int)intent.getFloatExtra("score",0);

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

        scoreTv = findViewById(R.id.act_score);
        scoreTv.setText(String.valueOf(score));
    }
}
