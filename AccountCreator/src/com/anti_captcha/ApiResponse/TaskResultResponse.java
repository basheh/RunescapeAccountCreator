package com.anti_captcha.ApiResponse;

import com.anti_captcha.Helper.DebugHelper;
import com.anti_captcha.Helper.JsonHelper;

import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TaskResultResponse {
    private Integer errorId;
    private String errorCode;
    private String errorDescription;
    private StatusType status;
    private Double cost;
    private String ip;

    /**
     * ﻿Task create time in UTC
     */
    private ZonedDateTime createTime;

    /**
     * ﻿Task end time in UTC
     */
    private ZonedDateTime endTime;
    private Integer solveCount;
    private SolutionData solution;

    public TaskResultResponse(JSONObject json) {
        errorId = JsonHelper.extractInt(json, "errorId");

        if (errorId != null) {
            if (errorId.equals(0)) {
                status = parseStatus(JsonHelper.extractStr(json, "status"));

                if (status.equals(StatusType.READY)) {
                    cost = JsonHelper.extractDouble(json, "cost");
                    ip = JsonHelper.extractStr(json, "ip");
                    solveCount = JsonHelper.extractInt(json, "solveCount");
                    createTime = unixTimeStampToDateTime(JsonHelper.extractDouble(json, "createTime"));
                    endTime = unixTimeStampToDateTime(JsonHelper.extractDouble(json, "endTime"));

                    solution = new SolutionData();
                    solution.gRecaptchaResponse = JsonHelper.extractStr(json, "solution", "gRecaptchaResponse", true);
                    solution.gRecaptchaResponseMd5 = JsonHelper.extractStr(json, "solution", "gRecaptchaResponseMd5", true);
                    solution.text = JsonHelper.extractStr(json, "solution", "text", true);
                    solution.url = JsonHelper.extractStr(json, "solution", "url", true);

                    if (solution.gRecaptchaResponse == null && solution.text == null)
                    {
                        DebugHelper.out("Got no 'solution' field from API", DebugHelper.Type.ERROR);
                    }
                }
            } else {
                errorCode = JsonHelper.extractStr(json, "errorCode");
                errorDescription = JsonHelper.extractStr(json, "errorDescription");

                DebugHelper.out(errorDescription, DebugHelper.Type.ERROR);
            }
        } else {
            DebugHelper.out("Unknown error", DebugHelper.Type.ERROR);
        }
    }

    private static ZonedDateTime unixTimeStampToDateTime(Double unixTimeStamp) {
        if (unixTimeStamp == null) {
            return null;
        }

        ZonedDateTime epochStart = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));

        return epochStart.plusSeconds((long) (double) unixTimeStamp);
    }

    public Integer getErrorId() {
        return errorId;
    }

    public String getErrorDescription() {
        return errorDescription == null ? "(no error description)" : errorDescription;
    }

    public StatusType getStatus() {
        return status;
    }

    public Double getCost() {
        return cost;
    }

    public String getIp() {
        return ip;
    }

    public ZonedDateTime getCreateTime() {
        return createTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public Integer getSolveCount() {
        return solveCount;
    }

    public SolutionData getSolution() {
        return solution;
    }

    private StatusType parseStatus(String status) {
        if (status == null || status.length() == 0) {
            return null;
        }

        try {
            return StatusType.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getErrorCode() {
        return errorCode;
    }

    public enum StatusType {
        PROCESSING,
        READY
    }

    public class SolutionData {
        private String gRecaptchaResponse; // Will be available for Recaptcha tasks only!
        private String gRecaptchaResponseMd5; // for Recaptcha with isExtended=true property
        private String text; // Will be available for ImageToText tasks only!
        private String url; // Will be available for ImageToText tasks only!

        public String getGRecaptchaResponseMd5() {
            return gRecaptchaResponseMd5;
        }

        public String getText() {
            return text;
        }

        public String getUrl() {
            return url;
        }

        public String getGRecaptchaResponse() {
            return gRecaptchaResponse;
        }
    }
}
