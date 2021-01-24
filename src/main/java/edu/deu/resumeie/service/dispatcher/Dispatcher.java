package edu.deu.resumeie.service.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Dispatcher {

    private static final Logger logger = LogManager.getLogger(Dispatcher.class);

    private static final int MAX_DISPATCH = 10000;
    private static final long ALIVE_TIMEOUT = 3 * 60 * 1000; // 3 minutes

    private final List<Integer> allIds;
    private final Random random;
    private final Map<Integer, TimerTask> tasks;
    private final Timer mainTimer;

    private final Map<Integer, ?> reference;

    public <T extends Map<Integer, ?>> Dispatcher(T reference){
        allIds = new ArrayList<>();
        for (int i = 0; i <= MAX_DISPATCH; i++){
            allIds.add(i);
        }
        random = new Random();
        tasks = new HashMap<>();
        this.reference = reference;
        mainTimer = new Timer();
    }

    public synchronized int getId(){
        int rnd = random.nextInt(allIds.size());
        int id = allIds.remove(rnd);
        startTimer(id);
        return id;
    }

    private void startTimer(int id){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                allIds.add(id);
                reference.remove(id);
                logger.info(String.format("Result %d has been deleted due to timeout", id));
            }
        };
        mainTimer.schedule(task, ALIVE_TIMEOUT);
        tasks.put(id, task);
    }

    private void stopTimer(int id){
        if (tasks.containsKey(id)){
            tasks.get(id).cancel();
            tasks.remove(id);
        }
    }

    public void resetTimer(int id){
        stopTimer(id);
        startTimer(id);
    }

}
