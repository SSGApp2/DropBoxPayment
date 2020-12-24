package com.dropbox.payment.service;

import com.ccpp.PKCS7;
import com.dropbox.payment.entity.app.*;
import com.dropbox.payment.repository.PaymentRepository;
import com.dropbox.payment.repository.PaymentTempRepository;
import com.dropbox.payment.repository.SaTransPayRepository;
import com.dropbox.payment.repository.custom.ParameterDetailRepositoryCustom;
import com.dropbox.payment.util.AppUtil;
import com.dropbox.payment.util.PkcsUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

@Slf4j
@Service
public class Payment123Service {

    @Value("${payment.2c2p.merchant-id}")
    private String MERCHANT_ID = "764764000003660";

    @Value("${payment.2c2p.one23.preferred-agent}")
    private final String ONE23_PREFERRED_AGENT = "PAYATPOST";

    @Value("${payment.2c2p.one23.preferred-channel}")
    private final String ONE23_PREFERRED_CHANNEL = "OTC";

    @Value("${payment.2c2p.one23.preferred-sub-channel}")
    private final String ONE23_PREFERRED_SUB_CHANNEL = "";

    @Value("${payment.2c2p.one23.redirect-url}")
    private final String ONE23_REDIRECT_URL = "https://fea06cc1ec4b.ngrok.io/offre";
    //Landing page after payment

    @Value("${payment.2c2p.one23.notification-url}")
    private String ONE23_NOTIFICATION_URL = "https://123d6a7505a0.ngrok.io/payment/one23/notification";
    //Payment notification URL

    @Value("${payment.2c2p.one23.offline-payment-url}")
    private String START_OFFLINE_PAYMENT123_URL = "https://th-merchants-proxy-v1-uat-123.2c2p.com/api/merchantenc/start-offline-payment";

    @Value("${payment.2c2p.one23.get-payment-status-url}")
    private final String START_OFFLINE_GET_PAYMENT123_URL = "https://th-merchants-proxy-v1-uat-123.2c2p.com/api/merchantenc/get-payment-status";

    @Value("${payment.2c2p.one23.cancel-payment-url}")
    private String START_OFFLINE_CANCEL_PAYMENT123_URL = "https://th-merchants-proxy-v1-uat-123.2c2p.com/api/merchantenc/cancel-payment";

    @Value("${payment.2c2p.merchant-secretkey}")
    private final String merchantSecretKey = "55B3A315FE7F50778512624F02EB08461BA769BCA95C9A4BB41EAE71F72FD2F6";

    @Value("${payment.2c2p.merchant-123-secretkey}")
    private final String merchant123SecretKey = "XU7D42QYU08ZLH4MVJRMNJE27ZJO18QE";

    final DecimalFormat checkSumNoFormat = new DecimalFormat("###,##0.00");

    private final int SERVICE_ID_123 = 1;

    private final HMac hmac = new HMac(new SHA256Digest());

    final okhttp3.MediaType MEDIA_TYPE_JSON = okhttp3.MediaType.get("application/json; charset=utf-8");

    final PkcsUtil pkcsUtil = PkcsUtil.getInstance();

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    PaymentParameterService paymentParameterService;

    @Autowired
    SaTransPayRepository saTransPayRepository;

    @Autowired
    PaymentTempRepository paymentTempRepository;

    public String one23StartPayRequest(String jsonInput){
        log.debug("json input:: {}", jsonInput);
        String encrypted = null;
        String payType = "";
        Double payAmount = null;
        JsonNode payloadObj = null;

        String notiUrl = paymentParameterService.get123NotificationURL();
        if(notiUrl != null){
            ONE23_NOTIFICATION_URL = notiUrl;
        }

        String url123 = paymentParameterService.get123URL();
        if(url123 != null){
            START_OFFLINE_PAYMENT123_URL = url123+"/api/merchantenc/start-offline-payment";
        }

        log.debug(" 123 url (DB) :: {}", url123);
        log.debug(" 123 Notification URL (DB) :: {}", notiUrl);
        try {
            //Parse input for validate JSON structure
            ObjectMapper mapper = new ObjectMapper();
            payloadObj = mapper.readTree(jsonInput);
            log.debug(" parsed payload {}", payloadObj);

            if (!payloadObj.has("amount")) {
                throw new IllegalArgumentException("Amount is unknown");
            }
            payAmount = payloadObj.get("amount").asDouble();

            if (payloadObj.has("pay_type") && AppUtil.isNotNull(payloadObj.get("pay_type"))){
                payType = payloadObj.get("pay_type").asText();
            }

            JsonNode jn = addDefaultProperties(payloadObj);

            log.info("plain message:: {}", jn.toString());

            encrypted = PKCS7.encrypt(pkcsUtil.getPublicKey(), jn.toString().getBytes());

            log.info("enc message:: {}", encrypted);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (null == encrypted) {
            throw new IllegalStateException("No Keypairs");
        }

        JsonObject payloadJson = new JsonObject();
        payloadJson.addProperty("message", encrypted);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(START_OFFLINE_PAYMENT123_URL)
                .post(okhttp3.RequestBody.create(payloadJson.toString(), MEDIA_TYPE_JSON))
                .build();

        log.info("start offline pay");
        String result = "";
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String body = response.body().string();

            ObjectMapper mapper = new ObjectMapper();
            payloadObj = mapper.readTree(body);
            log.debug(" response content:: {}", payloadObj);

            if (!payloadObj.hasNonNull("message")) {
                throw new RuntimeException("no message responsed");
            }
            result = readEncrypted123(payloadObj.get("message").asText());

            log.debug(" result:: {}", result);
            JSONObject jsonObject = new JSONObject(result);
            log.debug(" result jsonObject:: {}", jsonObject);

            String paymentRefCode = jsonObject.getString("payment_code");
            PaymentTemp paymentTemp = new PaymentTemp();
            paymentTemp.setPaymentAmount(payAmount);
            paymentTemp.setPaymentRefCode(paymentRefCode);
            paymentTemp.setPaymentStatus("Pending");
            paymentTemp.setPaymentType("123");
            paymentTempRepository.save(paymentTemp);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private JsonNode addDefaultProperties(JsonNode payloadObj) throws UnsupportedEncodingException {
        String merchantRefCode = String.valueOf(System.currentTimeMillis());
        String merchantID = paymentParameterService.getMerchantID(payloadObj.get("dropbox_type").asText());
        String checkSumString = createCheckSumFor123(merchantRefCode, payloadObj.get("amount").asText(),merchantID);

        //Ignore the input if existed

        ((ObjectNode) payloadObj).put("merchant_id", merchantID);
        ((ObjectNode) payloadObj).put("merchant_reference", merchantRefCode);
        ((ObjectNode) payloadObj).put("preferred_agent", ONE23_PREFERRED_AGENT);
        ((ObjectNode) payloadObj).put("preferred_channel", ONE23_PREFERRED_CHANNEL);
        ((ObjectNode) payloadObj).put("preferred_sub_channel", ONE23_PREFERRED_SUB_CHANNEL);
        ((ObjectNode) payloadObj).put("notify_buyer", true);
        ((ObjectNode) payloadObj).put("include_instructions_url", true);
        ((ObjectNode) payloadObj).put("redirect_url", ONE23_REDIRECT_URL);
        ((ObjectNode) payloadObj).put("notification_url", ONE23_NOTIFICATION_URL);
        ((ObjectNode) payloadObj).put("checksum", checkSumString);
        log.debug("addDefaultProperties (payloadObj):: {}", payloadObj);
        return payloadObj;
    }

    private String createCheckSumFor123(String merchantRefCode, String amountText,String merchantID) throws UnsupportedEncodingException {
        if (null == amountText || "".equalsIgnoreCase(amountText)) {
            throw new IllegalArgumentException("amount must not empty");
        }
        if (null == merchantRefCode || "".equalsIgnoreCase(merchantRefCode)) {
            throw new IllegalArgumentException("Merchant RefCode must not empty");
        }
        BigDecimal amount = new BigDecimal(amountText);
        StringBuffer str = new StringBuffer(merchantID);
        String CURRENCY_CODE_123 = "THB";
        str.append(merchantRefCode).append(checkSumNoFormat.format(amount)).append(CURRENCY_CODE_123);
        log.debug("checkSumFor123 (plain):: {}", str.toString());
        return encodeHMAC(str.toString(), SERVICE_ID_123);
    }

    private String encodeHMAC(String message, int serviceId) throws UnsupportedEncodingException {
        log.debug(" encodeHMAC() ");
        log.debug(" message: {}", message);

        byte[] resultBuffer = new byte[hmac.getMacSize()];
        byte[] plainByte = message.getBytes(StandardCharsets.UTF_8.toString());

        if (0 == serviceId) {
            hmac.init(new KeyParameter(merchantSecretKey.getBytes(StandardCharsets.UTF_8.toString())));
        } else if (1 == serviceId) {
            hmac.init(new KeyParameter(merchant123SecretKey.getBytes(StandardCharsets.UTF_8.toString())));
        }
        hmac.update(plainByte, 0, plainByte.length);
        hmac.doFinal(resultBuffer, 0);

        return new String(Hex.encode(resultBuffer));
    }

    @Value("${payment.2c2p.one23.private-key-pass}")
    private String PRIVATEKEY_PASSPHRASE;
    @Value("${payment.2c2p.one23.bks-pass}")
    private String BKS_PASSPHRASE;

    private String readEncrypted123(String encodedMessage) throws Exception {
        log.info("readEncrypted123()...");
        return PKCS7.decrypt(pkcsUtil.getPrivateKey(), PRIVATEKEY_PASSPHRASE, Base64.decode(encodedMessage.getBytes()), BKS_PASSPHRASE);
    }

    public String one23GetStatus(String jsonInput){
        log.info(" -> offlineGetStatus() ->");
        log.debug("json input:: {}", jsonInput);
        String encrypted = null;
        JsonNode payloadObj = null;
        //Test
        try {
            //Parse input for validate JSON structure
            ObjectMapper mapper = new ObjectMapper();
            payloadObj = mapper.readTree(jsonInput);
            log.debug(" parsed payload {}", payloadObj);

            String paymentCode = payloadObj.get("payment_code").asText();

            StringBuffer str = new StringBuffer(MERCHANT_ID);
            str.append(paymentCode);
            log.debug("checkSumFor123 (plain):: {}", str.toString());
            String checkSumString = encodeHMAC(str.toString(), SERVICE_ID_123);

            //Ignore the input if existed
            ((ObjectNode) payloadObj).put("merchant_id", MERCHANT_ID);
            ((ObjectNode) payloadObj).put("payment_code", paymentCode);
            ((ObjectNode) payloadObj).put("merchant_reference", "");
            ((ObjectNode) payloadObj).put("checksum", checkSumString);

            log.info("plain message:: {}", payloadObj.toString());

            encrypted = PKCS7.encrypt(pkcsUtil.getPublicKey(), payloadObj.toString().getBytes());

            log.info("enc message:: {}", encrypted);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (null == encrypted) {
            throw new IllegalStateException("No Keypairs");
        }

        JsonObject payloadJson = new JsonObject();
        payloadJson.addProperty("message", encrypted);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(START_OFFLINE_GET_PAYMENT123_URL)
                .post(okhttp3.RequestBody.create(payloadJson.toString(), MEDIA_TYPE_JSON))
                .build();

        String result = "";
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String body = response.body().string();

            ObjectMapper mapper = new ObjectMapper();
            payloadObj = mapper.readTree(body);
            log.debug(" response content:: {}", payloadObj);

            if (!payloadObj.hasNonNull("message")) {
                throw new RuntimeException("no message responsed");
            }
            result = readEncrypted123(payloadObj.get("message").asText());

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public String one23CancelPayment(String jsonInput){
        log.info(" -> offlineGetStatus() ->");
        log.debug("json input:: {}", jsonInput);
        String encrypted = null;
        JsonNode payloadObj = null;
        String paymentCode = "";

        String url123 = paymentParameterService.get123URL();
        if(url123 != null){
            START_OFFLINE_CANCEL_PAYMENT123_URL = url123+"/api/merchantenc/cancel-payment";
        }

        log.debug(" 123 URL (DB) :: {}", url123);
        //Test
        try {
            //Parse input for validate JSON structure
            ObjectMapper mapper = new ObjectMapper();
            payloadObj = mapper.readTree(jsonInput);
            log.debug(" parsed payload {}", payloadObj);

            paymentCode = payloadObj.get("payment_code").asText();

            String merchantID = payloadObj.get("dropbox_type").asText();
            StringBuffer str = new StringBuffer(merchantID);
            str.append(paymentCode);
            log.debug("checkSumFor123 (plain):: {}", str.toString());
            String checkSumString = encodeHMAC(str.toString(), SERVICE_ID_123);

            //Ignore the input if existed
            ((ObjectNode) payloadObj).put("merchant_id", merchantID);
            ((ObjectNode) payloadObj).put("payment_code", paymentCode);
            ((ObjectNode) payloadObj).put("merchant_reference", "");
            ((ObjectNode) payloadObj).put("checksum", checkSumString);

            log.info("plain message:: {}", payloadObj.toString());

            encrypted = PKCS7.encrypt(pkcsUtil.getPublicKey(), payloadObj.toString().getBytes());

            log.info("enc message:: {}", encrypted);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (null == encrypted) {
            throw new IllegalStateException("No Keypairs");
        }

        JsonObject payloadJson = new JsonObject();
        payloadJson.addProperty("message", encrypted);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(START_OFFLINE_CANCEL_PAYMENT123_URL)
                .post(okhttp3.RequestBody.create(payloadJson.toString(), MEDIA_TYPE_JSON))
                .build();


        String result = "";
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String body = response.body().string();

            ObjectMapper mapper = new ObjectMapper();
            payloadObj = mapper.readTree(body);
            log.debug(" response content:: {}", payloadObj);

            if (!payloadObj.hasNonNull("message")) {
                throw new RuntimeException("no message responsed");
            }
            result = readEncrypted123(payloadObj.get("message").asText());

            PaymentTemp paymentTemp = paymentTempRepository.findByPaymentRefCode(paymentCode);
            if (AppUtil.isNotNull(paymentTemp)){
                paymentTemp.setPaymentStatus("Cancelled");
                paymentTempRepository.save(paymentTemp);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public String one23Notification(String jsonInput) throws UnsupportedEncodingException {
        String responseCode = "00";
        String paymentCode = "";
        String merchantId = "";

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jn = mapper.readTree(jsonInput);
            String responseJson = jn.get("message").asText();
            log.debug("jn message :: {}",responseJson);
            String data = readEncrypted123(responseJson);

            //TODO Test; occurred when we got notification json. mocked data after decrypted
//            String data = jsonInput;
//            log.debug(" notification-in:: {}", data);
            // end mocked data

            mapper = new ObjectMapper();
            jn = mapper.readTree(data);

            if (jn.has("transaction_status")) {
                responseCode = "00";
            }
            //TODO use information below for the system database
            String transactionStatus = jn.get("transaction_status").asText();
            String channelCode = jn.get("channel_code").asText();
            String agentCode = jn.get("agent_code").asText();
            String completedDateTime = jn.get("completed_date_time").asText();
            String amount = jn.get("amount").asText();
            String paidAmount = jn.get("paid_amount").asText();
            merchantId = jn.get("merchant_id").asText();
            String merchantReference = jn.get("merchant_reference").asText();
            paymentCode = jn.get("payment_code").asText();

            if(AppUtil.isNotEmpty(transactionStatus)){
                String paymentStatus = "";
                switch (transactionStatus){
                    case "PE" : paymentStatus = "Pending"; break;
                    case "NA" : paymentStatus = "No action (web payments)"; break;
                    case "PA" : paymentStatus = "Paid"; break;
                    case "PM" : paymentStatus = "Paid more"; break;
                    case "PL" : paymentStatus = "Paid less"; break;
                    case "EX" : paymentStatus = "Expired"; break;
                    case "CA" : paymentStatus = "Cancelled"; break;
                    case "FA" : paymentStatus = "Failed"; break;
                }

                PaymentTemp paymentTemp = paymentTempRepository.findByPaymentRefCode(paymentCode);
                if(paymentTemp != null){
                    paymentTemp.setPaymentStatus(paymentStatus);
                    paymentTempRepository.save(paymentTemp);
                }
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            responseCode = "01";
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            responseCode = "01";
        }

        JsonObject jo = new JsonObject();
        jo.addProperty("response_code", responseCode);
        //checksum = merchantId + paymentCode + responseCode
        jo.addProperty("checksum", createCheckSumForAcknowledge(paymentCode, responseCode,merchantId));
        log.debug(" -> noti ack message: {}", jo.toString());
        return jo.toString();
    }

    private String createCheckSumForAcknowledge(String paymentCode, String responseCode,String merchantId) throws UnsupportedEncodingException {
        log.debug("checkSumForAcknowledge()");
        if (null == paymentCode || "".equalsIgnoreCase(paymentCode)) {
            throw new IllegalArgumentException("Payment Code is empty");
        }
        if (null == responseCode || "".equalsIgnoreCase(responseCode)) {
            throw new IllegalArgumentException("Response Code is empty");
        }
        StringBuffer str = new StringBuffer(merchantId);
        str.append(paymentCode).append(responseCode);
        log.debug(" ACK plain msg:: {}", str.toString());
        return encodeHMAC(str.toString(), SERVICE_ID_123);
    }

    public String one23GetInternalStatus(String jsonInput){
        log.debug("one23GetInternalStatus()");
        String result = "";
        try{
            JSONObject jsonObject = new JSONObject(jsonInput);
            if (!jsonObject.isNull("payment_code")){
                String paymentCode = jsonObject.getString("payment_code");
                PaymentTemp paymentTemp = paymentTempRepository.findByPaymentRefCode(paymentCode);
                if (AppUtil.isNotNull(paymentTemp)){
                    if (paymentTemp.getPaymentStatus().equals("Paid")){
                        result = "Paid";
                    } else {
                        result = "Not";
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    //Dev
    public String one23TestPaid(String jsonInput){
        log.debug("one23TestPaid()");
        try{
            JSONObject jsonObject = new JSONObject(jsonInput);
            if (!jsonObject.isNull("payment_code")){
                String paymentCode = jsonObject.getString("payment_code");
                SaTransPay saTransPay = saTransPayRepository.findByPaymentRefCode(paymentCode);
                if (AppUtil.isNotNull(saTransPay)){
                    saTransPay.setPaymentStatus("PA");
                    saTransPayRepository.save(saTransPay);
                }
            }
            return "Success";
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return "Fail";
        }

    }
}
