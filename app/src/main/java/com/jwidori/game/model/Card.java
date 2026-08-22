package com.jwidori.game.model;

public class Card {

    public enum Food {
        CHEESE("치즈", "🧀", "ch"),
        BREAD("빵", "🍞", "b"),
        SAUSAGE("소세지", "🌭", "s"),
        COOKIE("쿠키", "🍪", "c");

        private final String koreanName;
        private final String icon;
        private final String assetPrefix;

        Food(String koreanName, String icon, String assetPrefix) {
            this.koreanName = koreanName;
            this.icon = icon;
            this.assetPrefix = assetPrefix;
        }

        public String getKoreanName() {
            return koreanName;
        }

        public String getIcon() {
            return icon;
        }

        public String getAssetPrefix() {
            return assetPrefix;
        }
    }

    public enum Kind {
        NORMAL,
        HIDE,
        FLASH,
        CATCH
    }

    private final Food food;
    private final String rank;
    private final Kind kind;

    public Card(Food food, String rank) {
        this(food, rank, Kind.NORMAL);
    }

    private Card(Food food, String rank, Kind kind) {
        this.food = food;
        this.rank = rank;
        this.kind = kind;
    }

    public static Card hideJoker() {
        return new Card(null, "숨숨", Kind.HIDE);
    }

    public static Card flashJoker() {
        return new Card(null, "+3", Kind.FLASH);
    }

    public static Card catchJoker() {
        return new Card(null, "+5", Kind.CATCH);
    }

    public Food getFood() {
        return food;
    }

    public String getRank() {
        return rank;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isJoker() {
        return kind != Kind.NORMAL;
    }

    public boolean matches(Card other) {
        if (other == null) {
            return false;
        }
        if (isJoker() || other.isJoker()) {
            return true;
        }
        return food == other.food || rank.equals(other.rank);
    }

    public String getDisplayText() {
        switch (kind) {
            case HIDE:
                return "🐱\n숨숨";
            case FLASH:
                return "✨🐱\n+3";
            case CATCH:
                return "🐱🐭\n+5";
            case NORMAL:
            default:
                return food.getIcon() + "\n" + rank;
        }
    }

    public String getDescription() {
        switch (kind) {
            case HIDE:
                return "숨숨 카드 · 한 장 더 내기";
            case FLASH:
                return "번쩍 카드 · 다음 플레이어 +3장";
            case CATCH:
                return "쥐잡기 카드 · 다음 플레이어 +5장";
            case NORMAL:
            default:
                return food.getKoreanName() + " " + rank;
        }
    }

    public String getAssetPath() {
        switch (kind) {
            case HIDE:
                return "joker/sum.png";
            case FLASH:
                return "joker/bun.png";
            case CATCH:
                return "joker/catch.png";
            case NORMAL:
            default:
                String assetRank = rank.toLowerCase();
                return "normal/" + food.getAssetPrefix() + assetRank + ".png";
        }
    }
}
