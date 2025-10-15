package com.hminq.quizlett.ui.secondtab.dashboard;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.DashboardData;
import com.hminq.quizlett.data.repository.LessonRepository;
import com.hminq.quizlett.data.repository.QuestionRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class DashboardViewModel extends ViewModel {
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final MutableLiveData<Integer> lessonCount = new MutableLiveData<>();
    private final MutableLiveData<DashboardData> dashboardDataLive = new MutableLiveData<>();
    private final MutableLiveData<Integer> questionCount = new MutableLiveData<>();
    private final MutableLiveData<Throwable> errorLiveData = new MutableLiveData<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public DashboardViewModel(QuestionRepository questionRepository, LessonRepository lessonRepository) {
        this.questionRepository = questionRepository;
        this.lessonRepository = lessonRepository;
    }

    public void loadData(String uid) {
        Single<Integer> lessonCountSingle = lessonRepository.getTotalLessons(uid);
        Single<Integer> questionCountSingle = questionRepository.getTotalQuestions(uid);

        compositeDisposable.add(
                Single.zip(lessonCountSingle, questionCountSingle,
                                // this function called when both Single succeed
                                (lCount, qCount) -> new DashboardData(20, lCount, qCount, 40, 35, 25) //wrap 2 result in a Pair
                        )
                        .subscribeOn(Schedulers.io()) // run this in IO threads
                        .observeOn(AndroidSchedulers.mainThread()) //observe in main (Android UI) thread
                        .subscribe(
                                // update livedata when succeed
                                dashboardData -> {
                                    dashboardDataLive.setValue(dashboardData);
                                },
                                // update errorLiveData
                                error -> errorLiveData.setValue(error)
                        )
        );
    }

    public LiveData<DashboardData> getDashboardDataLive() {
        return dashboardDataLive;
    }

    public LiveData<Throwable> getErrorLive() {
        return errorLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
