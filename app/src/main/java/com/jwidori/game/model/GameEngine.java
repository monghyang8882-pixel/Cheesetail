package com.jwidori.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameEngine {

    private final int playerCount;
    private final List<Card> drawPile = new ArrayList<>();
    private final List<List<Card>> hands = new ArrayList<>();
    private Card topCard;
    private int currentPlayer = 0;
    private int winner = -1;

    public GameEngine(int playerCount, int cardsPerPlayer, Random random) {
        if (playerCount < 2 || playerCount > 4) {
            throw new IllegalArgumentException("playerCount must be 2-4");
        }
        this.playerCount = playerCount;

        for (int i = 0; i < playerCount; i++) {
            hands.add(new ArrayList<>());
        }

        String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        for (Card.Food food : Card.Food.values()) {
            for (String rank : ranks) {
                drawPile.add(new Card(food, rank));
            }
        }

        Collections.shuffle(drawPile, random);

        for (int c = 0; c < cardsPerPlayer; c++) {
            for (int p = 0; p < playerCount; p++) {
                hands.get(p).add(drawOne());
            }
        }

        topCard = drawOne();
    }

    private Card drawOne() {
        if (drawPile.isEmpty()) {
            return null;
        }
        return drawPile.remove(drawPile.size() - 1);
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public Card getTopCard() {
        return topCard;
    }

    public int getWinner() {
        return winner;
    }

    public int getDrawPileCount() {
        return drawPile.size();
    }

    public List<Card> getHand(int player) {
        return Collections.unmodifiableList(hands.get(player));
    }

    public int getHandSize(int player) {
        return hands.get(player).size();
    }

    public boolean canPlay(Card card) {
        return card != null && card.matches(topCard);
    }

    public boolean playCard(int player, int handIndex) {
        if (winner >= 0 || player != currentPlayer) {
            return false;
        }
        List<Card> hand = hands.get(player);
        if (handIndex < 0 || handIndex >= hand.size()) {
            return false;
        }
        Card card = hand.get(handIndex);
        if (!canPlay(card)) {
            return false;
        }

        hand.remove(handIndex);
        topCard = card;

        if (hand.isEmpty()) {
            winner = player;
            return true;
        }

        advanceTurn();
        return true;
    }

    public boolean drawAndPass(int player) {
        if (winner >= 0 || player != currentPlayer) {
            return false;
        }
        Card card = drawOne();
        if (card != null) {
            hands.get(player).add(card);
        }
        advanceTurn();
        return true;
    }

    public int findFirstPlayableIndex(int player) {
        List<Card> hand = hands.get(player);
        for (int i = 0; i < hand.size(); i++) {
            if (canPlay(hand.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private void advanceTurn() {
        currentPlayer = (currentPlayer + 1) % playerCount;
    }
}
