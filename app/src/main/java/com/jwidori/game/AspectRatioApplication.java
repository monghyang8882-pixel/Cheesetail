package com.jwidori.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class AspectRatioApplication extends Application {

    private String shownAnnouncementKey = "";
    private boolean maintenanceDialogVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();
        AdminRuntime.init(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (activity instanceof MainActivity) {
                    installAspectRatioGuard(activity);
                }
            }

            @Override public void onActivityStarted(Activity activity) { }

            @Override
            public void onActivityResumed(Activity activity) {
                if (activity instanceof MainActivity) {
                    refreshAdminState(activity);
                }
            }

            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });
    }

    private void refreshAdminState(Activity activity) {
        AdminRuntime.refresh(config -> {
            if (activity.isFinishing() || activity.isDestroyed()) return;

            if (config.maintenanceMode) {
                if (!maintenanceDialogVisible) {
                    maintenanceDialogVisible = true;
                    String message = config.maintenanceMessage == null || config.maintenanceMessage.trim().isEmpty()
                            ? "현재 서버 점검 중입니다. 잠시 후 다시 이용해 주세요."
                            : config.maintenanceMessage;
                    AlertDialog dialog = new AlertDialog.Builder(activity)
                            .setTitle("점검 안내")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("게임 종료", (d, which) -> activity.finish())
                            .create();
                    dialog.setOnDismissListener(d -> maintenanceDialogVisible = false);
                    dialog.show();
                }
                return;
            }

            maintenanceDialogVisible = false;
            String title = config.latestAnnouncementTitle == null ? "" : config.latestAnnouncementTitle.trim();
            String body = config.latestAnnouncementBody == null ? "" : config.latestAnnouncementBody.trim();
            String key = title + "\n" + body;
            if (!title.isEmpty() && !key.equals(shownAnnouncementKey)) {
                shownAnnouncementKey = key;
                new AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(body)
                        .setPositiveButton("확인", null)
                        .show();
            }
        });
    }

    private void installAspectRatioGuard(Activity activity) {
        View contentView = activity.findViewById(android.R.id.content);
        if (!(contentView instanceof ViewGroup)) return;

        ViewGroup content = (ViewGroup) contentView;
        if (content.getChildCount() == 0 || !(content.getChildAt(0) instanceof FrameLayout)) return;

        FrameLayout root = (FrameLayout) content.getChildAt(0);
        if (root.getChildCount() == 0 || !(root.getChildAt(0) instanceof ImageView)) return;

        ImageView screenImage = (ImageView) root.getChildAt(0);
        applyAspectScale(screenImage);

        screenImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                applyAspectScale(screenImage);
                return true;
            }
        });

        root.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (isHotspot(child)) remapHotspot(root, screenImage, child);
            }
            @Override public void onChildViewRemoved(View parent, View child) { }
        });
    }

    private void applyAspectScale(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) return;
        if (drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0
                && imageView.getScaleType() != ImageView.ScaleType.FIT_CENTER) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    private boolean isHotspot(View child) {
        if (child.getClass() != View.class || !(child.getLayoutParams() instanceof FrameLayout.LayoutParams)) return false;
        Drawable background = child.getBackground();
        return background instanceof ColorDrawable
                && ((ColorDrawable) background).getColor() == Color.TRANSPARENT;
    }

    private void remapHotspot(FrameLayout root, ImageView screenImage, View hotspot) {
        int rootWidth = root.getWidth();
        int rootHeight = root.getHeight();
        if (rootWidth <= 0 || rootHeight <= 0) return;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) hotspot.getLayoutParams();
        if (lp.width <= 0 || lp.height <= 0) return;

        float xRatio = lp.leftMargin / (float) rootWidth;
        float yRatio = lp.topMargin / (float) rootHeight;
        float widthRatio = lp.width / (float) rootWidth;
        float heightRatio = lp.height / (float) rootHeight;

        float[] bounds = AspectRatioHelper.getFitCenterBounds(screenImage);
        float imageLeft = bounds[0];
        float imageTop = bounds[1];
        float imageWidth = bounds[2];
        float imageHeight = bounds[3];

        lp.width = Math.max(1, Math.round(imageWidth * widthRatio));
        lp.height = Math.max(1, Math.round(imageHeight * heightRatio));
        lp.leftMargin = Math.round(imageLeft + imageWidth * xRatio);
        lp.topMargin = Math.round(imageTop + imageHeight * yRatio);
        hotspot.setLayoutParams(lp);
    }
}
