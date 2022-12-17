package com.example.kaplat.server;

import com.example.kaplat.server.enums.ThreadContextKey;
import lombok.Getter;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Getter
public class RequestCounterController {
    private AtomicInteger requestCounter = new AtomicInteger(0);

    public void increaseCounter() {
        this.requestCounter.set(this.requestCounter.get() + 1);
        ThreadContext.put(ThreadContextKey.REQUEST_COUNTER.toString(), this.requestCounter.toString());
    }
}
