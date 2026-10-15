package com.sushishop.payment;

final class Compensation {

    private Compensation() {
    }

    static void run(Runnable action, Runnable compensation) {
        try {
            action.run();
        } catch (RuntimeException e) {
            compensation.run();
            throw e;
        }
    }
}
