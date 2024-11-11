package be.pbo.jeugdcup.ranking.infrastructure.db;

import be.pbo.jeugdcup.ranking.domain.EventNameWithDate;
import be.pbo.jeugdcup.ranking.domain.Match;
import be.pbo.jeugdcup.ranking.domain.Player;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public class TpRepositoryCardImpl extends TpRepositoryImpl {

    public TpRepositoryCardImpl(Path filePath) {
        super(filePath);
    }

    @Override
    public List<Match> getMatches() {
        if (playerById.isEmpty()) {
            this.getPlayers();
        }
        if (drawById.isEmpty()) {
            this.getDraws();
        }
        if (teamById.isEmpty()) {
            this.fillTeams();
        }


        final List<Match> result = new ArrayList<>();
        final String query = "SELECT hometeam.entry hometeam_entry_id, awayteam.entry awayteam_entry_id, "
                + "thematch.team1set1, thematch.team2set1, "
                + "thematch.team1set2, thematch.team2set2, "
                + "thematch.team1set3, thematch.team2set3, "
                + "thematch.matchnr, thematch.roundnr, "
                + "thematch.draw, thematch.winner, thematch.id, thematch.scorestatus, thematch.plandate " + "FROM PlayerMatch thematch "
                + "INNER JOIN PlayerMatch AS hometeam ON thematch.van1 = hometeam.planning "
                + "INNER JOIN PlayerMatch AS awayteam ON thematch.van2 = awayteam.planning "
                + "AND thematch.draw = hometeam.draw AND thematch.draw = awayteam.draw " + "AND reversehomeaway=FALSE "
                + "AND thematch.winner < 1 " //Bye's have already a winner. Not interested in these matches.
                + "ORDER BY thematch.plandate;";

        try (final ResultSet rs = executeSql(query)) {
            while (rs.next()) {
                final Match match = Match.builder()
                        .team1(teamById.get(rs.getInt("hometeam_entry_id")))
                        .team2(teamById.get(rs.getInt("awayteam_entry_id")))
                        .set1(buildSet(rs, "team1set1", "team2set1"))
                        .set2(buildSet(rs, "team1set2", "team2set2"))
                        .set3(buildSet(rs, "team1set3", "team2set3"))
                        //.winner(teamById.get(rs.getInt(rs.getInt("winner"))))
                        .draw(drawById.get(rs.getInt("draw")))
                        .matchnr(rs.getInt("matchnr"))
                        .roundnr(rs.getInt("roundnr"))
                        .id(rs.getInt("id"))
                        .isWalkOverMatch(rs.getInt("scorestatus") == 1)
                        .isLostByGivingUp(rs.getInt("scorestatus") == 2)
                        .planDate(new java.util.Date(rs.getTimestamp("plandate").getTime()))
                        .build();
                if (!(match.getTeam1() == null && match.getTeam2()== null)) {
                    result.add(match);
                }
                //else {
                //    System.out.println("Match without teams: " + match);
                //}
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Unable to get matches", e);
        }

        return result;
    }

    public Map<Player, List<EventNameWithDate>> getFirstMatchPerPlayerPerEventType(List<Match> matches) {
       HashMap<Player, List<EventNameWithDate>> result = new HashMap<>();

       matches.forEach(match -> {

           List<Player> players = new ArrayList<>();
           if (match.getTeam1() != null) {
               players.add(match.getTeam1().getPlayer1());
               players.add(match.getTeam1().getPlayer2());
           }
           if (match.getTeam2() != null) {
               players.add(match.getTeam2().getPlayer1());
               players.add(match.getTeam2().getPlayer2());
           }

           players.stream()
                   .filter(Objects::nonNull)
                   .forEach(player -> {
                        result.computeIfAbsent(player, k -> new ArrayList<>());
                        result.computeIfPresent(player, (k, v) ->  {
                            EventNameWithDate eventNameWithDate = new EventNameWithDate(match.getDraw().getEvent().getName(), match.getPlanDate(), match);

                            Optional<EventNameWithDate> existingEventWithSameName = v.stream().filter(x -> x.getEventName().equals(eventNameWithDate.getEventName())).findFirst();
                            if (!existingEventWithSameName.isPresent()) {
                                v.add(eventNameWithDate);
                            } else if ((eventNameWithDate.isPlanned() && !existingEventWithSameName.get().isPlanned()) || eventNameWithDate.getDate().before(existingEventWithSameName.get().getDate())) {
                                existingEventWithSameName.get().from(eventNameWithDate);
                            }
                            return v;
                        });
                   });


       });

       result.forEach((k,v) -> {
              Collections.sort(v, (o1, o2) -> {
                  if (o1.isPlanned() && o2.isPlanned()) {
                      return o1.getDate().compareTo(o2.getDate());
                  } else if (o1.isPlanned()) {
                        return -1;
                  } else if (o2.isPlanned()) {
                      return 1;
                  } else {
                      return o1.getEventName().compareTo(o2.getEventName());
                  }
              });
         });

       return result;
    }

}
