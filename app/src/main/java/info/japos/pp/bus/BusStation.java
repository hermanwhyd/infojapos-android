package info.japos.pp.bus;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusStation {
    private static final Bus BUS = new Bus(ThreadEnforcer.ANY);

    public static Bus getBus() {
        return BUS;
    }

    private BusStation() {
    }
}
