package info.japos.pp.models.enums;

/**
 * Created by HWAHYUDI on 24-Dec-17.
 */

public enum PresensiStatus {
    H("Hadir", "#00E676"), // green
    A("Alpa", "#FF3D00"), // red
    I("Izin", "#FF9100"); // orange

    private String value, color;

    PresensiStatus(final String value, final String color) {
        this.value = value;
        this.color = color;
    }

    public String getValue() {
        return value;
    }

    public String getColor() { return color; }

    @Override
    public String toString() {
        return this.getValue();
    }
}
