package com.hminq.quizlett.ui.secondtab.question.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Question;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> questions;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Question question);
    }

    public QuestionAdapter(List<Question> questions, OnItemClickListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    public void updateQuestions(List<Question> newQuestions) {
        this.questions.clear();
        this.questions.addAll(newQuestions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.bind(question, listener);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionText;
        TextView tvCategory;
        TextView tvAnswerOptions;
        TextView tvCorrectAnswer;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAnswerOptions = itemView.findViewById(R.id.tvAnswerOptions);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
        }

        public void bind(Question question, OnItemClickListener listener) {
            tvQuestionText.setText(question.getQuestionText());
            tvCategory.setText(question.getCategory().name().toUpperCase());
            tvAnswerOptions.setText("Answer Options: " + question.getAnswerOptions().toString());
            tvCorrectAnswer.setText("Correct Answer: " + question.getAnswerOptions().get(question.getCorrectAnswerIndex()));
            itemView.setOnClickListener(v -> listener.onItemClick(question));
        }
    }
}