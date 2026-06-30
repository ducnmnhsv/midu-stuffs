package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;

import lombok.Data;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

@Data
public class UserCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private StringFilter fullName;

    private BooleanFilter status;

    private StringFilter roles;

    private StringFilter isSuperAdmin;
    private StringFilter isAdmin;
    
    public UserCriteria() {}

    public UserCriteria(UserCriteria other) {
        this.fullName = other.fullName.toString() == null ? null : other.fullName.copy();
        this.status = other.status.toString() == null ? null : other.status.copy();
        this.roles = other.roles.toString() == null ? null : other.roles.copy();
        this.isSuperAdmin = other.isSuperAdmin == null ? null : other.isSuperAdmin.copy();
        this.isAdmin = other.isAdmin == null ? null : other.isAdmin.copy();
    }

    @Override
    public UserCriteria copy() {
        return new UserCriteria(this);
    }

}
