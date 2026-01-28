package com.techx.tradex.common.utils;

import lombok.Setter;

public class JobState {
    public final static int INIT = 0;
    public final static int DOING = 1;
    public final static int DONE = 2;
    public final static int FAIL = 3;
    @Setter
    private IEvent event;
    private volatile short state = INIT;

    public boolean isNotDo() {
        return this.state == INIT;
    }

    public boolean isDoing() {
        return this.state == DOING;
    }

    public boolean isDone() {
        return this.state == DONE;
    }

    public boolean isFailed() {
        return this.state >= FAIL;
    }

    public boolean isInit() {
        return this.state == INIT;
    }

    public short failCount() {
        return (short) (this.state - FAIL - 1);
    }

    public void doing() {
        this.state = DOING;
        if (this.event != null) this.event.doing();
    }

    public void done() {
        this.state = DONE;
        if (this.event != null) this.event.finish();
    }

    public void fail() {
        if (this.state >= FAIL) {
            this.state++;
        } else {
            this.state = FAIL;
        }
        if (this.event != null) this.event.fail(this.failCount());
    }

    public interface IEvent {
        void finish();
        void doing();
        void fail(int times);
    }
}
