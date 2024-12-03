package vn.fsaproject.carental.constant;

public enum BookingStatus {
    PENDING_DEPOSIT("Pending Deposit"),
    CONFIRMED("Confirmed"),
    AWAITING_PICKUP_CONFIRMATION("Awaiting Pickup Confirmation"),
    IN_PROGRESS("In Progress"),
    PENDING_PAYMENT("Pending Payment"),
    COMPLETED("Completed"),
    CANCELED("Canceled");
    ;
    private String message;

    public String getMessage() {
        return message;
    }

    BookingStatus(String message) {
        this.message = message;
    }
}
