package com.hminq.quizlett.ui.secondtab.lesson.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons;
    private final OnItemClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

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
        holder.bind(lesson, listener, sdf);
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
        TextView txtTitle, txtCategory, txtLastVisited, txtNumberVisited, txtQuestionCount;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.tvLessonTitle);
            txtCategory = itemView.findViewById(R.id.tvCategory);
            txtNumberVisited = itemView.findViewById(R.id.tvNumberVisited);
            txtLastVisited = itemView.findViewById(R.id.tvLastVisited);
            txtQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
        }

        void bind(Lesson lesson, OnItemClickListener listener, SimpleDateFormat sdf) {
            txtTitle.setText(lesson.getTitle());
            txtCategory.setText(lesson.getCategory() != null ? lesson.getCategory().name() : "Unknown");


            txtNumberVisited.setText("Number of Visits: "+lesson.getVisitCount());

            int questionCount = (lesson.getQuestions() != null) ? lesson.getQuestions().size() : 0;
            txtQuestionCount.setText("Number of Questions: "+ questionCount);

            txtLastVisited.setText(
                    lesson.getLastVisited() != null
                            ? "Last visited: " + sdf.format(lesson.getLastVisited())
                            : "No visits yet"
            );

            itemView.setOnClickListener(v -> listener.onItemClick(lesson));
        }
    }
}
