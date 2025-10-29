package com.studioedge.focus_to_levelup_server.global.jwt;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserAuthentication extends UsernamePasswordAuthenticationToken {

    public UserAuthentication(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public Long getMemberId() {
        if (super.getPrincipal() instanceof Long) {
            return (Long) super.getPrincipal();
        }
        if (super.getPrincipal() instanceof Member) {
            return ((Member) super.getPrincipal()).getMemberId();
        }
        return null;
    }

    public Member getMember() {
        if (super.getPrincipal() instanceof Member) {
            return (Member) super.getPrincipal();
        }
        return null;
    }
}
