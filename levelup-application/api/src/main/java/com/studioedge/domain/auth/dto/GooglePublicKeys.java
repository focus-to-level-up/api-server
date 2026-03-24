package com.studioedge.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GooglePublicKeys {
    private List<GooglePublicKey> keys;
}
