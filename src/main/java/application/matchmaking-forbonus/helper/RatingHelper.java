package application.matchmaking.helper;

import application.matchmaking.model.Match;
import application.matchmaking.model.Player;

public interface RatingHelper {

    /**
     * <p>
     * This method initializes the rating of each player.
     * </p>
     */
    void initRating(Player player);

    /**
     * <p>
     * This method should be called after each match.
     * It updates each player's rating based on their match result.
     * </p>
     */
    void updateRatingAfterMatch(Match match);

}