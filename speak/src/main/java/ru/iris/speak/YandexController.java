package ru.iris.speak;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import ru.iris.commons.bus.speak.SpeakEvent;
import ru.iris.commons.config.ConfigLoader;
import ru.iris.commons.database.dao.SpeakDAO;
import ru.iris.commons.database.model.Speaks;
import ru.iris.commons.service.AbstractService;
import ru.iris.commons.service.Speak;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;

@Component
@Profile("yandex")
@Slf4j
public class YandexController extends AbstractService implements Speak {

    private static final String YANDEX_SYNTHESISER_URL = "https://tts.voicetech.yandex.net/generate";
    private final ConfigLoader config;
    private final ArrayBlockingQueue<SpeakEvent> queue = new ArrayBlockingQueue<>(50);
    private final SpeakDAO speakDAO;
    @Autowired
    private EventBus r;
    private Map<String, Long> cache = new HashMap<>();
    private String API_KEY;
    private String language;
    private String speaker;

    @Autowired
    public YandexController(SpeakDAO speakDAO, ConfigLoader config) {
        this.speakDAO = speakDAO;
        this.config = config;
    }

    @Override
    public void onStartup() {
        logger.info("Starting up Yandex Speak service");
        if (!config.loadPropertiesFormCfgDirectory("speak"))
            logger.error("Cant load speak-specific configs. Check speak.property if exists");
    }

    @PostConstruct
    public void aSayHello() {
        r.notify("event.speak", Event.wrap(new SpeakEvent("Запускается модуль синтеза речи")));
    }

    @Override
    public void onShutdown() {
        logger.info("Shutdown Yandex Speak service");
    }

    @Override
    public void subscribe() throws Exception {
        addSubscription("event.speak");
    }

    @Override
    @Async
    public void run() {

        logger.info("Starting Yandex listen thread");

        API_KEY = config.get("yandexApiKey");
        setLanguage(config.get("yandexLanguage"));
        setSpeaker(config.get("yandexVoice"));

        // caching
        for (Speaks speak : speakDAO.findAll()) {
            cache.put(speak.getText(), speak.getCache());
        }

        while (true) {
            SpeakEvent adv = null;
            try {
                adv = queue.poll(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                logger.error("Error: {}", ex.getLocalizedMessage());
            }
            if (adv == null)
                continue;

            logger.debug("Something coming into the pool!");

            String text = adv.getText();

            if (cache.containsKey(text)) {
                logger.info("Saying from cache: {} ({})", text, "data/cache-" + cache.get(text) + ".mp3");
            } else {
                logger.info("Saying new phrase: {}", text);
                long cacheIdent = new Date().getTime();
                OutputStream outputStream = null;
                InputStream resultForWrite = null;
                InputStream result = null;

                try {
                    outputStream = new FileOutputStream(new File("data/cache-" + cacheIdent + ".mp3"));
                    logger.info("Trying to get MP3 data");
                    result = getMP3Data(text);

                    byte[] byteArray = IOUtils.toByteArray(result);

                    resultForWrite = new ByteArrayInputStream(byteArray);

                    int read;
                    byte[] bytes = new byte[1024];

                    while ((read = resultForWrite.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }

                    logger.info("Saved");
                } catch (IOException ex) {
                    logger.error("Error: {}", ex.getLocalizedMessage());
                    return;
                } finally {
                    try {
                        if (resultForWrite != null)
                            resultForWrite.close();
                        if (outputStream != null)
                            outputStream.close();
                        if (result != null)
                            result.close();
                    } catch (IOException ex) {
                        logger.error("Error: {}", ex.getLocalizedMessage());
                    }
                }

                logger.info("Saving cache into db");

                Speaks speak = new Speaks();
                speak.setCache(cacheIdent);
                speak.setText(text);
                speakDAO.save(speak);

                // put new value in cache
                cache.put(text, cacheIdent);
            }

            // play sound
            play(cache.get(text));
        }

    }

    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    @Override
    public Consumer<Event<?>> handleMessage() {
        return event -> {

            if (event.getData() instanceof SpeakEvent) {
                try {
                    SpeakEvent speakEvent = (SpeakEvent) event.getData();

                    // zone == null -> speak everywhere
                    if (speakEvent.getZone() == null)
                        queue.put(speakEvent);

                } catch (InterruptedException e) {
                    logger.error("Error: ", e.getLocalizedMessage());
                }
            } else {
                logger.error("Unknown advert to Speak: {}", event.getData().getClass());
            }
        };
    }

    private void play(Long cacheId) {
        InputStream result = null;
        try {
            result = new FileInputStream("data/cache-" + cacheId + ".mp3");
            Player player = new Player(result);
            player.play();
            player.close();
        } catch (FileNotFoundException | JavaLayerException e) {
            logger.error("Error while trying to play {}: {}", cacheId, e.getLocalizedMessage());
        } finally {
            try {
                if (result != null)
                    result.close();
            } catch (IOException e) {
                logger.error("Error: ", e.getLocalizedMessage());
            }
        }
    }

    public InputStream getMP3Data(String synthText) throws IOException {

        if (synthText.length() > 100) {
            List<String> fragments = parseString(synthText);//parses String if too long
            return getMP3Data(fragments);
        }

        String encoded = URLEncoder.encode(synthText, "UTF-8"); //Encode

        String sb = YANDEX_SYNTHESISER_URL + "?key=" + API_KEY +
                "&text=" + encoded +
                "&lang=" + language +
                "&format=mp3" +
                "&speaker=" + speaker;

        URL url = new URL(sb); //create url

        // Open New URL connection channel.
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection(); //Open connection
        urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0) Gecko/20100101 Firefox/4.0"); //Adding header for user agent is required

        int responseCode = urlConn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return urlConn.getInputStream();
        } else {
            logger.error("Error while downloading: " + responseCode);
            return null;
        }
    }

    /**
     * Gets an InputStream to MP3Data for the returned information from a request
     *
     * @param synthText List of Strings you want to be synthesized into MP3 data
     * @return Returns an input stream of all the MP3 data that is returned from Google
     * @throws java.io.IOException Throws exception if it cannot complete the request
     */
    private InputStream getMP3Data(List<String> synthText) throws IOException {
        //Uses an executor service pool for concurrency. Limit to 100 threads max.
        ExecutorService pool = Executors.newFixedThreadPool(100);
        //Stores the Future (Data that will be returned in the future)
        Set<Future<InputStream>> set = new LinkedHashSet<>(synthText.size());
        for (String part : synthText) { //Iterates through the list
            Callable<InputStream> callable = new MP3DataFetcher(part);//Creates Callable
            Future<InputStream> future = pool.submit(callable);//Begins to run Callable
            set.add(future);//Adds the response that will be returned to a set.
        }
        List<InputStream> inputStreams = new ArrayList<InputStream>(set.size());
        for (Future<InputStream> future : set) {
            try {
                inputStreams.add(future.get());//Gets the returned data from the future.
            } catch (ExecutionException e) {//Thrown if the MP3DataFetcher encountered an error.
                Throwable ex = e.getCause();
                if (ex instanceof IOException) {
                    throw (IOException) ex;//Downcasts and rethrows it.
                }
            } catch (InterruptedException e) {//Will probably never be called, but just in case...
                Thread.currentThread().interrupt();//Interrupts the thread since something went wrong.
            }
        }
        return new SequenceInputStream(Collections.enumeration(inputStreams));//Sequences the stream.
    }

    /**
     * Separates a string into smaller parts so that Google will not reject the request.
     *
     * @param input The string you want to separate
     * @return A List<String> of the String fragments from your input..
     */
    private List<String> parseString(String input) {
        return parseString(input, new ArrayList<String>());
    }

    /**
     * Separates a string into smaller parts so that Google will not reject the request.
     *
     * @param input     The string you want to break up into smaller parts
     * @param fragments List<String> that you want to add stuff too.
     *                  If you don't have a List<String> already constructed "new ArrayList<String>()" works well.
     * @return A list of the fragments of the original String
     */
    private List<String> parseString(String input, List<String> fragments) {
        if (input.length() <= 100) {//Base Case
            fragments.add(input);
            return fragments;
        } else {
            int lastWord = findLastWord(input);//Checks if a space exists
            if (lastWord <= 0) {
                fragments.add(input.substring(0, 100));//In case you sent gibberish to Google.
                return parseString(input.substring(100), fragments);
            } else {
                fragments.add(input.substring(0, lastWord));//Otherwise, adds the last word to the list for recursion.
                return parseString(input.substring(lastWord), fragments);
            }
        }
    }

    /**
     * Finds the last word in your String (before the index of 99) by searching for spaces and ending punctuation.
     * Will preferably parse on punctuation to alleviate mid-sentence pausing
     *
     * @param input The String you want to search through.
     * @return The index of where the last word of the string ends before the index of 99.
     */
    private int findLastWord(String input) {
        if (input.length() < 100)
            return input.length();
        int space = -1;
        for (int i = 99; i > 0; i--) {
            char tmp = input.charAt(i);
            if (isEndingPunctuation(tmp)) {
                return i + 1;
            }
            if (space == -1 && tmp == ' ') {
                space = i;
            }
        }
        if (space > 0) {
            return space;
        }
        return -1;
    }

    /**
     * Checks if char is an ending character
     * Ending punctuation for all languages according to Wikipedia (Except for Sanskrit non-unicode)
     *
     * @param input char you want check
     * @return True if it is, false if not.
     */
    private boolean isEndingPunctuation(char input) {
        return input == '.' || input == '!' || input == '?' || input == ';' || input == ':' || input == '|';
    }

    /**
     * This class is a callable.
     * A callable is like a runnable except that it can return data and throw exceptions.
     * Useful when using futures. Dramatically improves the speed of execution.
     *
     * @author Aaron Gokaslan (Skylion)
     */
    private class MP3DataFetcher implements Callable<InputStream> {

        private String synthText;

        MP3DataFetcher(String synthText) {
            this.synthText = synthText;
        }

        public InputStream call() throws IOException {
            return getMP3Data(synthText);
        }
    }
}
