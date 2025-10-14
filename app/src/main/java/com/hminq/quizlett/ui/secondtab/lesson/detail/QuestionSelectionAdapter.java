package com.hminq.quizlett.ui.secondtab.lesson.detail;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.data.remote.model.Question;
import com.hminq.quizlett.databinding.ItemQuestionSelectionBinding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuestionSelectionAdapter extends ListAdapter<Question, QuestionSelectionAdapter.QuestionViewHolder> {

    private final OnQuestionSelectedListener listener;
    private final Set<String> selectedIds = new HashSet<>();

    public interface OnQuestionSelectedListener {
        void onQuestionSelected(Question question, boolean isChecked);
    }

    public QuestionSelectionAdapter(OnQuestionSelectedListener listener, List<String> initialSelectedIds) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        if (initialSelectedIds != null) {
            this.selectedIds.addAll(initialSelectedIds);
        }
    }

    private static final DiffUtil.ItemCallback<Question> DIFF_CALLBACK = new DiffUtil.ItemCallback<Question>() {
        @Override
        public boolean areItemsTheSame(@NonNull Question oldItem, @NonNull Question newItem) {
            return oldItem.getQuesId().equals(newItem.getQuesId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Question oldItem, @NonNull Question newItem) {
            return oldItem.getQuestionText().equals(newItem.getQuestionText());
        }
    };

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemQuestionSelectionBinding binding = ItemQuestionSelectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new QuestionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question currentQuestion = getItem(position);
        holder.bind(currentQuestion, listener, selectedIds.contains(currentQuestion.getQuesId()));
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final ItemQuestionSelectionBinding binding;

        public QuestionViewHolder(ItemQuestionSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Question question, OnQuestionSelectedListener listener, boolean isSelected) {
            binding.tvQuestionContent.setText(question.getQuestionText());
            binding.checkboxQuestion.setOnCheckedChangeListener(null);
            binding.checkboxQuestion.setChecked(isSelected);

            binding.checkboxQuestion.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String questionId = question.getQuesId();
                if (isChecked) {
                    selectedIds.add(questionId);
                } else {
                    selectedIds.remove(questionId);
                }
                if (listener != null) {
                    listener.onQuestionSelected(question, isChecked);
                }
            });
        }
    }
}
