package com.treem.treem.activities.signup;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.treem.treem.R;
import com.treem.treem.activities.signup.phone.SignupPhoneActivity;
import com.treem.treem.helpers.security.Dialogs.AlertDialogOptions;
import com.treem.treem.helpers.security.Dialogs.CustomAlertDialogs;
import com.treem.treem.helpers.security.ProgressBar.LoadingProgressBar;
import com.treem.treem.models.signup.SignupAuthorizeAppResponse;
import com.treem.treem.models.signup.SignupQuestionAnswer;
import com.treem.treem.models.signup.SignupQuestionResponse;
import com.treem.treem.services.Treem.TreemAuthorizationService;
import com.treem.treem.services.Treem.TreemOAuthConsumerToken;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.util.Arrays;
import java.util.List;

public class SignupQuestionActivity extends Activity {
    private String currentQuestionId;
    private LoadingProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_question);

        // Connect loading mask to the main view group
        this.loadingProgressBar = new LoadingProgressBar((ViewGroup) findViewById(R.id.signupQuestionFrameLayout));

        /* Events */

        // resend question button event
        ((Button) findViewById(R.id.resendQuestionButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // retrieve new question
                getQuestion();
            }
        });

        // load challenge question
        getQuestion();
    }

    // Load challenge question
    private void getQuestion() {
        // load question to verify user is human
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                final Gson gson = new Gson();

                SignupQuestionResponse response = gson.fromJson(data, SignupQuestionResponse.class);

                // store current question ID
                SignupQuestionActivity.this.currentQuestionId = response.id;

                // load question into display
                loadQuestionIntoDisplay(response);

                // hide initial progress bar
                loadingProgressBar.toggleProgressBar(false);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                // hide initial progress bar
                loadingProgressBar.toggleProgressBar(false);

                // show general alert view
                CustomAlertDialogs.showGeneralErrorAlertDialog(findViewById(R.id.signupQuestionFrameLayout).getContext());
            }
        };

        // show loading progress bar indicator
        loadingProgressBar.toggleProgressBar(true);

        TreemAuthorizationService.getChallengeQuestion(request);
    }

    // Submit challenge answer
    private void submitAnswer(SignupQuestionAnswer answer) {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                // store the response
                final Gson gson = new Gson();

                SignupAuthorizeAppResponse response = gson.fromJson(data, SignupAuthorizeAppResponse.class);

                // store the device specific credentials
                TreemOAuthConsumerToken.SHARED_INSTANCE.setDeviceSpecificUUIDAndTokens(response);

                SignupPhoneActivity.setupPhoneNumber(SignupQuestionActivity.this);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                Context context = findViewById(R.id.signupQuestionFrameLayout).getContext();
                Resources resources = context.getResources();

                // hide initial progress bar
                loadingProgressBar.toggleProgressBar(false);

                // question expired error
                if (error == TreemServiceResponseCode.GENERIC_RESPONSE_CODE_2) {

                    AlertDialogOptions options = new AlertDialogOptions() {
                        @Override
                        public void positiveOnClick() {
                            // reload question when expired
                            SignupQuestionActivity.this.getQuestion();
                        }
                    };

                    options.title   = resources.getString(R.string.signup_question_expired_title);
                    options.message = resources.getString(R.string.signup_question_expired_message);

                    CustomAlertDialogs.showCustomErrorAlertDialog(context, options);
                }
                // question incorrect error
                else if (error == TreemServiceResponseCode.GENERIC_RESPONSE_CODE_3) {

                    AlertDialogOptions options = new AlertDialogOptions() {
                        @Override
                        public void positiveOnClick() {
                            // reload question when expired
                            SignupQuestionActivity.this.getQuestion();
                        }
                    };

                    options.title   = resources.getString(R.string.signup_question_incorrect_answer_title);
                    options.message = resources.getString(R.string.signup_question_incorrect_answer_message);

                    CustomAlertDialogs.showCustomErrorAlertDialog(context, options);
                }
                else {
                    // show general alert view
                    CustomAlertDialogs.showGeneralErrorAlertDialog(context);
                }
            }
        };

        // show loading progress bar indicator
        loadingProgressBar.toggleProgressBar(true);

        TreemAuthorizationService.authorizeApp(request, answer.a_id, this.currentQuestionId);
    }

    // Load question into display
    private void loadQuestionIntoDisplay(SignupQuestionResponse response) {
        // load the question text
        TextView questionText = (TextView)findViewById(R.id.questionTextView);
        questionText.setText(response.question);

        // load the answers
        final ListView listview = (ListView) findViewById(R.id.answersListView);

        // create adapter to bind answers to the table
        final QuestionsArrayAdapter adapter = new QuestionsArrayAdapter(this, R.layout.activity_signup_question_row, Arrays.asList(response.answers));

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);

                final SignupQuestionAnswer answerItem = (SignupQuestionAnswer) parent.getItemAtPosition(position);

                // call service to check answer
                submitAnswer(answerItem);
            }
        });
    }

    // create questions table adapter
    private class QuestionsArrayAdapter extends ArrayAdapter<SignupQuestionAnswer> {
        private final Context context;
        private final List<SignupQuestionAnswer> answers;

        public QuestionsArrayAdapter(Context context, int textViewResourceId, List<SignupQuestionAnswer> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            this.answers = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.activity_signup_question_row, parent, false);

            // update text
            TextView textView = (TextView) rowView.findViewById(R.id.answerTextView);
            textView.setText(answers.get(position).answer);

            return rowView;
        }
    }
}
