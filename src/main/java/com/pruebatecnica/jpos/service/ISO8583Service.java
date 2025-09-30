package com.pruebatecnica.jpos.service;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ISO8583Service {

    private static final Logger logger = LoggerFactory.getLogger(ISO8583Service.class);

    @Autowired
    private ISOPackager isoPackager;

    public String createAuthorizationRequest(String pan, String amount, String merchantId) {
        try {
            ISOMsg msg = new ISOMsg();
            msg.setPackager(isoPackager);
            
            msg.setMTI("0100");
            
            if (pan != null && !pan.isEmpty()) {
                msg.set(2, pan);
            }
            
            msg.set(3, "000000");
            msg.set(4, String.format("%012d", Long.parseLong(amount)));
            
            String datetime = new SimpleDateFormat("MMddHHmmss").format(new Date());
            msg.set(7, datetime);
            
            String stan = String.format("%06d", (int)(Math.random() * 999999));
            msg.set(11, stan);
            
            String time = new SimpleDateFormat("HHmmss").format(new Date());
            msg.set(12, time);
            
            String date = new SimpleDateFormat("MMdd").format(new Date());
            msg.set(13, date);
            
            msg.set(18, "5999");
            msg.set(22, "021");
            msg.set(25, "00");
            msg.set(41, merchantId != null ? merchantId : "TERM001");
            msg.set(42, "MERCHANT123456");
            msg.set(49, "170");
            
            byte[] packed = msg.pack();
            String result = new String(packed);
            
            logger.info("Mensaje ISO 8583 creado - MTI: {}, STAN: {}, Amount: {}", 
                       msg.getMTI(), stan, amount);
            
            return result;
            
        } catch (ISOException e) {
            logger.error("Error creando mensaje ISO 8583: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    public String createAuthorizationResponse(String originalMessage, String responseCode) {
        try {
            ISOMsg originalMsg = new ISOMsg();
            originalMsg.setPackager(isoPackager);
            originalMsg.unpack(originalMessage.getBytes());
            
            ISOMsg responseMsg = new ISOMsg();
            responseMsg.setPackager(isoPackager);
            
            responseMsg.setMTI("0110");
            
            for (int i = 2; i <= 49; i++) {
                if (originalMsg.hasField(i)) {
                    responseMsg.set(i, originalMsg.getString(i));
                }
            }
            
            responseMsg.set(39, responseCode);
            
            if ("00".equals(responseCode)) {
                String authId = String.format("%06d", (int)(Math.random() * 999999));
                responseMsg.set(38, authId);
            }
            
            byte[] packed = responseMsg.pack();
            String result = new String(packed);
            
            logger.info("Respuesta ISO 8583 creada - MTI: {}, Response Code: {}", 
                       responseMsg.getMTI(), responseCode);
            
            return result;
            
        } catch (ISOException e) {
            logger.error("Error creando respuesta ISO 8583: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    public ISO8583MessageInfo parseMessage(String messageData) {
        try {
            ISOMsg msg = new ISOMsg();
            msg.setPackager(isoPackager);
            msg.unpack(messageData.getBytes());
            
            ISO8583MessageInfo info = new ISO8583MessageInfo();
            info.mti = msg.getMTI();
            info.pan = msg.hasField(2) ? maskPAN(msg.getString(2)) : null;
            info.amount = msg.hasField(4) ? msg.getString(4) : null;
            info.stan = msg.hasField(11) ? msg.getString(11) : null;
            info.responseCode = msg.hasField(39) ? msg.getString(39) : null;
            info.authId = msg.hasField(38) ? msg.getString(38) : null;
            info.merchantId = msg.hasField(41) ? msg.getString(41) : null;
            
            return info;
            
        } catch (ISOException e) {
            logger.error("Error parseando mensaje ISO 8583: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateMessage(String messageData) {
        try {
            ISOMsg msg = new ISOMsg();
            msg.setPackager(isoPackager);
            msg.unpack(messageData.getBytes());
            
            String mti = msg.getMTI();
            if (mti == null || mti.length() != 4) {
                return false;
            }
            
            if (mti.startsWith("01")) {
                return msg.hasField(2) && msg.hasField(4) && msg.hasField(11);
            }
            
            return true;
            
        } catch (ISOException e) {
            logger.error("Error validando mensaje ISO 8583: {}", e.getMessage());
            return false;
        }
    }

    private String maskPAN(String pan) {
        if (pan == null || pan.length() < 6) {
            return pan;
        }
        return pan.substring(0, 6) + "*".repeat(pan.length() - 10) + pan.substring(pan.length() - 4);
    }

    public static class ISO8583MessageInfo {
        public String mti;
        public String pan;
        public String amount;
        public String stan;
        public String responseCode;
        public String authId;
        public String merchantId;
        
        public String getFormattedAmount() {
            if (amount == null) return null;
            try {
                long cents = Long.parseLong(amount);
                double dollars = cents / 100.0;
                return String.format("%.2f", dollars);
            } catch (NumberFormatException e) {
                return amount;
            }
        }
        
        public String getResponseDescription() {
            if (responseCode == null) return null;
            switch (responseCode) {
                case "00": return "Approved";
                case "05": return "Do not honor";
                case "14": return "Invalid card number";
                case "51": return "Insufficient funds";
                case "54": return "Expired card";
                default: return "Unknown response";
            }
        }
    }
}