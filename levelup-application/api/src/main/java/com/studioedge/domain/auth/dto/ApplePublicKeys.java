package com.studioedge.domain.auth.dto;

import com.studioedge.domain.auth.dto.ApplePublicKey;
import lombok.Data;

import java.util.List;

@Data
public class ApplePublicKeys {
    private List<ApplePublicKey> keys;
}
