package com.difisoft.marketcollector.services.realtime;

public class StartOrRetry extends Command {
    final long version;
    final Long delay;

    public StartOrRetry(CommandType type, long version) {
        super(type);
        this.version = version;
        delay = null;
    }

    public StartOrRetry(long version, long delay) {
        super(CommandType.DISCONNECTED);
        this.version = version;
        this.delay = delay;
    }

    public StartOrRetry(long delay) {
        super(CommandType.START);
        this.version = 0;
        this.delay = delay;
    }
}
