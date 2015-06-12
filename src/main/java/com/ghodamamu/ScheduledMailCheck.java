package com.ghodamamu;

import java.util.Timer;

/**
 * Created by anand on 11/6/15.
 */
public class ScheduledMailCheck {
    public static void main(String args[]) throws InterruptedException {

        Timer time = new Timer();
        Main main = new Main();
        time.schedule(main, 0, 10000);

    }
}
