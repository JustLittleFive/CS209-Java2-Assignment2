package application.matchmaking.helper;

import application.matchmaking.model.Player;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class NearestNeighbourPlayerFinder implements PlayerFinder {

    /**
     * <p>
     * Find the players who have the closest ratings with the current player.
     * </p>
     */
    public Player findSimilarPlayer(Player currentPlayer, List<Player> playerPool) {
        if (playerPool.size() < 1) {
            return null;
        }
        List<Player> similarPlayers = playerPool.stream()
                .sorted((player1, player2) -> Double.compare(playerDistance(currentPlayer, player1), playerDistance(currentPlayer, player2)))
                .limit(1)
                .collect(Collectors.toList());
        return similarPlayers.get(0);
    }

    private double playerDistance(Player player1, Player player2) {
        return abs(player1.getRating() - player2.getRating());
    }

}
