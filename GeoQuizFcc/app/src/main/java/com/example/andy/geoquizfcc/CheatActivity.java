package com.example.andy.geoquizfcc;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CheatActivity extends AppCompatActivity {

    private static final String EXTRA_ANSWER_IS_TRUE = "com.example.andy.geoquizfcc.answer_is_true";
    private static final String EXTRA_ANSWER_SHOWN = "com.example.andy.geoquizfcc.answer_shown";

    private TextView mAnswerTextView;
    private Button mShowAnswerButton;
    private boolean mAnswerIsTrue;

    public static Intent newIntent(Context context, boolean answerIsTrue) {
        Intent i = new Intent(context, CheatActivity.class);
        i.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue);
        return i;
    }

    private void setAnswerShownResult(boolean didTheyShowAnswer) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_SHOWN, didTheyShowAnswer);
        setResult(RESULT_OK, data);
    }

    public static boolean wasAnswerShown(Intent i) {
        return i.getBooleanExtra(EXTRA_ANSWER_SHOWN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);

        mAnswerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false);


        mAnswerTextView = (TextView) findViewById(R.id.answer_text_view);

        mShowAnswerButton = (Button) findViewById(R.id.show_answer_button);
        mShowAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnswerTextView.setText(mAnswerIsTrue ? R.string.true_button : R.string.false_button);
                setAnswerShownResult(true);
            }
        });
    }
}
