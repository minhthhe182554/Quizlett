package com.hminq.quizlett.utils;

import android.view.View;
import android.widget.LinearLayout;

import com.hminq.quizlett.data.remote.model.LessonCategory;

import java.util.Map;

public final class PercentageBarHelper {
    public static void update(View scienceView, View humanitiesView, View othersView, Map<String, Float> percentages) {

        float sciencePercent = percentages.getOrDefault(LessonCategory.Sciences.toString(), 0.0f);
        float humanitiesPercent = percentages.getOrDefault(LessonCategory.Humanities.toString(), 0.0f);
        float othersPercent = percentages.getOrDefault(LessonCategory.Others.toString(), 0.0f);

        updateSegment(scienceView, sciencePercent);
        updateSegment(humanitiesView, humanitiesPercent);
        updateSegment(othersView, othersPercent);

        if (scienceView.getParent() instanceof View) {
            ((View) scienceView.getParent()).requestLayout();
        }
    }

    private static void updateSegment(View view, float weight) {
        view.setVisibility(weight > 0 ? View.VISIBLE : View.GONE);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }
}
