package edu.rit.cs.cs_word_count;

public class MyTimer {
    private String timerName;
    private long startTime;
    private long endTime;
    private long elapsedTime;

    public MyTimer(String timerName){
        this.timerName = timerName;
    }

    public void start_timer(){
        this.startTime = System.nanoTime();
    }

    public void stop_timer(){
        this.endTime = System.nanoTime();
    }

    public void print_elapsed_time(){
        this.elapsedTime = this.endTime - this.startTime;
        double elapsedTimeInSecond = (double) this.elapsedTime / 1_000_000_000;
        System.out.println("ElapsedTime (" + this.timerName + "): "+ elapsedTimeInSecond + " seconds");
    }

}
