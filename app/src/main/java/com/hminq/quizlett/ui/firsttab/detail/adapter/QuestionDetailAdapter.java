package com.hminq.quizlett.ui.firsttab.detail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.databinding.ItemQuestionBinding;

import java.util.ArrayList;
import java.util.List;

public class QuestionDetailAdapter extends RecyclerView.Adapter<QuestionDetailAdapter.QuestionViewHolder> {

    private List<Question> questions = new ArrayList<>();

    public void setQuestions(List<Question> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemQuestionBinding binding = ItemQuestionBinding.inflate(inflater, parent, false);
        return new QuestionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.bind(question);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionBinding binding;

        public QuestionViewHolder(ItemQuestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Question question) {
            // Show question text
            binding.tvQuestionText.setText(question.getQuestionText());

            // Get correct answer text from answerOptions using correctAnswerIndex
            String correctAnswerText = "Unknown";
            if (question.getAnswerOptions() != null
                    && !question.getAnswerOptions().isEmpty()
                    && question.getCorrectAnswerIndex() >= 0
                    && question.getCorrectAnswerIndex() < question.getAnswerOptions().size()) {
                correctAnswerText = question.getAnswerOptions().get(question.getCorrectAnswerIndex());
            }

            String correctAnswer = "Correct: " + correctAnswerText;
            binding.tvCorrectAnswer.setText(correctAnswer);

            // Hide answer options and category
            binding.tvAnswerOptions.setVisibility(View.GONE);
            binding.tvCategory.setVisibility(View.GONE);
        }
    }
}