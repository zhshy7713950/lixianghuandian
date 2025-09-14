# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#网易一键登录
-dontwarn com.cmic.gen.sdk.**
-keep class com.cmic.gen.sdk.**{*;}
-dontwarn com.unicom.online.account.shield.**
-keep class com.unicom.online.account.shield.** {*;}
-dontwarn com.unicom.online.account.kernel.**
-keep class com.unicom.online.account.kernel.** {*;}
-keep class cn.com.chinatelecom.account.**{*;}
-keep public class * extends android.view.View
-keep class com.netease.nis.quicklogin.entity.**{*;}
-keep class com.netease.nis.quicklogin.listener.**{*;}
-keep class com.netease.nis.quicklogin.QuickLogin{
    public <methods>;
    public <fields>;
}
-keep class com.netease.nis.quicklogin.helper.UnifyUiConfig{*;}
-keep class com.netease.nis.quicklogin.helper.UnifyUiConfig$Builder{
     public <methods>;
     public <fields>;
 }
-keep class com.netease.nis.quicklogin.utils.LoginUiHelper$CustomViewListener{
     public <methods>;
     public <fields>;
}
-keep class com.netease.nis.basesdk.**{
    public *;
    protected *;
}

# ========== AdScope聚合广告SDK混淆配置 ==========

# 基础混淆规则
-keep class android.support.** { *; }
-keep class android.app.**{*;}
-keep class **.R$* {*;}

# AdScope聚合混淆
-dontwarn xyz.adscope.amps.**
-keep class xyz.adscope.amps.** {*; }
-dontwarn xyz.adscope.common.**
-keep class xyz.adscope.common.** {*; }

# AdScope广告渠道适配器混淆
-dontwarn xyz.adscope.amps.adapter.**
-keep class  xyz.adscope.amps.adapter.**{*;}

# 倍孜混淆，不接入bz sdk可以不引入
-dontwarn com.beizi.fusion.**
-dontwarn com.beizi.ad.**
-keep class com.beizi.fusion.** {*; }
-keep class com.beizi.ad.** {*; }

# 广点通混淆，不接入gdt sdk可以不引入
-keep class com.qq.e.** {
	public protected *;
}

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-dontwarn  org.apache.**

# 穿山甲-GroMore融合版本广告渠道混淆 不接入csj sdk可以不引入
-keep class bykvm*.**
-keep class com.bytedance.msdk.adapter.**{ public *; }
-keep class com.bytedance.msdk.api.** {
 public *;
}

# 快手广告渠道混淆 不接入ks sdk可以不引入
-keep class org.chromium.** {*;}
-keep class org.chromium.** { *; }
-keep class aegon.chrome.** { *; }
-keep class com.kwai.**{ *; }
-keep class com.yxcorp.kuaishou.addfp.android.Orange {*;}
-dontwarn com.kwai.**
-dontwarn com.kwad.**
-dontwarn com.ksad.**
-dontwarn aegon.chrome.**

