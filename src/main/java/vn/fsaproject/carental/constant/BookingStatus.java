package vn.fsaproject.carental.constant;

public enum BookingStatus {
    PENDING_DEPOSIT("Pending Deposit"),
    CONFIRMED("Confirmed"),
    AWAITING_PICKUP_CONFIRMATION("Awaiting Pickup Confirmation"),
    DEPOSIT_PAID("Deposit Paid"),
    IN_PROGRESS("In Progress"),
    PENDING_PAYMENT("Pending Payment"),
    PAYMENT_PAID("Payment Paid"),
    COMPLETED("Completed"),
    CANCELED("Canceled"),
    PENDING_REFUND("Pending Refund"),
    REFUND_PAID("Refund Paid");
    ;
    private String message;

    public String getMessage() {
        return message;
    }

    BookingStatus(String message) {
        this.message = message;
    }
}
