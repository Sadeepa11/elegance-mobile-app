package com.nexora.elegance.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.nexora.elegance.R;

public class AnimationHelper {

    /**
     * Adds a subtle scale animation on touch to provide visual feedback for clicks.
     * @param view The view to apply the animation to.
     */
    public static void addPressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Animation scaleDown = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_down);
                    scaleDown.setFillAfter(true);
                    v.startAnimation(scaleDown);
                    return true;
                case MotionEvent.ACTION_UP:
                    Animation scaleUp = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_up);
                    scaleUp.setFillAfter(true);
                    v.startAnimation(scaleUp);
                    
                    // Only perform click if the release is within the view's bounds
                    if (isWithinBounds(v, event)) {
                        v.performClick();
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    // If the user slides out of the view, cancel the "pressed" state
                    if (!isWithinBounds(v, event)) {
                        Animation cancelAnim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_up);
                        cancelAnim.setFillAfter(true);
                        v.startAnimation(cancelAnim);
                    }
                    return false; // Return false so parents (like RecyclerView) can still handle scrolling
                case MotionEvent.ACTION_CANCEL:
                    Animation scaleUpCancel = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_up);
                    scaleUpCancel.setFillAfter(true);
                    v.startAnimation(scaleUpCancel);
                    return true;
            }
            return false;
        });
    }

    /**
     * Checks if a MotionEvent occurred within the bounds of a view.
     */
    private static boolean isWithinBounds(View v, MotionEvent event) {
        return event.getX() >= 0 && event.getX() <= v.getWidth() &&
               event.getY() >= 0 && event.getY() <= v.getHeight();
    }

    /**
     * Applies a fade-in animation to a view.
     */
    public static void fadeIn(View view) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in);
        view.startAnimation(fadeIn);
    }
}
