package com.techx.tradex.common.constants;

public enum RightNameEnum {

    DIVIDEND("Cổ tức"), STOCKS("Cổ phiếu"), CASH("Tiền"), RELEASE_MORE("Phát hành thêm");

    private String rightName;

    public String getRightName() {
        return rightName;
    }

    public void setRightName(String rightName) {
        this.rightName = rightName;
    }

    RightNameEnum(String rightName) {
        this.rightName = rightName;
    }

}
