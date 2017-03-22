package com.example.andy.geoquizfcc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    // Tag for debug and other logs
    private static final String TAG = "QuizActivity";

    // key for savedInstanceBundle
    private static final String  KEY_INDEX = "index";
    private static final String KEY_SCORE = "score";
    private static final int REQUEST_CODE_CHEAT = 1;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mRandomButton;
    private Button mCheatButton;
    private ImageButton mPrevButton;
    private ImageButton mNextButton;
    private TextView mQuestionTextView;
    private TextView mScoreTextView;

    private int mCurrentIndex = 0;
    private int mScore = 0;
    private boolean mIsCheater;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_ca, false),
            new Question(R.string.question_sf, false),
            new Question(R.string.question_la, true),
            new Question(R.string.question_sd, true)
    };

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void randomizeQuestionIndex() {
        int limit = mQuestionBank.length - 1;
        int randomIndex = new Random().nextInt(limit);

        if (randomIndex == mCurrentIndex) {
            randomizeQuestionIndex();
        } else {
            mCurrentIndex = randomIndex;
            return;
        }
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;

        if (mIsCheater) {
            messageResId = R.string.judgement_toast;
        } else if (userPressedTrue == answerIsTrue) {
            messageResId = R.string.correct_toast;
            mScore++;
            mScoreTextView.setText("Score: " + mScore);
        } else {
            messageResId = R.string.incorrect_toast;
        }

        Toast t = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT);
        //t.setGravity(Gravity.TOP, 0, 0);
        t.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() was just called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was just called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() was just called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() was just called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() was just called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState() called");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putInt(KEY_SCORE, mScore);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate() was just called");

        setContentView(R.layout.activity_quiz);

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mScoreTextView = (TextView) findViewById(R.id.score);

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsCheater = false;
                if (mCurrentIndex == 0) {
                    mCurrentIndex = mQuestionBank.length - 1;
                } else {
                    mCurrentIndex--;
                }
                updateQuestion();
            }
        });

        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsCheater = false;
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mRandomButton = (Button) findViewById(R.id.random_button);
        mRandomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomizeQuestionIndex();
                updateQuestion();
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent i = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(i, REQUEST_CODE_CHEAT);
            }
        });

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mScore = savedInstanceState.getInt(KEY_SCORE, 0);
        }

        mScoreTextView.setText("Score: " + mScore);

        updateQuestion();
    }
}
