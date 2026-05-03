package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePaymentRequest;
import com.sep490.vtuber_fanhub.dto.responses.PaymentHistoryResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface PaymentHistoryService {
    void createPayment(CreatePaymentRequest request, long id);
    void updatePaymentStatus(long id, String status);
    List<PaymentHistoryResponse> getPaymentHistoryByCurrentUser(HttpServletRequest request);
}
