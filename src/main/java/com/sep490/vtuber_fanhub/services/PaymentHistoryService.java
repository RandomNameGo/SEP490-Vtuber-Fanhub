package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePaymentRequest;

public interface PaymentHistoryService {
    void createPayment(CreatePaymentRequest request, long id);
    void updatePaymentStatus(long id, String status);
}
