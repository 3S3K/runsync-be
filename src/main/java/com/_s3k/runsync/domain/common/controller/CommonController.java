package com._s3k.runsync.domain.common.controller;

import com._s3k.runsync.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommonController {

    @GetMapping("/health")
    public CommonResponse<String> healthCheck() {
        return CommonResponse.success("OK");
    }
}
