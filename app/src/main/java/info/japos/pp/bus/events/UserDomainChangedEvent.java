package info.japos.pp.bus.events;

public class UserDomainChangedEvent {
    private int identifier;

    public UserDomainChangedEvent(int identifier) {
        this.identifier = identifier;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }
}
