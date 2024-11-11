package be.pbo.jeugdcup.ranking.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventNameWithDate {
    private static Date plannedDateCutOff = new Date(2024 - 1900,10,1);
    private String eventName;
    private Date date;
    private Match match;

    public boolean isPlanned() {
        return date != null && getDate().after(plannedDateCutOff);
    }

    public EventNameWithDate from(EventNameWithDate another) {
        this.setMatch(another.getMatch());
        this.setDate(another.getDate());
        this.setEventName(another.getEventName());

        return this;
    }

    public String teamVsTeamVisual() {
        return match.getTeam1().toStringVisual() + " vs " + match.getTeam2().toStringVisual();
    }

    public List<Player> oppositeTeamPlayers(String memberId) {
        List<Player> result = new ArrayList<>();
        if (
                (match.getTeam1() != null && match.getTeam1().getPlayer1() != null && match.getTeam1().getPlayer1().getMemberId().equals(memberId))
                        || (match.getTeam1() != null && match.getTeam1().getPlayer2() != null && match.getTeam1().getPlayer2().getMemberId().equals(memberId))
        ) {
            if (match.getTeam2() != null ) {
                addIfNotNull(result, match.getTeam2().getPlayer1());
                addIfNotNull(result, match.getTeam2().getPlayer2());
            }
        } else if ((
                (match.getTeam2() != null && match.getTeam2().getPlayer1() != null && match.getTeam2().getPlayer1().getMemberId().equals(memberId))
                        || (match.getTeam2() != null && match.getTeam2().getPlayer2() != null && match.getTeam2().getPlayer2().getMemberId().equals(memberId))
        )) {
            if (match.getTeam1() != null) {
                addIfNotNull(result, match.getTeam1().getPlayer1());
                addIfNotNull(result, match.getTeam1().getPlayer2());
            }
        }
        return result;
    }

    private void addIfNotNull(List<Player> result, Player player) {
        if (player != null) {
            result.add(player);
        }
    }
}
