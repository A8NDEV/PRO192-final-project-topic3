package models;

public enum OrderStatus {
    PENDING("Pending"),
    PREPARING("Preparing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}