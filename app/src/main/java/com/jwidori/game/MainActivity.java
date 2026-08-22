package com.jwidori.game;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends Activity {

    private FrameLayout root;
    private ImageView screenImage;
    private final Random random = new Random();
    private int lastLobbyIndex = -1;
    private boolean onLobby = false;

    private final int[] lobbyImages = new int[] {
            R.drawable.lobby_01,
            R.drawable.lobby_02,
            R.drawable.lobby_03,
            R.drawable.lobby_04
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(255, 247, 233));

        screenImage = new ImageView(this);
        screenImage.setScaleType(ImageView.ScaleType.FIT_XY);
        screenImage.setAdjustViewBounds(false);
        screenImage.setBackgroundColor(Color.rgb(255, 247, 233));

        FrameLayout.LayoutParams imageLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        root.addView(screenImage, imageLp);

        setContentView(root);
        showHome();
    }

    private void clearHotspots() {
        while (root.getChildCount() > 1) {
            root.removeViewAt(1);
        }
    }

    private void showHome() {
        onLobby = false;
        clearHotspots();
        screenImage.setImageResource(R.drawable.home_main);

        // 메인 화면의 큰 '게임 시작' 버튼 영역
        addHotspot(0.20f, 0.47f, 0.60f, 0.12f, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRandomLobby();
            }
        });
    }

    private void showRandomLobby() {
        onLobby = true;
        clearHotspots();

        int idx;
        if (lobbyImages.length == 1) {
            idx = 0;
        } else {
            do {
                idx = random.nextInt(lobbyImages.length);
            } while (idx == lastLobbyIndex);
        }
        lastLobbyIndex = idx;
        screenImage.setImageResource(lobbyImages[idx]);

        // 방 만들기 영역
        addHotspot(0.14f, 0.54f, 0.72f, 0.14f, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("방 만들기 화면은 다음 단계에서 연결됩니다.");
            }
        });

        // 자동 매칭 영역
        addHotspot(0.14f, 0.69f, 0.72f, 0.14f, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("자동 매칭을 시작합니다.");
            }
        });
    }

    private void addHotspot(float x, float y, float w, float h, View.OnClickListener listener) {
        View hotspot = new View(this);
        hotspot.setBackground(new ColorDrawable(Color.TRANSPARENT));
        hotspot.setOnClickListener(listener);

        root.post(new Runnable() {
            @Override
            public void run() {
                int rw = root.getWidth();
                int rh = root.getHeight();

                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        Math.max(1, (int) (rw * w)),
                        Math.max(1, (int) (rh * h))
                );
                lp.leftMargin = (int) (rw * x);
                lp.topMargin = (int) (rh * y);
                hotspot.setLayoutParams(lp);
                root.addView(hotspot);
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (onLobby) {
            showHome();
        } else {
            super.onBackPressed();
        }
    }
}
