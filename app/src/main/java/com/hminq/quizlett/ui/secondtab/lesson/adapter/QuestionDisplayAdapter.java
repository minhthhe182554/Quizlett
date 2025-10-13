package com.hminq.quizlett.ui.secondtab.lesson.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionDisplayAdapter extends RecyclerView.Adapter<QuestionDisplayAdapter.QuestionDisplayViewHolder> {

    private List<Question> questions = new ArrayList<>();

    @NonNull
    @Override
    public QuestionDisplayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionDisplayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionDisplayViewHolder holder, int position) {
        holder.bind(questions.get(position));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public void setQuestions(List<Question> newQuestions) {
        this.questions.clear();
        if (newQuestions != null) {
            this.questions.addAll(newQuestions);
        }
        notifyDataSetChanged();
    }

    static class QuestionDisplayViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQuestionText;
        private final TextView tvCategory;
        private final TextView tvAnswerOptions;
        private final TextView tvCorrectAnswer;

        public QuestionDisplayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAnswerOptions = itemView.findViewById(R.id.tvAnswerOptions);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
        }

        void bind(Question question) {
            tvQuestionText.setText(question.getQuestionText());
            tvCategory.setText(question.getCategory().name());


            tvAnswerOptions.setText("Answer Options: " + question.getAnswerOptions().toString());

            int correctIndex = question.getCorrectAnswerIndex();
            if (correctIndex >= 0 && correctIndex < question.getAnswerOptions().size()) {
                tvCorrectAnswer.setText("Correct Answer: " + question.getAnswerOptions().get(question.getCorrectAnswerIndex()));
            } else {
                tvCorrectAnswer.setText("Correct answer not set");
            }
        }
    }
}
