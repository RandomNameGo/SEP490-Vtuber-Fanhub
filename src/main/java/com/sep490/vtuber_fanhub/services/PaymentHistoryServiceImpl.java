package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePaymentRequest;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.PaidPackage;
import com.sep490.vtuber_fanhub.models.PaymentHistory;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.PaidPackageRepository;
import com.sep490.vtuber_fanhub.repositories.PaymentHistoryRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentHistoryServiceImpl implements PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserRepository userRepository;
    private final PaidPackageRepository paidPackageRepository;

    @Override
    @Transactional
    public void createPayment(CreatePaymentRequest request, long id) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getUserId()));

        PaidPackage paidPackage = paidPackageRepository.findById(request.getPaidPackageId())
                .orElseThrow(() -> new NotFoundException("Paid package not found with id: " + request.getPaidPackageId()));

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setId(id);
        paymentHistory.setUser(user);
        paymentHistory.setAmount(request.getPrice());
        paymentHistory.setDescription(request.getPaidPackageDescription());
        paymentHistory.setPackageField(paidPackage);
        paymentHistory.setCreatedAt(Instant.now());
        paymentHistory.setStatus("PENDING");

        paymentHistoryRepository.save(paymentHistory);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(long id, String status) {
        PaymentHistory paymentHistory = paymentHistoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment history not found with id: " + id));

            paymentHistory.setStatus("SUCCESS");
            
            User user = paymentHistory.getUser();
            PaidPackage paidPackage = paymentHistory.getPackageField();
            
            if (paidPackage != null) {
                long currentPoints = user.getPaidPoints() != null ? user.getPaidPoints() : 0L;
                user.setPaidPoints(currentPoints + paidPackage.getPaidPoints());
                userRepository.save(user);
            }
            
            paymentHistoryRepository.save(paymentHistory);

    }
}
