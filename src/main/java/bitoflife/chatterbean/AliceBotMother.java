package bitoflife.chatterbean;

import bitoflife.chatterbean.parser.AliceBotParser;
import bitoflife.chatterbean.util.Searcher;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;

public class AliceBotMother {

    private ByteArrayOutputStream gossip;
    private String corpusFile;

    public void setCorpusFile(String corpusFile) {
        this.corpusFile = corpusFile;
    }

    public void setUp() {
        gossip = new ByteArrayOutputStream();
    }

    public String gossip() {
        return gossip.toString();
    }

    public AliceBot newInstance() throws Exception {
        Searcher searcher = new Searcher();
        AliceBotParser parser = new AliceBotParser();
        AliceBot bot = parser.parse(new ClassPathResource("bots/context.xml").getInputStream(),
                new ClassPathResource("bots/splitters.xml").getInputStream(),
                new ClassPathResource("bots/substitutions.xml").getInputStream(),
                searcher.search(System.getProperty("user.dir") + System.getProperty("file.separator") + "corpus", corpusFile));
        Context context = bot.getContext();
        context.outputStream(gossip);
        return bot;
    }

}
