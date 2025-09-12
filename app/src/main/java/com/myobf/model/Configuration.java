package com.myobf.model;

import com.google.gson.annotations.SerializedName;

public class Configuration {

    @SerializedName("rename")
    private boolean rename = false;

    @SerializedName("stringEncryption")
    private boolean stringEncryption = false;

    @SerializedName("controlFlow")
    private String controlFlow = "none";

    @SerializedName("illegalBytecode")
    private boolean illegalBytecode = false;

    @SerializedName("virtualization")
    private boolean virtualization = false;

    @SerializedName("watermark")
    private String watermark = "";

    @SerializedName("customClassLoader")
    private boolean customClassLoader = false;

    @SerializedName("antiDebug")
    private boolean antiDebug = false;

    @SerializedName("polymorphic")
    private boolean polymorphic = false;

    public boolean isRename() {
        return rename;
    }

    public boolean isStringEncryption() {
        return stringEncryption;
    }

    public String getControlFlow() {
        return controlFlow;
    }

    public boolean isIllegalBytecode() {
        return illegalBytecode;
    }

    public boolean isVirtualization() {
        return virtualization;
    }

    public String getWatermark() {
        return watermark;
    }

    public boolean isCustomClassLoader() {
        return customClassLoader;
    }

    public boolean isAntiDebug() {
        return antiDebug;
    }

    public boolean isPolymorphic() {
        return polymorphic;
    }
}
