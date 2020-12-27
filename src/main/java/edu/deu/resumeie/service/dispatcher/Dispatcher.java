package edu.deu.resumeie.service.dispatcher;

import java.util.*;


public class Dispatcher {

    private static final int TOTAL_ID = 10000;
    private static final long TIMER_TIME = 20 * 60 * 1000; // 20 minutes

    private final List<Integer> allIds;
    private final Random random;
    private final Map<Integer, TimerTask> tasks;

    private final Map<Integer, ?> reference;

    public <T extends Map<Integer, ?>> Dispatcher(T reference){
        allIds = new ArrayList<>();
        for (int i = 0; i <= TOTAL_ID; i++){
            allIds.add(i);
        }
        random = new Random();
        tasks = new HashMap<>();
        this.reference = reference;
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
                System.out.printf("Result %d has been deleted due to timeout%n", id);
            }
        };
        new Timer().schedule(task, TIMER_TIME);
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
