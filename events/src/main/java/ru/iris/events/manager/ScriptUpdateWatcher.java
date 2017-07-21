package ru.iris.events.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.nio.file.StandardWatchEventKinds.*;

public class ScriptUpdateWatcher implements Runnable {
    static private final Logger logger = LoggerFactory.getLogger(ScriptUpdateWatcher.class);

    private ScriptManager scriptManager;
    private File folder;

    private HashMap<File, Long> lastUpdate = new HashMap<>();

    public ScriptUpdateWatcher(ScriptManager scriptManager, File folder) {
        this.scriptManager = scriptManager;
        this.folder = folder;
    }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();

            Path dir = Paths.get(folder.getAbsolutePath());
            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

            while (true) {
                WatchKey key;

                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    logger.info("ScriptUpdateWatcher interrupted");
                    return;
                }

                long currentTime = System.currentTimeMillis();

                ArrayList<File> removedScripts = new ArrayList<>();
                ArrayList<File> addedScripts = new ArrayList<>();
                ArrayList<File> modifiedScripts = new ArrayList<>();

                for (WatchEvent<?> event : key.pollEvents()) {

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    WatchEvent.Kind<Path> kind = ev.kind();

                    Path fileName = ev.context();

                    File f = new File(folder, fileName.toString());

                    //skip files ending with ".script" (as these files are definitely no known scripting language)
                    if (f.getName().endsWith(".script")) {
                        continue;
                    }

                    Long lastTime = lastUpdate.get(f);

                    if (lastTime == null || currentTime - lastTime > 5000) {
                        logger.debug(kind.name() + ": " + fileName);
                        lastUpdate.put(f, currentTime);
                        if (kind == ENTRY_CREATE) {
                            addedScripts.add(f);
                        } else if (kind == ENTRY_DELETE) {
                            removedScripts.add(f);
                        } else if (kind == ENTRY_MODIFY) {
                            modifiedScripts.add(f);
                        }
                    }
                }

                try {
                    scriptManager.scriptsChanged(addedScripts, removedScripts, modifiedScripts);
                } catch (Exception ex) {
                    logger.error("Error during script change processing", ex);
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }

            }

        } catch (IOException e1) {
            logger.error("WatchService could not be started", e1);
        }
    }

}
