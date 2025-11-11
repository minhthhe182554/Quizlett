package com.hminq.quizlett.ui.firsttab.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.remote.model.Lesson;
import com.hminq.quizlett.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class HomeLessonAdapter extends RecyclerView.Adapter<HomeLessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons;
    private final OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public HomeLessonAdapter(OnLessonClickListener listener) {
        this.lessons = new ArrayList<>();
        this.listener = listener;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons != null ? lessons : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addLessons(List<Lesson> lessons) {
        if (lessons != null) {
            this.lessons.addAll(lessons);
            notifyDataSetChanged();
        }
    }

    public void clearLessons() {
        this.lessons.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson_card_home, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.bind(lesson, listener);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLessonTitle;
        private final TextView tvQuestionCount;
        private final TextView tvCategory;
        private final ImageView ivProfileImage;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
            tvQuestionCount = itemView.findViewById(R.id.tvQuestionCount);
            tvCategory = itemView.findViewById(R.id.tvCategoryBadge);
            ivProfileImage = itemView.findViewById(R.id.profile_image);
        }

        public void bind(Lesson lesson, OnLessonClickListener listener) {
            tvLessonTitle.setText(lesson.getTitle());

            int questionCount = (lesson.getQuestions() != null)
                    ? lesson.getQuestions().size()
                    : 0;
            tvQuestionCount.setText(questionCount + " questions");

            tvCategory.setText(lesson.getCategory() != null
                    ? lesson.getCategory().name()
                    : "Others");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });

            //fix: Load creator profile image from creatorImage URL loaded by ViewModel
            // creatorImage đã được load bất đồng bộ trong HomeViewModel
            ImageLoader.loadImage(ivProfileImage, lesson.getCreatorImage());
        }
    }
}