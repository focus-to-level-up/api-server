package com.studioedge.domain.auth.dto;

import com.studioedge.focus_to_levelup_server.domain.auth.dto.ApplePublicKey;
import lombok.Data;

import java.util.List;

@Data
public class ApplePublicKeys {
    private List<ApplePublicKey> keys;
}
