package application.matchmaking;

import application.matchmaking.helper.PlayerFinder;
import application.matchmaking.helper.TeamBuilder;
import application.matchmaking.model.Match;
import application.matchmaking.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * The matchmaking implementation that you will write.
 */
public class MatchmakerImpl implements Matchmaker {

    private PlayerFinder playerFinder;
    private TeamBuilder matchBuilder;
    private List<Player> playersPool;

    public MatchmakerImpl(PlayerFinder playerFinder, TeamBuilder matchBuilder) {
        playersPool = new ArrayList<Player>();
        this.playerFinder = playerFinder;
        this.matchBuilder = matchBuilder;
    }

    public Match findMatch() {
        int playersRequired = 2;
        int playerSize = playersPool.size();
        if (playerSize < playersRequired) {
            return null;
        }
        Player firstPlayer = playersPool.remove(0);
        Player selectedPlayer = playerFinder.findSimilarPlayer(firstPlayer, playersPool);
        if (selectedPlayer == null) {
            // Add the player back to the end of the waiting list.
            // this can be enhanced to have more robust retrying logic to optimize the waiting time.
            playersPool.add(firstPlayer);
            return null;
        }
        playersPool.remove(selectedPlayer);
        // selectedPlayer.add(firstPlayer);
        ArrayList<Player> matchedPlayers = new ArrayList<>();
        matchedPlayers.add(firstPlayer);
        matchedPlayers.add(selectedPlayer);
        return matchBuilder.splitPlayersIntoMatch(matchedPlayers);
    }

    public void enterMatchmaking(Player player) {
        if (!playersPool.contains(player)) {
            playersPool.add(player);
        }
    }

    public List<Player> getPlayers() {
        return playersPool;
    }

}
