package com.hminq.quizlett.data.dto.request;

import com.hminq.quizlett.data.remote.model.Question;

import java.io.Serializable;
import java.util.List;

public class CreateTestRequest implements Serializable {
    private TestMethod testMethod;
    private List<Question> questionList;
}
