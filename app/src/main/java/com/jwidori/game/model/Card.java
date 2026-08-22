package com.jwidori.game.model;

public class Card {

    public enum Food {
        CHEESE("치즈", "🧀"),
        BREAD("빵", "🍞"),
        SAUSAGE("소세지", "🌭"),
        COOKIE("쿠키", "🍪");

        private final String koreanName;
        private final String icon;

        Food(String koreanName, String icon) {
            this.koreanName = koreanName;
            this.icon = icon;
        }

        public String getKoreanName() {
            return koreanName;
        }

        public String getIcon() {
            return icon;
        }
    }

    private final Food food;
    private final String rank;

    public Card(Food food, String rank) {
        this.food = food;
        this.rank = rank;
    }

    public Food getFood() {
        return food;
    }

    public String getRank() {
        return rank;
    }

    public boolean matches(Card other) {
        return other != null && (food == other.food || rank.equals(other.rank));
    }

    public String getDisplayText() {
        return food.getIcon() + "\n" + rank;
    }

    public String getDescription() {
        return food.getKoreanName() + " " + rank;
    }
}
