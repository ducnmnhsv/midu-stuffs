package com.techx.tradex.ekycadmin.models.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FptECEnvelopesRecipientRequest {

    @NotBlank
    private String contactId;

    private String envelopeId;
}
