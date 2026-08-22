package com.jwidori.game;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jwidori.game.model.Card;
import com.jwidori.game.model.GameEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    private static final int STATE_HOME = 0;
    private static final int STATE_LOBBY = 1;
    private static final int STATE_WAITING = 2;
    private static final int STATE_GAME = 3;

    private FrameLayout root;
    private ImageView screenImage;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int state = STATE_HOME;
    private int lastLobbyIndex = -1;
    private int currentLobbyIndex = -1;
    private GameEngine gameEngine;
    private boolean botTurnScheduled = false;

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

    private void clearOverlayViews() {
        handler.removeCallbacksAndMessages(null);
        botTurnScheduled = false;
        while (root.getChildCount() > 1) {
            root.removeViewAt(1);
        }
    }

    private void showHome() {
        state = STATE_HOME;
        gameEngine = null;
        clearOverlayViews();
        screenImage.setScaleType(ImageView.ScaleType.FIT_XY);
        screenImage.setImageResource(R.drawable.home_main);

        addHotspot(0.20f, 0.49f, 0.60f, 0.11f, v -> showRandomLobby());
    }

    private void showRandomLobby() {
        state = STATE_LOBBY;
        gameEngine = null;
        clearOverlayViews();

        int idx;
        if (lobbyImages.length == 1) {
            idx = 0;
        } else {
            do {
                idx = random.nextInt(lobbyImages.length);
            } while (idx == lastLobbyIndex);
        }

        lastLobbyIndex = idx;
        currentLobbyIndex = idx;
        screenImage.setScaleType(ImageView.ScaleType.FIT_XY);
        screenImage.setImageResource(lobbyImages[idx]);
        attachLobbyHotspots();
    }

    private void showCurrentLobby() {
        state = STATE_LOBBY;
        gameEngine = null;
        clearOverlayViews();
        if (currentLobbyIndex < 0) {
            showRandomLobby();
            return;
        }
        screenImage.setScaleType(ImageView.ScaleType.FIT_XY);
        screenImage.setImageResource(lobbyImages[currentLobbyIndex]);
        attachLobbyHotspots();
    }

    private void attachLobbyHotspots() {
        addHotspot(0.15f, 0.57f, 0.70f, 0.11f, v -> showCreateRoomDialog());
        addHotspot(0.15f, 0.72f, 0.70f, 0.11f, v -> startAutoMatch());

        addHotspot(0.015f, 0.015f, 0.12f, 0.08f, v -> toast("우편함은 다음 단계에서 연결됩니다."));
        addHotspot(0.14f, 0.015f, 0.14f, 0.08f, v -> toast("공지사항은 다음 단계에서 연결됩니다."));
        addHotspot(0.86f, 0.015f, 0.12f, 0.08f, v -> toast("설정은 다음 단계에서 연결됩니다."));
    }

    private void showCreateRoomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(22), dp(20), dp(22), dp(18));
        panel.setBackground(roundedDrawable(Color.rgb(255, 249, 235), Color.rgb(209, 158, 94), 24, 2));

        TextView title = makeText("방 만들기", 25, Color.rgb(89, 48, 30), true);
        title.setGravity(Gravity.CENTER);
        panel.addView(title, matchWrapMargins(0, 0, 0, 14));

        TextView roomLabel = makeText("방 이름", 15, Color.rgb(102, 65, 43), true);
        panel.addView(roomLabel, matchWrapMargins(0, 0, 0, 6));

        final EditText roomName = new EditText(this);
        roomName.setSingleLine(true);
        roomName.setText("치즈빌리지 방");
        roomName.setTextSize(17);
        roomName.setPadding(dp(14), dp(10), dp(14), dp(10));
        roomName.setBackground(roundedDrawable(Color.WHITE, Color.rgb(231, 193, 132), 16, 1));
        panel.addView(roomName, matchWrapMargins(0, 0, 0, 14));

        TextView countLabel = makeText("인원", 15, Color.rgb(102, 65, 43), true);
        panel.addView(countLabel, matchWrapMargins(0, 0, 0, 4));

        final RadioGroup countGroup = new RadioGroup(this);
        countGroup.setOrientation(RadioGroup.HORIZONTAL);
        countGroup.setGravity(Gravity.CENTER);
        RadioButton two = makeRadio("2명", 2);
        RadioButton three = makeRadio("3명", 3);
        RadioButton four = makeRadio("4명", 4);
        countGroup.addView(two);
        countGroup.addView(three);
        countGroup.addView(four);
        four.setChecked(true);
        panel.addView(countGroup, matchWrapMargins(0, 0, 0, 10));

        TextView modeLabel = makeText("게임 모드", 15, Color.rgb(102, 65, 43), true);
        panel.addView(modeLabel, matchWrapMargins(0, 0, 0, 4));

        final RadioGroup modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.HORIZONTAL);
        modeGroup.setGravity(Gravity.CENTER);
        RadioButton normal = makeRadio("일반전", 100);
        RadioButton friendly = makeRadio("친선전", 101);
        modeGroup.addView(normal);
        modeGroup.addView(friendly);
        normal.setChecked(true);
        panel.addView(modeGroup, matchWrapMargins(0, 0, 0, 8));

        final CheckBox privateRoom = new CheckBox(this);
        privateRoom.setText("비공개 방");
        privateRoom.setTextSize(15);
        privateRoom.setTextColor(Color.rgb(102, 65, 43));
        panel.addView(privateRoom, matchWrapMargins(0, 0, 0, 6));

        final EditText password = new EditText(this);
        password.setHint("비밀번호 4자리");
        password.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        password.setMaxLines(1);
        password.setVisibility(View.GONE);
        password.setPadding(dp(14), dp(9), dp(14), dp(9));
        password.setBackground(roundedDrawable(Color.WHITE, Color.rgb(231, 193, 132), 16, 1));
        panel.addView(password, matchWrapMargins(0, 0, 0, 12));

        privateRoom.setOnCheckedChangeListener((buttonView, isChecked) ->
                password.setVisibility(isChecked ? View.VISIBLE : View.GONE)
        );

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);

        Button cancel = makeButton("취소", Color.rgb(221, 221, 221), Color.rgb(80, 80, 80));
        Button create = makeButton("만들기", Color.rgb(243, 133, 144), Color.WHITE);

        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        buttonLp.setMargins(dp(4), dp(4), dp(4), 0);
        buttons.addView(cancel, buttonLp);
        buttons.addView(create, buttonLp);
        panel.addView(buttons);

        dialog.setContentView(panel);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setDimAmount(0.45f);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        cancel.setOnClickListener(v -> dialog.dismiss());
        create.setOnClickListener(v -> {
            String name = roomName.getText().toString().trim();
            if (name.isEmpty()) {
                roomName.setError("방 이름을 입력해 주세요.");
                return;
            }
            if (privateRoom.isChecked() && password.getText().toString().trim().length() < 4) {
                password.setError("비밀번호 4자리를 입력해 주세요.");
                return;
            }

            int checkedId = countGroup.getCheckedRadioButtonId();
            int maxPlayers = checkedId == 2 ? 2 : checkedId == 3 ? 3 : 4;
            String mode = modeGroup.getCheckedRadioButtonId() == 101 ? "친선전" : "일반전";
            dialog.dismiss();
            showWaitingRoom(name, maxPlayers, mode, false);
        });

        dialog.show();
        if (w != null) {
            w.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.88f), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private RadioButton makeRadio(String label, int id) {
        RadioButton b = new RadioButton(this);
        b.setId(id);
        b.setText(label);
        b.setTextSize(15);
        b.setTextColor(Color.rgb(102, 65, 43));
        b.setPadding(dp(5), 0, dp(5), 0);
        return b;
    }

    private void startAutoMatch() {
        state = STATE_WAITING;
        clearOverlayViews();

        FrameLayout shade = new FrameLayout(this);
        shade.setBackgroundColor(Color.argb(80, 66, 43, 25));
        root.addView(shade, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(28), dp(24), dp(28), dp(24));
        box.setBackground(roundedDrawable(Color.rgb(255, 249, 235), Color.rgb(226, 173, 99), 24, 2));

        TextView title = makeText("자동매칭", 27, Color.rgb(92, 52, 31), true);
        title.setGravity(Gravity.CENTER);
        box.addView(title, matchWrapMargins(0, 0, 0, 12));

        TextView msg = makeText("치즈빌리지에서\n함께 놀 친구를 찾고 있어요…", 18, Color.rgb(103, 72, 53), false);
        msg.setGravity(Gravity.CENTER);
        box.addView(msg, matchWrapMargins(0, 0, 0, 16));

        TextView dots = makeText("●  ●  ●", 20, Color.rgb(239, 132, 143), true);
        dots.setGravity(Gravity.CENTER);
        box.addView(dots, matchWrapMargins(0, 0, 0, 12));

        Button cancel = makeButton("매칭 취소", Color.rgb(225, 225, 225), Color.rgb(80, 80, 80));
        cancel.setOnClickListener(v -> showCurrentLobby());
        box.addView(cancel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.78f),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;
        root.addView(box, lp);

        handler.postDelayed(() -> {
            if (state == STATE_WAITING && box.getParent() != null) {
                showWaitingRoom("자동매칭 #" + (100 + random.nextInt(900)), 4, "일반전", true);
            }
        }, 1400);
    }

    private void showWaitingRoom(String roomName, int maxPlayers, String mode, boolean autoFilled) {
        state = STATE_WAITING;
        clearOverlayViews();

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        card.setBackground(roundedDrawable(Color.argb(242, 255, 249, 235), Color.rgb(215, 157, 85), 24, 2));

        TextView title = makeText("게임 대기실", 27, Color.rgb(87, 47, 29), true);
        title.setGravity(Gravity.CENTER);
        card.addView(title, matchWrapMargins(0, 0, 0, 4));

        TextView info = makeText(roomName + "  ·  " + mode + "  ·  " + maxPlayers + "인", 14, Color.rgb(127, 91, 66), false);
        info.setGravity(Gravity.CENTER);
        card.addView(info, matchWrapMargins(0, 0, 0, 14));

        final List<TextView> slots = new ArrayList<>();
        for (int i = 0; i < maxPlayers; i++) {
            String label;
            if (i == 0) {
                label = "🐭  나   준비 완료";
            } else if (autoFilled) {
                label = "🐭  플레이어 " + (i + 1) + "   준비 완료";
            } else {
                label = "＋   빈 자리";
            }
            TextView slot = makeText(label, 17, Color.rgb(89, 66, 52), i == 0);
            slot.setGravity(Gravity.CENTER_VERTICAL);
            slot.setPadding(dp(14), dp(10), dp(14), dp(10));
            slot.setBackground(roundedDrawable(Color.WHITE, Color.rgb(235, 207, 164), 15, 1));
            card.addView(slot, matchWrapMargins(0, 0, 0, 8));
            slots.add(slot);
        }

        TextView status = makeText(autoFilled ? "모든 플레이어가 준비됐어요!" : "친구를 초대하거나 테스트 봇으로 채워 주세요.", 14, Color.rgb(120, 80, 60), false);
        status.setGravity(Gravity.CENTER);
        card.addView(status, matchWrapMargins(0, 4, 0, 10));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);

        Button leave = makeButton("로비로", Color.rgb(224, 224, 224), Color.rgb(75, 75, 75));
        Button fill = makeButton(autoFilled ? "준비 완료" : "봇 채우기", Color.rgb(119, 191, 218), Color.WHITE);
        Button start = makeButton("게임 시작", Color.rgb(243, 133, 144), Color.WHITE);
        start.setEnabled(autoFilled);
        start.setAlpha(autoFilled ? 1f : 0.45f);

        LinearLayout.LayoutParams actionLp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        actionLp.setMargins(dp(3), 0, dp(3), 0);
        actions.addView(leave, actionLp);
        actions.addView(fill, actionLp);
        actions.addView(start, actionLp);
        card.addView(actions);

        leave.setOnClickListener(v -> showCurrentLobby());

        fill.setOnClickListener(v -> {
            for (int i = 1; i < slots.size(); i++) {
                slots.get(i).setText("🐭  테스트 봇 " + i + "   준비 완료");
                slots.get(i).setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            }
            status.setText("모든 플레이어가 준비됐어요!");
            fill.setText("준비 완료");
            start.setEnabled(true);
            start.setAlpha(1f);
        });

        start.setOnClickListener(v -> startLocalGame(maxPlayers));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.88f),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = Gravity.CENTER;
        root.addView(card, lp);
    }

    private void startLocalGame(int playerCount) {
        state = STATE_GAME;
        clearOverlayViews();
        gameEngine = new GameEngine(playerCount, 7, random);
        renderGameScreen();
        scheduleBotTurnIfNeeded();
    }

    private void renderGameScreen() {
        if (state != STATE_GAME || gameEngine == null) {
            return;
        }
        clearGameOverlayOnly();

        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {Color.rgb(219, 244, 244), Color.rgb(255, 241, 208), Color.rgb(255, 226, 218)}
        );
        screenImage.setScaleType(ImageView.ScaleType.FIT_XY);
        screenImage.setImageDrawable(background);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(12), dp(16), dp(12), dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        Button back = makeButton("← 로비", Color.rgb(255, 248, 230), Color.rgb(91, 58, 39));
        back.setOnClickListener(v -> showCurrentLobby());
        header.addView(back, new LinearLayout.LayoutParams(dp(82), dp(44)));

        TextView gameTitle = makeText("치즈테일 카드게임", 22, Color.rgb(92, 52, 31), true);
        gameTitle.setGravity(Gravity.CENTER);
        header.addView(gameTitle, new LinearLayout.LayoutParams(0, dp(44), 1f));

        TextView pile = makeText("더미 " + gameEngine.getDrawPileCount(), 13, Color.rgb(100, 75, 55), true);
        pile.setGravity(Gravity.CENTER);
        header.addView(pile, new LinearLayout.LayoutParams(dp(72), dp(44)));
        page.addView(header, matchWrapMargins(0, 0, 0, 8));

        LinearLayout opponents = new LinearLayout(this);
        opponents.setOrientation(LinearLayout.HORIZONTAL);
        opponents.setGravity(Gravity.CENTER);
        for (int p = 1; p < gameEngine.getPlayerCount(); p++) {
            TextView opponent = makeText(
                    "🐭 P" + (p + 1) + "\n" + gameEngine.getHandSize(p) + "장",
                    15,
                    Color.rgb(83, 62, 48),
                    gameEngine.getCurrentPlayer() == p
            );
            opponent.setGravity(Gravity.CENTER);
            opponent.setPadding(dp(8), dp(8), dp(8), dp(8));
            int fill = gameEngine.getCurrentPlayer() == p ? Color.rgb(255, 230, 151) : Color.argb(230, 255, 252, 244);
            opponent.setBackground(roundedDrawable(fill, Color.rgb(223, 180, 114), 16, 1));
            LinearLayout.LayoutParams oppLp = new LinearLayout.LayoutParams(0, dp(72), 1f);
            oppLp.setMargins(dp(3), 0, dp(3), 0);
            opponents.addView(opponent, oppLp);
        }
        page.addView(opponents, matchWrapMargins(0, 0, 0, 12));

        TextView turn = makeText(
                gameEngine.getCurrentPlayer() == 0 ? "내 차례야 찍!" : "P" + (gameEngine.getCurrentPlayer() + 1) + " 차례…",
                19,
                Color.rgb(110, 65, 49),
                true
        );
        turn.setGravity(Gravity.CENTER);
        page.addView(turn, matchWrapMargins(0, 0, 0, 10));

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.HORIZONTAL);
        center.setGravity(Gravity.CENTER);

        Button drawButton = makeButton("카드 뽑기\n" + gameEngine.getDrawPileCount() + "장", Color.rgb(119, 191, 218), Color.WHITE);
        drawButton.setEnabled(gameEngine.getCurrentPlayer() == 0 && gameEngine.getWinner() < 0);
        drawButton.setAlpha(drawButton.isEnabled() ? 1f : 0.45f);
        drawButton.setOnClickListener(v -> {
            if (gameEngine != null && gameEngine.drawAndPass(0)) {
                renderGameScreen();
                scheduleBotTurnIfNeeded();
            }
        });
        center.addView(drawButton, cardSlotParams());

        Card topCard = gameEngine.getTopCard();
        TextView top = makeText(topCard.getDisplayText(), 30, Color.rgb(70, 49, 39), true);
        top.setGravity(Gravity.CENTER);
        top.setBackground(roundedDrawable(cardFoodColor(topCard.getFood()), Color.rgb(118, 75, 49), 18, 2));
        center.addView(top, cardSlotParams());
        page.addView(center, matchWrapMargins(0, 0, 0, 12));

        TextView guide = makeText(
                "같은 음식 또는 같은 숫자/문양 카드를 내세요.\n낼 카드가 없으면 카드 뽑기를 누르세요.",
                13,
                Color.rgb(111, 83, 64),
                false
        );
        guide.setGravity(Gravity.CENTER);
        page.addView(guide, matchWrapMargins(0, 0, 0, 8));

        TextView handLabel = makeText("내 카드 · " + gameEngine.getHandSize(0) + "장", 17, Color.rgb(85, 54, 37), true);
        handLabel.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(handLabel, matchWrapMargins(4, 0, 0, 6));

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout handRow = new LinearLayout(this);
        handRow.setOrientation(LinearLayout.HORIZONTAL);
        handRow.setGravity(Gravity.CENTER_VERTICAL);
        handRow.setPadding(dp(4), dp(2), dp(4), dp(4));

        List<Card> hand = gameEngine.getHand(0);
        for (int i = 0; i < hand.size(); i++) {
            final int handIndex = i;
            Card card = hand.get(i);
            Button cardButton = makeButton(card.getDisplayText(), cardFoodColor(card.getFood()), Color.rgb(73, 49, 38));
            boolean playable = gameEngine.getCurrentPlayer() == 0 && gameEngine.canPlay(card) && gameEngine.getWinner() < 0;
            cardButton.setEnabled(gameEngine.getCurrentPlayer() == 0 && gameEngine.getWinner() < 0);
            cardButton.setAlpha(playable ? 1f : 0.65f);
            cardButton.setOnClickListener(v -> {
                if (gameEngine == null || gameEngine.getCurrentPlayer() != 0) {
                    return;
                }
                if (!gameEngine.canPlay(gameEngine.getHand(0).get(handIndex))) {
                    toast("같은 음식이나 같은 숫자/문양 카드를 내야 해요 찍!");
                    return;
                }
                if (gameEngine.playCard(0, handIndex)) {
                    if (gameEngine.getWinner() >= 0) {
                        renderGameScreen();
                        showGameResult(gameEngine.getWinner());
                    } else {
                        renderGameScreen();
                        scheduleBotTurnIfNeeded();
                    }
                }
            });
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(dp(76), dp(108));
            cardLp.setMargins(dp(4), 0, dp(4), 0);
            handRow.addView(cardButton, cardLp);
        }
        scroll.addView(handRow, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        page.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(118)));

        TextView rules = makeText("현재 테스트판: 일반 A~K 카드 규칙 · 음식 4종", 12, Color.rgb(130, 102, 82), false);
        rules.setGravity(Gravity.CENTER);
        page.addView(rules, matchWrapMargins(0, 8, 0, 0));

        root.addView(page, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    private void clearGameOverlayOnly() {
        while (root.getChildCount() > 1) {
            root.removeViewAt(1);
        }
    }

    private LinearLayout.LayoutParams cardSlotParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(112), dp(150));
        lp.setMargins(dp(8), 0, dp(8), 0);
        return lp;
    }

    private int cardFoodColor(Card.Food food) {
        switch (food) {
            case CHEESE:
                return Color.rgb(255, 225, 139);
            case BREAD:
                return Color.rgb(243, 207, 161);
            case SAUSAGE:
                return Color.rgb(249, 174, 172);
            case COOKIE:
            default:
                return Color.rgb(209, 191, 235);
        }
    }

    private void scheduleBotTurnIfNeeded() {
        if (state != STATE_GAME || gameEngine == null || gameEngine.getWinner() >= 0) {
            return;
        }
        if (gameEngine.getCurrentPlayer() == 0 || botTurnScheduled) {
            return;
        }

        botTurnScheduled = true;
        handler.postDelayed(() -> {
            botTurnScheduled = false;
            if (state != STATE_GAME || gameEngine == null || gameEngine.getWinner() >= 0) {
                return;
            }

            int bot = gameEngine.getCurrentPlayer();
            if (bot == 0) {
                renderGameScreen();
                return;
            }

            int playable = gameEngine.findFirstPlayableIndex(bot);
            if (playable >= 0) {
                gameEngine.playCard(bot, playable);
            } else {
                gameEngine.drawAndPass(bot);
            }

            renderGameScreen();
            if (gameEngine.getWinner() >= 0) {
                showGameResult(gameEngine.getWinner());
            } else {
                scheduleBotTurnIfNeeded();
            }
        }, 650);
    }

    private void showGameResult(int winner) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(28), dp(24), dp(28), dp(22));
        panel.setBackground(roundedDrawable(Color.rgb(255, 249, 235), Color.rgb(215, 157, 85), 24, 2));

        String resultTitle = winner == 0 ? "승리! 찍~! 🎉" : "이번 판은 P" + (winner + 1) + " 승리!";
        TextView title = makeText(resultTitle, 26, Color.rgb(91, 50, 31), true);
        title.setGravity(Gravity.CENTER);
        panel.addView(title, matchWrapMargins(0, 0, 0, 12));

        TextView message = makeText(
                winner == 0 ? "내 카드를 모두 내려놓았어요!" : "다음 판에는 더 빠르게 카드를 내려봐요 찍!",
                16,
                Color.rgb(112, 78, 57),
                false
        );
        message.setGravity(Gravity.CENTER);
        panel.addView(message, matchWrapMargins(0, 0, 0, 16));

        Button lobby = makeButton("멀티플레이 로비로", Color.rgb(243, 133, 144), Color.WHITE);
        lobby.setOnClickListener(v -> {
            dialog.dismiss();
            showCurrentLobby();
        });
        panel.addView(lobby, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));

        dialog.setContentView(panel);
        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setDimAmount(0.5f);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            w.setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.84f), WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void addHotspot(float x, float y, float w, float h, View.OnClickListener listener) {
        View hotspot = new View(this);
        hotspot.setBackground(new ColorDrawable(Color.TRANSPARENT));
        hotspot.setOnClickListener(listener);

        root.post(() -> {
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
        });
    }

    private TextView makeText(String text, int sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        return tv;
    }

    private Button makeButton(String text, int background, int foreground) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTextColor(foreground);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(roundedDrawable(background, Color.TRANSPARENT, 16, 0));
        return b;
    }

    private GradientDrawable roundedDrawable(int fill, int stroke, int radiusDp, int strokeDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fill);
        gd.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            gd.setStroke(dp(strokeDp), stroke);
        }
        return gd;
    }

    private LinearLayout.LayoutParams matchWrapMargins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return lp;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (state == STATE_GAME) {
            showCurrentLobby();
        } else if (state == STATE_WAITING) {
            showCurrentLobby();
        } else if (state == STATE_LOBBY) {
            showHome();
        } else {
            super.onBackPressed();
        }
    }
}
