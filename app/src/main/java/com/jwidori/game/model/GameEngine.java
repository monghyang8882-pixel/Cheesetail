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

        drawPile.add(Card.hideJoker());
        drawPile.add(Card.flashJoker());
        drawPile.add(Card.catchJoker());

        Collections.shuffle(drawPile, random);

        for (int c = 0; c < cardsPerPlayer; c++) {
            for (int p = 0; p < playerCount; p++) {
                Card dealt = drawOne();
                if (dealt != null) {
                    hands.get(p).add(dealt);
                }
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

    private void drawCards(int player, int count) {
        for (int i = 0; i < count; i++) {
            Card card = drawOne();
            if (card == null) {
                return;
            }
            hands.get(player).add(card);
        }
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

        applyCardEffect(card, player);
        return true;
    }

    private void applyCardEffect(Card card, int player) {
        switch (card.getKind()) {
            case HIDE:
                currentPlayer = player;
                break;
            case FLASH: {
                int target = nextPlayer(player);
                drawCards(target, 3);
                currentPlayer = nextPlayer(target);
                break;
            }
            case CATCH: {
                int target = nextPlayer(player);
                drawCards(target, 5);
                currentPlayer = nextPlayer(target);
                break;
            }
            case NORMAL:
            default:
                currentPlayer = nextPlayer(player);
                break;
        }
    }

    public boolean drawAndPass(int player) {
        if (winner >= 0 || player != currentPlayer) {
            return false;
        }
        Card card = drawOne();
        if (card != null) {
            hands.get(player).add(card);
        }
        currentPlayer = nextPlayer(player);
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

    private int nextPlayer(int player) {
        return (player + 1) % playerCount;
    }
}
