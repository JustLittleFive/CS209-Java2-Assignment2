package application.matchmaking.helper;

import application.matchmaking.model.Player;

import java.util.List;

import static java.lang.Math.abs;

public class GreedyPlayerFinder implements PlayerFinder {

    private double rangeOffset;

    public GreedyPlayerFinder(double rangeOffset) {
        this.rangeOffset = rangeOffset;
    }

    /**
     * <p>
     * Iterate through the list of players. A player will be picked if his rating is within the range of the current player's rating.
     * It will stop when the required number of players have been found and ignore the others.
     * </p>
     */
    public Player findSimilarPlayer(Player currentPlayer, List<Player> playerPool) {
        Player similarPlayer = null;
        for (Player player : playerPool) {
            double absoluteRatingDiff = abs(currentPlayer.getRating() - player.getRating()) / currentPlayer.getRating();
            if (absoluteRatingDiff <= rangeOffset) {
                similarPlayer = player;
            }
            if (similarPlayer != null) {
                break;
            }
        }
        return similarPlayer;
    }

}
