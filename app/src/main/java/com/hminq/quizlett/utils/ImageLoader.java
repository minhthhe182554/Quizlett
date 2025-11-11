package com.hminq.quizlett.utils;

import android.widget.ImageView;

import com.hminq.quizlett.R;
import com.hminq.quizlett.data.repository.LessonRepository;
import com.squareup.picasso.Picasso;

public class ImageLoader {
    public static void loadImage(ImageView imageView, String imageUrl) {
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.color.light_blue)
                .error(R.drawable.error_img)
                .into(imageView);
    }
}
