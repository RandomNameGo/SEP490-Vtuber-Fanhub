package com.sep490.vtuber_fanhub.services;

import com.sep490.vtuber_fanhub.dto.requests.CreatePaymentRequest;
import com.sep490.vtuber_fanhub.dto.responses.PaymentHistoryResponse;
import com.sep490.vtuber_fanhub.exceptions.NotFoundException;
import com.sep490.vtuber_fanhub.models.PaidPackage;
import com.sep490.vtuber_fanhub.models.PaymentHistory;
import com.sep490.vtuber_fanhub.models.User;
import com.sep490.vtuber_fanhub.repositories.PaidPackageRepository;
import com.sep490.vtuber_fanhub.repositories.PaymentHistoryRepository;
import com.sep490.vtuber_fanhub.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentHistoryServiceImpl implements PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final UserRepository userRepository;
    private final PaidPackageRepository paidPackageRepository;
    private final AuthService authService;

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
        PaymentHistory paymentHistory = paymentHistoryRepository.findByIdWithUserAndPackage(id)
                .orElseThrow(() -> new NotFoundException("Payment history not found with id: " + id));

        paymentHistory.setStatus(status);

        if ("SUCCESS".equals(status)) {
            User user = paymentHistory.getUser();
            PaidPackage paidPackage = paymentHistory.getPackageField();

            if (paidPackage != null) {
                long currentPoints = user.getPaidPoints() != null ? user.getPaidPoints() : 0L;
                user.setPaidPoints(currentPoints + paidPackage.getPaidPoints());
                userRepository.save(user);
            }
        }

        paymentHistoryRepository.save(paymentHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistoryByCurrentUser(HttpServletRequest request) {
        User user = authService.getUserFromToken(request);
        List<PaymentHistory> historyList = paymentHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return historyList.stream()
                .map(ph -> PaymentHistoryResponse.builder()
                        .id(ph.getId())
                        .amount(ph.getAmount())
                        .description(ph.getDescription())
                        .createdAt(ph.getCreatedAt())
                        .packageName(ph.getPackageField() != null ? ph.getPackageField().getPackageName() : null)
                        .status(ph.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
