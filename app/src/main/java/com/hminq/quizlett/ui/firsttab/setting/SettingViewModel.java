package com.hminq.quizlett.ui.firsttab.setting;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.hminq.quizlett.data.remote.model.User;
import com.hminq.quizlett.data.remote.model.Language;
import com.hminq.quizlett.data.repository.LessonRepository;
import com.hminq.quizlett.data.repository.UserRepository;
import com.hminq.quizlett.utils.LocaleHelper;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SettingViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final Context context;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    public LiveData<User> getCurrentUser() {
        return currentUser;
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

    private final MutableLiveData<Boolean> updateLanguageResult = new MutableLiveData<>();

    public LiveData<Boolean> getUpdateLanguageResult() {
        return updateLanguageResult;
    }


    @Inject
    public SettingViewModel(
            @ApplicationContext Context context,
            UserRepository userRepository,
            LessonRepository lessonRepository
    ) {
        this.context = context;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;

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
                            
                            // Sync language from Firebase to SharedPreferences
                            if (user.getUserSetting() != null && user.getUserSetting().getLanguage() != null) {
                                String languageCode = user.getUserSetting().getLanguage().getCode();
                                String savedLanguage = LocaleHelper.getSavedLanguage(context);
                                if (!languageCode.equals(savedLanguage)) {
                                    LocaleHelper.saveLanguage(context, languageCode);
                                    Log.d(TAG, "Synced language from Firebase to SharedPreferences: " + languageCode);
                                }
                            }
                        }, throwable -> {
                            Log.e(TAG, "Error loading user profile: " + throwable.getMessage());
                        })
        );
    }

    private final MutableLiveData<Boolean> imageUploadSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getImageUploadSuccess() {
        return imageUploadSuccess;
    }
    
    public void updateProfileImage(Uri imageUri) {
        updateStatus.setValue("Uploading new profile image...");

        User user = currentUser.getValue();
        if (user == null) {
            updateStatus.setValue("Error: User not loaded");
            return;
        }

        String previousPath = user.getProfileImageUrl();
        String userId = user.getUid();

        disposables.add(
                userRepository.uploadNewProfileImage(imageUri, previousPath)
                        .subscribeOn(Schedulers.io())
                        .flatMapCompletable(downloadUrl -> {
                            // Sau khi upload thành công, update tất cả lessons của user
                            Log.d(TAG, "Updating all lessons with new image URL: " + downloadUrl);
                            return lessonRepository.updateAllLessonCreatorImage(userId, downloadUrl);
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            updateStatus.setValue("Profile image updated successfully.");
                            // Tải lại Profile để cập nhật URL ảnh mới và hiển thị
                            loadUserProfile();
                            // Trigger event để SharedViewModel reload
                            imageUploadSuccess.setValue(true);
                        }, throwable -> {
                            Log.e(TAG, "Error updating profile image: " + throwable.getMessage());
                            updateStatus.setValue("Error updating profile image: " + throwable.getMessage());
                            imageUploadSuccess.setValue(false);
                        })
        );
    }

    public void updateLanguageSetting(String languageCode) {
        updateLanguageResult.setValue(null);
        Language languageEnum = Language.fromCode(languageCode);

        // Save to SharedPreferences first for immediate locale change
        LocaleHelper.saveLanguage(context, languageCode);
        Log.d(TAG, "Saved language to SharedPreferences: " + languageCode);

        disposables.add(
                userRepository.updateLanguageSetting(languageEnum)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d(TAG, "Saved language to Firebase: " + languageCode);
                            updateLanguageResult.setValue(true);
                            loadUserProfile();
                        }, throwable -> {
                            Log.e(TAG, "Error updating language setting: " + throwable.getMessage());
                            updateLanguageResult.setValue(false);
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