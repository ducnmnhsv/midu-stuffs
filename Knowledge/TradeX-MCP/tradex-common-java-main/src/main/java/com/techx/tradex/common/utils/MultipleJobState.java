package com.techx.tradex.common.utils;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class MultipleJobState {
    private List<JobState> states = new ArrayList<>();
    @Setter
    private Operator doWhenAllFinished;
    @Setter
    private Operator doWhenAllSuccess;
    @Setter
    private Operator doWhenFirstFinished;
    @Setter
    private Operator doWhenFirstSuccess;
    private int totalState = 0;
    private int totalFinished = 0;
    private int totalSuccess = 0;
    private int totalFail = 0;
    @Setter
    private int maxRetry = 0;

    public JobState add() {
        MultipleJobState self = this;
        JobState jobState = new JobState();
        states.add(jobState);
        totalState++;
        jobState.setEvent(new JobState.IEvent() {
            @Override
            public void finish() {
                self.totalFinished++;
                self.totalSuccess++;
                self.checkState();
            }

            @Override
            public void doing() {
// nothing
            }

            @Override
            public void fail(int times) {
                if (times >= self.maxRetry) {
                    self.totalFinished++;
                    self.totalFail++;
                    self.checkState();
                }
            }
        });
        return jobState;
    }

    private void checkState() {
        if (this.totalFinished == 1 && this.doWhenFirstFinished != null) this.doWhenFirstFinished.operate();
        if (this.totalSuccess == 1 && this.doWhenFirstSuccess != null) this.doWhenFirstSuccess.operate();
        if (totalState == totalFinished) {
            if (this.doWhenAllFinished != null) this.doWhenAllFinished.operate();
            if (this.totalFail == 0 && this.doWhenAllSuccess != null) this.doWhenAllSuccess.operate();
        }
    }
}
