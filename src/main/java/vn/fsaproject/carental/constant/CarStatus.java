package vn.fsaproject.carental.constant;

public enum CarStatus {
    BOOKED("Booked"),
    AVAILABLE("Available"),
    STOPPED("Stopped")
    ;
    CarStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    private String message;
}
