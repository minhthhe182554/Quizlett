package com.hminq.quizlett.utils;

import com.hminq.quizlett.data.repository.UserRepository;

import javax.inject.Inject;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class Fix {
    private final UserRepository mUserRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public Fix(UserRepository userRepository)
    {
        mUserRepository = userRepository;
    }
}
