package com.hminq.quizlett.data.remote.model;

import java.io.Serializable;

public class IncorrectAnswer implements Serializable {
    private String questionText;
    private String yourAnswer;
    private String correctAnswer;

    public IncorrectAnswer(String questionText, String yourAnswer, String correctAnswer) {
        this.questionText = questionText;
        this.yourAnswer = yourAnswer;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getYourAnswer() {
        return yourAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
