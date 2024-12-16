package vn.fsaproject.carental.payment.processor;
import org.springframework.stereotype.Component;
import vn.fsaproject.carental.config.VNPAYConfig;
import vn.fsaproject.carental.entities.Transaction;
import vn.fsaproject.carental.entities.User;
import vn.fsaproject.carental.payment.constant.PaymentProcessor;
import vn.fsaproject.carental.repository.TransactionRepository;
import vn.fsaproject.carental.repository.UserRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class VNPayProcessor implements PaymentProcessor {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    public VNPayProcessor(UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }
    @Override
    public String generatePaymentUrl(Transaction transaction) {
        //Các bạn có thể tham khảo tài liệu hướng dẫn và điều chỉnh các tham số
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = transaction.getTransactionId();
        String vnp_IpAddr = transaction.getIpAddress();
        String vnp_TmnCode = VNPAYConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((int)transaction.getAmount()*100));
        vnp_Params.put("vnp_CurrCode", transaction.getCurrency());

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "" + transaction.getBooking().getId());
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        vnp_Params.put("vnp_ReturnUrl", VNPAYConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String salt = VNPAYConfig.vnp_HashSecret;
        String vnp_SecureHash = VNPAYConfig.hmacSHA512(salt, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPAYConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    @Override
    public String handleCallback(Map<String, String> parameters) {

        if (VNPAYConfig.verifyHash(parameters)) {
            String status = parameters.get("vnp_ResponseCode");
            switch (status) {
                case "00":
                    double amount = Double.parseDouble(parameters.get("vnp_Amount"));
                    deductWalletBalance(amount, transactionRepository.findByTransactionId(parameters.get("vnp_TxnRef")));
                    return "SUCCESS";
                case "24":
                    return "CANCELED";
                default:
                    return "FAILED";
            }
        }
        return "INVALID";
    }

    @Override
    public String getType() {
        return "VNPAY";
    }

    private void deductWalletBalance(double amount, Transaction transaction) {
        if (transaction.getStatus().equalsIgnoreCase("PENDING")) {
            User recipient = transaction.getRecipient();

            recipient.setWallet(recipient.getWallet() == 0 ? amount : recipient.getWallet() + amount);
            userRepository.save(recipient);
        }
    }
}
