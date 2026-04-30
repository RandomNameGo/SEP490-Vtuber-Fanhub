package com.sep490.vtuber_fanhub.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sep490.vtuber_fanhub.dto.requests.CreatePaymentRequest;
import com.sep490.vtuber_fanhub.dto.responses.APIResponse;
import com.sep490.vtuber_fanhub.dto.responses.PaidPackageResponse;
import com.sep490.vtuber_fanhub.services.PaidPackageService;
import com.sep490.vtuber_fanhub.services.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("vhub/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PayOS payOS;

    private final PaymentHistoryService paymentHistoryService;

    private final PaidPackageService paidPackageService;

    @GetMapping("/packages")
    public ResponseEntity<?> getAllPaidPackages() {
        List<PaidPackageResponse> packages = paidPackageService.getAllPaidPackages();
        return ResponseEntity.ok().body(APIResponse.<List<PaidPackageResponse>>builder()
                .success(true)
                .message("Success")
                .data(packages)
                .build()
        );
    }

    @GetMapping("/success")
    public ResponseEntity<?> paymentSuccess() {
        return ResponseEntity.ok("success");
    }

    @GetMapping("/cancel/{id}")
    public ResponseEntity<?> paymentCancel(@PathVariable long id) {
        paymentHistoryService.updatePaymentStatus(id, "CANCELLED");
        return ResponseEntity.ok().body(APIResponse.builder()
                .success(true)
                .message("Payment cancelled")
                .build());
    }

    @PostMapping(path = "/webhook")
    public ObjectNode payosTransferHandler(@RequestBody ObjectNode body)
            throws JsonProcessingException, IllegalArgumentException {

        System.out.println(body.toPrettyString());
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();
        Webhook webhookBody = objectMapper.treeToValue(body, Webhook.class);


        try {
            response.put("error", 0);
            response.put("message", "Webhook delivered");
            response.set("data", null);

            WebhookData data = payOS.webhooks().verify(webhookBody);
            
            String status = "00".equals(data.getCode()) ? "PAID" : "CANCELLED";
            paymentHistoryService.updatePaymentStatus(data.getOrderCode(), status);
            
            System.out.println(data);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            response.set("data", null);
            return response;
        }
    }

    @PostMapping("/create-payment-link")
    public ResponseEntity<?> createPaymentLink(@RequestBody CreatePaymentRequest requestBody) {
        try {
            final String productName = requestBody.getPaidPackageName();
            final String description = requestBody.getPaidPackageDescription();
            final String returnUrl = requestBody.getReturnUrl();

            final String cancelUrl = requestBody.getCancelUrl();
            final long price = requestBody.getPrice().longValue();
            String currentTimeString = String.valueOf(new Date().getTime());
            long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

            PaymentLinkItem item = PaymentLinkItem.builder().name(productName).quantity(1).price(price).build();

            CreatePaymentLinkRequest paymentData =
                    CreatePaymentLinkRequest.builder()
                            .orderCode(orderCode)
                            .description(description)
                            .amount(price)
                            .item(item)
                            .returnUrl(returnUrl)
                            .cancelUrl(cancelUrl)
                            .build();


            CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);


            paymentHistoryService.createPayment(requestBody, orderCode);

            return ResponseEntity.ok(data.getCheckoutUrl());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
