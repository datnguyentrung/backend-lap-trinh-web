package com.dat.backend_v2_2.util;

import com.dat.backend_v2_2.dto.RestResponse;

public final class FormatRestResponse {

    private FormatRestResponse() {
    }

    public static <T> RestResponse<T> success(String message, T data) {
        RestResponse<T> res = new RestResponse<>();
        res.setStatusCode(200);
        res.setMessage(message);
        res.setData(data);
        return res;
    }
}
