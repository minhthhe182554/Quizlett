package com.hminq.quizlett.ui.firsttab.setting;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.User;
import com.hminq.quizlett.data.remote.model.Language;
import com.hminq.quizlett.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SettingViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    private final MutableLiveData<Boolean> isPushNotificationEnabled = new MutableLiveData<>();
    public LiveData<Boolean> getIsPushNotificationEnabled() {
        return isPushNotificationEnabled;
    }
    private final MutableLiveData<Language> currentLanguage = new MutableLiveData<>();
    public LiveData<Language> getCurrentLanguage() {
        return currentLanguage;
    }

    private final MutableLiveData<Boolean> signOutSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getSignOutSuccess() {
        return signOutSuccess;
    }

    private final MutableLiveData<String> updateStatus = new MutableLiveData<>();
    public LiveData<String> getUpdateStatus() { return updateStatus; }

    private final MutableLiveData<String> profileImageUrl = new MutableLiveData<>();
    public LiveData<String> getProfileImageUrl() {
        return profileImageUrl;
    }
    @Inject
    public SettingViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;

        loadUserProfile();
//        loadPreferences();
    }

    public void loadUserProfile() {
        disposables.add(
                userRepository.getUserProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                                    currentUser.setValue(user);
                                    loadUserImage(user);
                                }, throwable -> {

                                })
        );
    }

    public void updateProfileImage(Uri imageUri) {
        updateStatus.setValue("Uploading new profile image...");

        disposables.add(
                userRepository.uploadNewProfileImage(imageUri)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            updateStatus.setValue("Profile image updated successfully.");
                            // Tải lại Profile để cập nhật URL ảnh mới và hiển thị
                            loadUserProfile();
                        }, throwable -> {
                            updateStatus.setValue("Error updating profile image: " + throwable.getMessage());
                        })
        );
    }
    public void updateProfileField(String fieldName, String newValue, String currentPassword) {
        Completable updateCompletable;

        switch (fieldName) {
            case "fullname":
                updateCompletable = userRepository.updateFullname(newValue);
                break;
            case "email":
                return;
            case "password":
                if (currentPassword == null || currentPassword.isEmpty()) {
                    updateStatus.setValue("Mật khẩu hiện tại là bắt buộc để thay đổi Mật khẩu.");
                    return;
                }
                updateCompletable = userRepository.updatePassword(newValue, currentPassword);
                break;
            default:
                return;
        }

        disposables.add(
                updateCompletable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            updateStatus.setValue(fieldName + " updated successfully.");
                            loadUserProfile();
                        }, throwable -> {
                            updateStatus.setValue("Error updating " + fieldName + ": " + throwable.getMessage());
                        })
        );
    }
    public void updateProfileField(String fieldName, String newValue) {
        updateProfileField(fieldName, newValue, null);
    }

//    private void loadPreferences() {
//        User.UserSetting settings = userRepository.getCurrentSettings();
//
//        isPushNotificationEnabled.setValue(settings.isPushNotificationEnabled());
//
//        currentLanguage.setValue(settings.getLanguage());
//    }

    private void loadUserImage(User user) {
        disposables.add(
                userRepository.loadProfileImage(user)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(imageUrl -> {
                            profileImageUrl.setValue(imageUrl);
                        }, throwable -> {
                            Log.e(TAG, "Failed to load profile image URL: " + throwable.getMessage());
                        })
        );
    }


    public void setAppLanguage(Language language) {
        currentLanguage.setValue(language);
    }

    public void signOut() {
        disposables.add(
                userRepository.signOut()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            signOutSuccess.setValue(true);
                        }, throwable -> {
                            signOutSuccess.setValue(false);
                        })
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}