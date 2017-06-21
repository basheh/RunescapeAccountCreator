package com.anti_captcha;

import org.json.JSONObject;

public interface IAnticaptchaTaskProtocol {
    JSONObject getPostData();
    String getTaskSolution();
}
