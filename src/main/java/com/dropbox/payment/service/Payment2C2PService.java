package com.dropbox.payment.service;

import com.dropbox.payment.entity.app.Payment;
import com.dropbox.payment.entity.app.SaTransPay;
import com.dropbox.payment.repository.PaymentRepository;
import com.dropbox.payment.repository.SaTransPayRepository;
import com.dropbox.payment.util.AppUtil;
import com.dropbox.payment.util.PkcsUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
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
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.imageio.ImageIO;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Payment2C2PService {

    @Value("${payment.2c2p.merchant-id}")
    private final String MERCHANT_ID = "764764000003660";

    @Value("${payment.2c2p.currency-code}")
    private final String CURRENCY_CODE = "764";

    @Value("${payment.2c2p.securepay-version}")
    private final String SECUREPAY_VERSION = "9.9";

    private final HMac hmac = new HMac(new SHA256Digest());
    @Value("${payment.2c2p.merchant-secretkey}")
    private final String merchantSecretKey = "55B3A315FE7F50778512624F02EB08461BA769BCA95C9A4BB41EAE71F72FD2F6";
    @Value("${payment.2c2p.merchant-123-secretkey}")
    private final String merchant123SecretKey = "XU7D42QYU08ZLH4MVJRMNJE27ZJO18QE";

    @Value("${payment.2c2p.securepay-url}")
    private final String SECUREPAY_URL = "https://demo2.2c2p.com/2C2PFrontEnd/SecurePayment/Payment.aspx";

    @Value("${payment.thaiqrlogo.path}")
    private final String THAI_QR_LOGO_PATH = "classpath:thaiqrpayments.png";

    private final String PROMPTPAY_QR_TYPE = "PP";

    private final int SERVICE_ID_SECUREPAY = 0;

    final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
    final DecimalFormat amountFormatter = new DecimalFormat("000000000000");

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    SaTransPayRepository saTransPayRepository;

    public String twoC2PStartPayRequest(String jsonInput){
        log.debug("twoC2PStartPayRequest()");
        log.debug("json input:: {}", jsonInput);

        String encrypted = null;
        String payType = "";
        Double payAmount = null;
        JsonNode payloadObj = null;

        try {
            //Parse input for validate JSON structure
            ObjectMapper mapper = new ObjectMapper();
            payloadObj = mapper.readTree(jsonInput);
            log.debug(" parsed payload {}", payloadObj);

            if (payloadObj.has("pay_type") && AppUtil.isNotNull(payloadObj.get("pay_type"))){
                payType = payloadObj.get("pay_type").asText();
            }

            String isAlternatePayment = payloadObj.has("alternate_payment") ? payloadObj.get("alternate_payment").asText() : "false";
            String paymentChannel = payloadObj.has("payment_channel") ? payloadObj.get("payment_channel").asText() : "";
            String channelCode = payloadObj.has("channel_code") ? payloadObj.get("channel_code").asText() : "";
            String mobileNo = payloadObj.has("mobile_no") ? payloadObj.get("mobile_no").asText() : "";
            String cardholderEmail = payloadObj.has("cardholder_email") ? payloadObj.get("cardholder_email").asText() : "";
            String agentCode = payloadObj.has("agent_code") ? payloadObj.get("agent_code").asText() : "";
            String encryptedCardInfo = payloadObj.has("encryptedCardInfo") ? payloadObj.get("encryptedCardInfo").asText() : "";

            String qrType = payloadObj.has("qr_type") ? payloadObj.get("qr_type").asText() : "";
            String desc = payloadObj.has("description") ? payloadObj.get("description").asText() : "";
            String amt = payloadObj.has("amount") ? payloadObj.get("amount").asText() : "";
            if ("".equalsIgnoreCase(amt)) {
                throw new IllegalArgumentException("Amount is empty");
            }
            Long serviceLife = payloadObj.has("service_lift") ? payloadObj.get("service_lift").asLong() : 5l;

            String amount = amountFormatter.format(Float.valueOf(amt) * 100);
            ZoneId zoneId = ZoneId.of("Asia/Bangkok");
            String paymentExpiry = ZonedDateTime.now(zoneId).plusMinutes(serviceLife).format(dtFormatter);
//            String paymentExpiry = ZonedDateTime.now(zoneId).plusMinutes(5).format(dtFormatter);
            String cardholderName = "";

            long uniqueTransactionCode = System.currentTimeMillis();
            // String paidAgent = "BBL";//paid_agent BAY,BBL,KTB,SCB, KBANK
            // Used when want to verify payload after sever response.
            String PAN_COUNTRY = "TH";

            final String signatureString = new StringBuilder(SECUREPAY_VERSION).append(MERCHANT_ID)
                    .append(uniqueTransactionCode).append(desc).append(amount).append(CURRENCY_CODE).append(PAN_COUNTRY)
                    .append(cardholderName).append(encryptedCardInfo).toString();

            StringBuilder xml = new StringBuilder("<PaymentRequest>");
            xml.append("<merchantID>");
            xml.append(MERCHANT_ID);
            xml.append("</merchantID>");
            xml.append("<uniqueTransactionCode>");
            xml.append(uniqueTransactionCode);
            xml.append("</uniqueTransactionCode>");
            xml.append("<desc> ");
            xml.append(desc);
            xml.append("</desc>");
            xml.append("<amt>");
            xml.append(amount);
            xml.append("</amt>");
            xml.append("<currencyCode>");
            xml.append(CURRENCY_CODE);
            xml.append("</currencyCode>");
            xml.append("<panCountry>");
            xml.append(PAN_COUNTRY);
            xml.append("</panCountry>");
            xml.append("<cardholderName>");
            xml.append(cardholderName);
            xml.append("</cardholderName>");
            xml.append("<cardholderEmail>");
            xml.append(cardholderEmail);
            xml.append("</cardholderEmail>");
            if ("true".equalsIgnoreCase(isAlternatePayment)) {
                log.info("...APM...");
                // Alternative payment
                xml.append("<paymentChannel>").append(paymentChannel).append("</paymentChannel>");
                xml.append("<agentCode>").append(agentCode).append("</agentCode>");
                xml.append("<channelCode>").append(channelCode).append("</channelCode>");
                xml.append("<paymentExpiry>").append(paymentExpiry).append("</paymentExpiry>");
                xml.append("<mobileNo>").append(mobileNo).append("</mobileNo>");
                xml.append("<qrOption>").append("1").append("</qrOption>");
                xml.append("<qrType>").append(qrType).append("</qrType>");
            }
            xml.append("</PaymentRequest>");

            log.debug("xml payload:: {}", xml);

            String paymentPayload = Base64.toBase64String(xml.toString().getBytes(StandardCharsets.UTF_8.toString()));
            String signature = encodeHMAC(paymentPayload, SERVICE_ID_SECUREPAY).toUpperCase();
            String payloadXML = new StringBuilder("<PaymentRequest>")
                    .append("<version>").append(SECUREPAY_VERSION).append("</version>")
                    .append("<payload>").append(paymentPayload).append("</payload>")
                    .append("<signature>").append(signature).append("</signature>")
                    .append("</PaymentRequest>").toString();

            String payload = Base64.toBase64String(payloadXML.getBytes(StandardCharsets.UTF_8.toString()));

            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder().add("paymentRequest", payload).build();
            // Alternate payment APM
            Request request = new Request.Builder().url(SECUREPAY_URL).post(formBody).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseString = response.body().string();
                log.debug("<- response 1:: {}", responseString);
                String result = "";
                Map<String, String> extractedResult = readRequestPayResponse(responseString);
                if (!extractedResult.isEmpty()) {
                    String qrImage = "";
                    if (extractedResult.containsKey("qrData")) {
                        if (PROMPTPAY_QR_TYPE.equalsIgnoreCase(qrType)) {
                            qrImage = imageToBase64(createQRImage(extractedResult.get("qrData"), THAI_QR_LOGO_PATH));
                        } else {
                            qrImage = imageToBase64(createQRImage(extractedResult.get("qrData"), ""));
                        }
                    }
                    extractedResult.put("qrBase64", qrImage);
                    Gson gson = new Gson();
                    result = gson.toJson(extractedResult);

                    String paymentCode = extractedResult.get("tranRef").toString();
                    Payment payment = paymentRepository.findByCode(payType);
                    SaTransPay saTransPay = new SaTransPay();
                    saTransPay.setPayment(payment);
                    saTransPay.setPaymentAmt(payAmount);
                    saTransPay.setPaymentRefCode(paymentCode);
                    saTransPay.setPaymentStatus("PE");
                    saTransPayRepository.save(saTransPay);
                }

                return result;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return "";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
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

    private Map<String, String> readRequestPayResponse(String resultString) {
        log.info(" readRequestPayResponse() ");
        Map<String, String> result = new HashMap<>();
        try {

            if ("".equalsIgnoreCase(resultString)) {
                throw new IOException("no response string");
            }
            String resPayloadXml = new String(Base64.decode(resultString), StandardCharsets.UTF_8.toString());
            log.debug("Lv1 response payload: {}", resPayloadXml);

            String PAYMENT_XMLHANDLER = "paymentRequest";
            Map<String, String> xmlReadResult = readResponse(resPayloadXml, PAYMENT_XMLHANDLER);

            if (xmlReadResult.containsKey("payload")) {
                log.info("Lv2 response payload: {}", xmlReadResult.get("payload"));

                String PAYLOAD_XMLHANDLER = "payloadResponse";
                Map<String, String> responsePayload = readResponse(xmlReadResult.get("payload"), PAYLOAD_XMLHANDLER);

                verifySecurePaySignature(xmlReadResult.get("signature"), xmlReadResult.get("payload"));

                return responsePayload;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, String> readRequestPayResponse2(String resultString) {
        log.info(" readRequestPayResponse() ");
        Map<String, String> result = new HashMap<>();
        try {

            if ("".equalsIgnoreCase(resultString)) {
                throw new IOException("no response string");
            }
            String resPayloadXml = new String(Base64.decode(resultString), StandardCharsets.UTF_8.toString());
            log.debug("Lv1 response payload: {}", resPayloadXml);

            String PAYMENT_XMLHANDLER = "paymentRequest";
            Map<String, String> xmlReadResult = readResponse(resPayloadXml, PAYMENT_XMLHANDLER);

            if (xmlReadResult.containsKey("payload")) {
                log.info("Lv2 response payload: {}", xmlReadResult.get("payload"));

                String PAYLOAD_XMLHANDLER = "payloadResponse";
                Map<String, String> responsePayload = readResponse(xmlReadResult.get("payload"), PAYLOAD_XMLHANDLER);

                //verifySecurePaySignature(xmlReadResult.get("signature"), xmlReadResult.get("payload"));

                return responsePayload;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, String> readResponse(String xmlString, String responseType) {
        log.debug(" readResponse() ");
        Map<String, String> result = new HashMap<String, String>();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        try {

            DefaultHandler responseHandler = new DefaultHandler() {

                boolean isPayload = false;
                boolean isSignature = false;

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    log.debug("start element: {}", qName);

                    if ("payload".equals(qName)) {
                        isPayload = true;
                    }
                    if ("signature".equals(qName)) {
                        isSignature = true;
                    }

                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    if (isPayload) {
                        String payloadBasse64 = new String(ch, start, length);
                        result.put("payload", payloadBasse64);
                        isPayload = false;
                    }
                    if (isSignature) {
                        String signature = new String(ch, start, length);
                        result.put("signature", signature);
                        isSignature = false;
                    }
                }
            };

            DefaultHandler payloadHandler = new DefaultHandler() {

                boolean isQRData = false;
                boolean isRespCode = false;
                boolean isStatus = false;
                boolean isFailReason = false;
                boolean isPaymentRes = false;
                boolean isMerchant = false;
                boolean isUniqTrans = false;
                boolean isTransRef = false;
                boolean isDateTime = false;
                boolean isProcessBy = false;
                boolean isPaymentScheme = false;

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    if (isRespCode) {
                        result.put("respCode", new String(ch, start, length));
                        isRespCode = false;
                    }
                    if (isFailReason) {
                        result.put("failReason", new String(ch, start, length));
                        isFailReason = false;
                    }
                    if (isQRData) {
                        // log.info("qr: {} {}",ch, length);
                        result.put("qrData", new String(ch, start, length));
                        isQRData = false;
                    }
                    if (isStatus) {
                        result.put("status", new String(ch, start, length));
                        isStatus = false;
                    }
                    if (isUniqTrans) {
                        result.put("uniqueTransactionCode", new String(ch, start, length));
                        isUniqTrans = false;
                    }
                    if (isTransRef) {
                        result.put("tranRef", new String(ch, start, length));
                        isTransRef = false;
                    }
                    if (isDateTime) {
                        result.put("dateTime", new String(ch, start, length));
                        isDateTime = false;
                    }
                    if (isProcessBy) {
                        result.put("processBy", new String(ch, start, length));
                        isProcessBy = false;
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {

                    if ("PaymentResponse".equals(qName)) {
                        isPaymentRes = true;
                    }
                    if ("merchantID".equals(qName)) {
                        isMerchant = true;
                    }
                    if ("respCode".equals(qName)) {
                        isRespCode = true;
                    }
                    if ("uniqueTransactionCode".equals(qName)) {
                        isUniqTrans = true;
                    }
                    if ("tranRef".equals(qName)) {
                        isTransRef = true;
                    }
                    if ("dateTime".equals(qName)) {
                        isDateTime = true;
                    }
                    if ("status".equals(qName)) {
                        isStatus = true;
                    }
                    if ("failReason".equals(qName)) {
                        isFailReason = true;
                    }
                    if ("processBy".equals(qName)) {
                        isProcessBy = true;
                    }
                    if ("paymentScheme".equals(qName)) {
                        isPaymentScheme = true;
                    }
                    if ("qrData".equals(qName)) {
                        isQRData = true;
                    }
                }
            };

            if ("paymentRequest".equalsIgnoreCase(responseType)) {
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(new InputSource(new StringReader(xmlString)), responseHandler);
            }
            if ("payloadResponse".equalsIgnoreCase(responseType)) {
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(new InputSource(new StringReader(xmlString)), payloadHandler);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (result.containsKey("payload")) {
            // log.info("read base64 {}", result.get("payload"));
            try {
                String payload = new String(Base64.decode(result.get("payload")), StandardCharsets.UTF_8.toString());
                result.put("payload", payload);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

    private void verifySecurePaySignature(String returnSignature, String payloadResponse) throws IllegalStateException, UnsupportedEncodingException {
        log.info(" verifySecurePaySignature() ");
        String payloadBase64 = Base64.toBase64String(payloadResponse.getBytes(StandardCharsets.UTF_8.toString()));
        String signatureHash = encodeHMAC(payloadBase64, SERVICE_ID_SECUREPAY);
        log.debug("signature xml: {}", returnSignature);
        log.debug("signature hashed: {}", signatureHash.toUpperCase());
        if (!signatureHash.toUpperCase().equals(returnSignature)) {
            throw new IllegalStateException("Signature not match!");
        }
        log.info(" -- OK");
    }

    private String imageToBase64(byte[] imageData) {
        return Base64.toBase64String(imageData);
    }

    private byte[] createQRImage(String qrRawData, String centerLogoPath) {
        try {
            //TODO QR image dimension.
            HashMap<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            BitMatrix matrix = new MultiFormatWriter().encode(qrRawData, BarcodeFormat.QR_CODE, 1024, 1024, hintMap);
            // Load QR image
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix, getMatrixConfig());

            BufferedImage combined;

            // Load logo image
            BufferedImage overlay = null;
            if (null != centerLogoPath && !"".equalsIgnoreCase(centerLogoPath)) {
                File overlayFile = ResourceUtils.getFile(centerLogoPath);
                log.info(" overlay: {}",overlayFile);
                overlay = ImageIO.read(overlayFile);

                // Calculate the delta height and width between QR code and logo
                int deltaHeight = qrImage.getHeight() - overlay.getHeight();
                int deltaWidth = qrImage.getWidth() - overlay.getWidth();

                // Initialize combined image
                combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) combined.getGraphics();

                // Write QR code to new image at position 0/0
                g.drawImage(qrImage, 0, 0, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

                // Write logo into combine image at position (deltaWidth / 2) and
                // (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
                // the same space for the logo to be centered
                g.drawImage(overlay, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);
            } else {
                combined = qrImage;
            }

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
//            MatrixToImageWriter.writeToStream(matrix, "PNG", pngOutputStream);
            ImageIO.write(combined, "png", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String twoC2PCallback(MultiValueMap<String, String> responseData) throws IOException {
        log.info("res time: {}", new Date());

        String resString = responseData.containsKey("paymentResponse")
                ? responseData.get("paymentResponse").get(0) : "";
        log.info("res: {}", resString);

        if ("".equalsIgnoreCase(resString)) {
            throw new IOException("no response string");
        }

        String result = "";
        Map<String, String> extractedResult = readRequestPayResponse2(resString);
        if (!extractedResult.isEmpty()) {
            Gson gson = new Gson();
            result = gson.toJson(extractedResult);

            String paymentCode = extractedResult.get("tranRef").toString();
            String transactionStatus = extractedResult.get("status").toString();

            SaTransPay saTransPay = saTransPayRepository.findByPaymentRefCode(paymentCode);
            if (AppUtil.isNotNull(saTransPay)){
                saTransPay.setPaymentStatus(transactionStatus);
                saTransPayRepository.save(saTransPay);
            }
        }

        log.info("result : {}", result);

        return "OK";
    }

    public String twoC2PGetInternalStatus(String jsonInput){
        log.debug("twoC2PGetInternalStatus()");
        String result = "";
        try{
            JSONObject jsonObject = new JSONObject(jsonInput);
            if (!jsonObject.isNull("payment_code")){
                String paymentCode = jsonObject.getString("payment_code");
                SaTransPay saTransPay = saTransPayRepository.findByPaymentRefCode(paymentCode);
                if (AppUtil.isNotNull(saTransPay)){
                    if (saTransPay.getPaymentStatus().equals("A")){
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

    private MatrixToImageConfig getMatrixConfig() {
        // ARGB Colors
        // Check Colors ENUM
        return new MatrixToImageConfig(Colors.BLACK.getArgb(), Colors.WHITE.getArgb());
    }

    public enum Colors {

        BLUE(0xFF40BAD0),
        RED(0xFFE91C43),
        PURPLE(0xFF8A4F9E),
        ORANGE(0xFFF4B13D),
        WHITE(0xFFFFFFFF),
        BLACK(0xFF000000);

        private final int argb;

        Colors(final int argb) {
            this.argb = argb;
        }

        public int getArgb() {
            return argb;
        }
    }

    //Dev
    public String twoC2PTestPaid(String jsonInput){
        log.debug("twoC2PTestPaid()");
        try{
            JSONObject jsonObject = new JSONObject(jsonInput);
            if (!jsonObject.isNull("payment_code")){
                String paymentCode = jsonObject.getString("payment_code");
                SaTransPay saTransPay = saTransPayRepository.findByPaymentRefCode(paymentCode);
                if (AppUtil.isNotNull(saTransPay)){
                    saTransPay.setPaymentStatus("A");
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
