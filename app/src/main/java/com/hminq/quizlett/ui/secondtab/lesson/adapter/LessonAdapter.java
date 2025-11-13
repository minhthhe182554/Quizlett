package com.hminq.quizlett.ui.secondtab.lesson.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessons, OnItemClickListener listener) {
        this.lessons = lessons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.bind(lesson, listener);
    }

    @Override
    public int getItemCount() {
        return lessons != null ? lessons.size() : 0;
    }

    public void updateLessons(List<Lesson> newLessons) {
        this.lessons = newLessons;
        notifyDataSetChanged();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtCategory, txtNumberVisited, txtQuestionCount;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.tvLessonTitle);
            txtCategory = itemView.findViewById(R.id.tvCategory);
            txtNumberVisited = itemView.findViewById(R.id.tvNumberVisited);
            txtQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
        }

        void bind(Lesson lesson, OnItemClickListener listener) {
            txtTitle.setText(lesson.getTitle());
            String categoryText = lesson.getCategory() != null
                    ? lesson.getCategory().getLocalizedName(itemView.getContext())
                    : itemView.getContext().getString(R.string.category_others);
            txtCategory.setText(categoryText);

            txtNumberVisited.setText(itemView.getContext().getString(
                    R.string.number_visit_placeholder, lesson.getVisitCount()));

            int questionCount = (lesson.getQuestions() != null) ? lesson.getQuestions().size() : 0;
            txtQuestionCount.setText(itemView.getContext().getString(
                    R.string.number_question_placeholder, questionCount));

            itemView.setOnClickListener(v -> listener.onItemClick(lesson));
        }
    }
}