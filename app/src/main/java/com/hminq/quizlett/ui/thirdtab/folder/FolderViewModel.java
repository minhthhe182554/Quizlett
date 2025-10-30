package com.hminq.quizlett.ui.thirdtab.folder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.Folder;
import com.hminq.quizlett.data.repository.FolderRepository;
import com.hminq.quizlett.exceptions.ValidationException;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FolderViewModel extends ViewModel {

    private final FolderRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<CreationResult> _creationResult = new MutableLiveData<>();
    public LiveData<CreationResult> creationResult = _creationResult;

    private final MutableLiveData<List<Folder>> _folders = new MutableLiveData<>();
    public LiveData<List<Folder>> folders = _folders;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public FolderViewModel(FolderRepository repository) {
        this.repository = repository;
        loadFolders();
    }

    public void createFolder(String folderName) {
        _creationResult.setValue(CreationResult.LOADING);

        disposables.add(
                repository.createNewFolder(folderName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    _creationResult.setValue(CreationResult.SUCCESS);
                                    loadFolders();
                                },
                                throwable -> {
                                    if (throwable instanceof ValidationException) {
                                        _creationResult.setValue(CreationResult.INVALID_INPUT);
                                    } else {
                                        _creationResult.setValue(CreationResult.ERROR);
                                    }
                                }
                        )
        );
    }

    public void loadFolders() {
        _isLoading.setValue(true);

        disposables.add(
                repository.getFolders()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                folderList -> {
                                    _isLoading.setValue(false);
                                    _folders.setValue(folderList);
                                },
                                throwable -> {
                                    _isLoading.setValue(false);
                                    _folders.setValue(null);
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public enum CreationResult {
        LOADING,
        SUCCESS,
        INVALID_INPUT,
        ERROR
    }
}