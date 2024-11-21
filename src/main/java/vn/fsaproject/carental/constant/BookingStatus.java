package vn.fsaproject.carental.constant;

public enum BookingStatus {
    PENDING_DEPOSIT("Pending Deposit"),
    CONFIRMED("Confirmed"),
    IN_PROGRESS("In Progress"),
    PENDING_PAYMENT("Pending Payment"),
    COMPLETED("Booking Completed"),
    CANCELED("Cancel Booking");
    ;
    private String message;

    public String getMessage() {
        return message;
    }

    BookingStatus(String message) {
        this.message = message;
    }
}
