package ch.heigvd.pro.b04.android.Question;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.LinkedList;
import java.util.List;

import ch.heigvd.pro.b04.android.Datamodel.Answer;
import ch.heigvd.pro.b04.android.Datamodel.Poll;
import ch.heigvd.pro.b04.android.Datamodel.Question;
import ch.heigvd.pro.b04.android.Network.Rockin;
import ch.heigvd.pro.b04.android.Utils.LocalDebug;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionViewModel extends ViewModel {
    private MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private MutableLiveData<List<Answer>> currentAnswers = new MutableLiveData<>(new LinkedList<>());

    private Callback<List<Answer>> callbackAnswers = new Callback<List<Answer>>() {
        @Override
        public void onResponse(Call<List<Answer>> call, Response<List<Answer>> response) {
            if (response.isSuccessful()) {
                saveAnswers(response.body());
            } else {
                LocalDebug.logUnsuccessfulRequest(call, response);
            }
        }

        @Override
        public void onFailure(Call<List<Answer>> call, Throwable t) {
            LocalDebug.logFailedRequest(call, t);
        }
    };

    private void saveAnswers(List<Answer> answers) {
        this.currentAnswers.postValue(answers);
    }

    public QuestionViewModel() {}

    public MutableLiveData<List<Answer>> getCurrentAnswers() {
        return currentAnswers;
    }

    public void requestAnswers(String token, Question question) {
        if (question == null)
            return;

        Rockin.api()
                .getAnswers(
                        question.getIdModerator(),
                        question.getIdPoll(),
                        question.getIdQuestion(),
                        token)
                .enqueue(callbackAnswers);
    }

    public void getAllQuestionsFromBackend(Poll poll, String token) {
        QuestionUtils.sendGetQuestionRequest(poll, token);
    }

    public LiveData<Question> getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Question question) {
        currentQuestion.postValue(question);
    }

    public void changeToPreviousQuestion() {
        double currentIndex = currentQuestion.getValue().getIndexInPoll();
        double previousIndex = Double.MAX_VALUE;
        Question previous = null;

        for (Question q : QuestionUtils.getQuestions().getValue()) {
            double newIndex = q.getIndexInPoll();
            if (newIndex < currentIndex && (currentIndex - newIndex < previousIndex)) {
                previousIndex = currentIndex - newIndex;
                previous = q;
            }
        }

        if (previous != null)
            currentQuestion.setValue(previous);
    }

    public void changeToNextQuestion() {
        double currentIndex = currentQuestion.getValue().getIndexInPoll();
        double nextIndex = Double.MAX_VALUE;
        Question next = null;

        for (Question q : QuestionUtils.getQuestions().getValue()) {
            Log.w("localDebug", q.getTitle() + " " + q.getIndexInPoll());
            double newIndex = q.getIndexInPoll();
            if (newIndex > currentIndex && (newIndex - currentIndex < nextIndex)) {
                nextIndex = currentIndex - newIndex;
                next = q;
            }
        }

        if (next != null)
            currentQuestion.setValue(next);
    }
}
