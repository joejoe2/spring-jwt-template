package com.joejoe2.demo.data.auth.request;

import lombok.Data;

@Data
public class RefreshRequest {
    //refresh token
    private String token;
}
