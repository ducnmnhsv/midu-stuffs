package com.difisoft.nhsv.admin.domain.response;

import com.difisoft.nhsv.admin.domain.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MtsMarketLeadersResponse {
    public Long marketLeaderId;
    public String username;
    public String fullname;
    public String introduction;
    public Long totalSubscribers;
    public Set<Authority> roles;
}
