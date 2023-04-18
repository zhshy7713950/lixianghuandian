package com.ruimeng.things.shop.bean;

import java.io.Serializable;



public class UploadFileBean implements Serializable {


    /**
     * errcode : 200
     * errmsg :
     * data : {"string":"http://cdnimg.qiniu.hr999999.com/201802071531545292.jpg"}
     */

    private int errcode;
    private String errmsg;
    private DataBean data;

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * string : http://cdnimg.qiniu.hr999999.com/201802071531545292.jpg
         */

        private String string;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }
}
