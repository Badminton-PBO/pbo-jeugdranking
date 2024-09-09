package be.pbo.jeugdcup.ranking.infrastructure.db;

import be.pbo.jeugdcup.ranking.domain.EventNameWithDate;
import be.pbo.jeugdcup.ranking.domain.Match;
import be.pbo.jeugdcup.ranking.domain.Player;
import be.pbo.jeugdcup.ranking.services.CardPlayer;
import be.pbo.jeugdcup.ranking.services.CardRenderingService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TpRepositoryCardImplTest {

    private static TpRepositoryCardImpl tpRepository;

    @BeforeAll
    public static void init() throws URISyntaxException {
        // https://www.toernooi.nl/tournament/2492F915-3372-422B-BE35-579E1AF9B639
        final Path path = Paths.get(TpRepositoryImpl.class.getResource("/tpFiles/PBOJeugdcupBCDePluimplukkers2024Voor.tp").toURI());
        tpRepository = new TpRepositoryCardImpl(path);
    }

    @Test
    public void testPocBK() {
        List<Match> m = tpRepository.getMatches();

        Map<Player, List<EventNameWithDate>> result = tpRepository.getFirstMatchPerPlayerPerEventType(m);
        int i = 0;


        //Map.Entry<Player, List<EventNameWithDate>> ex1 = result.entrySet().stream().collect(Collectors.toList()).get(0);
        List<CardPlayer> cardPlayers = result.entrySet().stream()
                .map(e -> new CardPlayer(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        new CardRenderingService().renderCards(cardPlayers);


        //System.out.println(result);
    }


}