package com.braidsbeautyByAngie.aggregates.constants;

import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.aws.IBucketUtil;
import com.braidsbeautybyangie.sagapatternspringboot.aggregates.aggregates.util.BucketParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.Timestamp;

@Slf4j
public class Constants {

    public static final Boolean STATUS_ACTIVE=true;
    public static final Boolean STATUS_INACTIVE=false;
    public static final String NUM_PAG_BY_DEFECT="0";
    public static final String SIZE_PAG_BY_DEFECT="10";
    public static final String ORDER_BY_DEFECT_ALL="createdAt";
////    public static final String ORDER_BY_DEFECT_PAGADOR_FLETE="creadoEn";
////    public static final String ORDER_BY_DEFECT_REMITENTE="creadoEn";
////    public static final String ORDER_BY_DEFECT_GUIATRANSPORTISTA="fechaEmision";
    public static final String ORDER_DIRECT_BY_DEFECT="0";

    public static Timestamp getTimestamp(){
        long currentTime = System.currentTimeMillis();
        return new Timestamp(currentTime);
    }
    public static StringBuilder parametersForLogger(int pageNumber, int pageSize, String orderBy, String sortDir){
        StringBuilder parameters = new StringBuilder();
        parameters.append("pageNumber=").append(pageNumber);
        parameters.append("&pageSize=").append(pageSize);
        parameters.append("&orderBy=").append(orderBy);
        parameters.append("&sortDir=").append(sortDir);
        return parameters;
    }

    public static String getUserInSession() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String username = request.getHeader("X-Username");
        String userId = request.getHeader("X-User-Id");
        return username + " - " + userId;
    }
    public static void deleteOldImageFromS3(String imageUrl, IBucketUtil bucketUtil, String bucketName) {
        try {
            String filePath = extractFilePathFromUrl(imageUrl, bucketName);
            if (filePath != null) {
                BucketParams bucketParams = BucketParams.builder()
                        .bucketName(bucketName)
                        .filePath(filePath)
                        .build();
                bucketUtil.deleteFile(bucketParams);
                log.info("File deleted from S3: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Error deleting file from S3: {}", imageUrl, e);
        }
    }
    public static String extractFilePathFromUrl(String imageUrl, String bucketName) {
        try {
            if (imageUrl.contains(bucketName + ".s3.amazonaws.com/")) {
                return imageUrl.substring(imageUrl.indexOf(bucketName + ".s3.amazonaws.com/") + (bucketName + ".s3.amazonaws.com/").length());
            } else if (imageUrl.contains("s3.amazonaws.com/" + bucketName + "/")) {
                return imageUrl.substring(imageUrl.indexOf("s3.amazonaws.com/" + bucketName + "/") + ("s3.amazonaws.com/" + bucketName + "/").length());
            }
            return null;
        } catch (Exception e) {
            log.error("Error extracting file path from URL: {}", imageUrl, e);
            return null;
        }
    }
}
