package be.pbo.jeugdcup.ranking.infrastructure.db;

import be.pbo.jeugdcup.ranking.domain.EventNameWithDate;
import be.pbo.jeugdcup.ranking.domain.Match;
import be.pbo.jeugdcup.ranking.domain.Player;
import be.pbo.jeugdcup.ranking.services.CardPlayer;
import be.pbo.jeugdcup.ranking.services.CardRenderingService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.ListUtil;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TpRepositoryCardImplTest {

    private static TpRepositoryCardImpl tpRepository;

    @BeforeAll
    public static void init() throws URISyntaxException {
        // https://www.toernooi.nl/tournament/2492F915-3372-422B-BE35-579E1AF9B639
        ///tpFiles/PBOJeugdcupBCDePluimplukkers2024Voor.tp
        final Path path = Paths.get(TpRepositoryImpl.class.getResource("/tpFiles/2024.11.07_bk_jeugd.tp").toURI());
        tpRepository = new TpRepositoryCardImpl(path);
    }

    @Test
    public void testPocBK() {
        List<Match> m = tpRepository.getMatches();

        Map<Player, List<EventNameWithDate>> result = tpRepository.getFirstMatchPerPlayerPerEventType(m);

        //Map.Entry<Player, List<EventNameWithDate>> ex1 = result.entrySet().stream().collect(Collectors.toList()).get(0);
        List<CardPlayer> cardPlayers = result.entrySet().stream()
                .map(e -> new CardPlayer(e.getKey(), e.getValue(),0,0))
                .collect(Collectors.toList());

        Collections.sort(cardPlayers);

        int partitionSize = 4;

        int partitionId = 0;
        for (int i=0; i<cardPlayers.size(); i += partitionSize) {
            partitionId++;
            List<CardPlayer> playersInPartition = cardPlayers.subList(i, Math.min(i + partitionSize, cardPlayers.size()));
            int boxId = 0;
            for (CardPlayer cp : playersInPartition) {
                boxId++;
                cp.setPartitionId(partitionId);
                cp.setBoxId(boxId);
            }
            if (boxId < partitionSize) {
                cardPlayers.add(new CardPlayer(null,null, 0, partitionSize));
            }

        }

        new CardRenderingService().renderCards(cardPlayers);


        //System.out.println(result);
    }


}