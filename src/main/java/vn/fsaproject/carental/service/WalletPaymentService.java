package vn.fsaproject.carental.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import vn.fsaproject.carental.utils.PaymentService;

@Component("wallet")
public class WalletPaymentService implements PaymentService {
    @Override
    public String createOrder(HttpServletRequest request, int amount, String orderInfo, String returnUrl) {
        return "";
    }

    @Override
    public String getPaymentType() {
        return "wallet";
    }
}
