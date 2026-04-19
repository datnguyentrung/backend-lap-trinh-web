package com.dat.backend_v2_2.dto.Security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginReq {

    @Data
    public static class UserBase {
        @NotBlank(message = "Phone number must not be blank")
        private String phoneNumber;

        @NotBlank(message = "Password must not be blank")
        private String password; // raw password (sẽ mã hóa trong service)

        // Bỏ @NotBlank ở đây để tránh lỗi 400 khi frontend chưa kịp gửi idDevice
        private String idDevice;

        private String fcmToken;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank(message = "RefreshToken must not be blank")
        private String refreshToken;
    }

    @Data
    public static class UpdateFcmReq {
        @NotBlank(message = "Refresh token không được để trống")
        private String refreshToken;

        @NotBlank(message = "FCM Token không được để trống")
        private String fcmToken;
    }
}