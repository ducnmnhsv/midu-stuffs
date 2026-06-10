package com.techx.tradex.ekycadmin.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EContractField {
    private String id;
    private String name;
    private String type;
    private String value;
    private String owner;
    private String dataType;
    private Boolean required;

    @Override
    public String toString() {
        return "EContractField{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", owner='" + owner + '\'' +
                ", dataType='" + dataType + '\'' +
                ", required=" + required +
                '}';
    }
}
