package es.um.asio.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class WatchDog {

    private long timeBefore;

    public WatchDog() {
        timeBefore = new Date().getTime();
    }

    public long calculateDelay() {
        return new Date().getTime()-timeBefore;
    }
}
