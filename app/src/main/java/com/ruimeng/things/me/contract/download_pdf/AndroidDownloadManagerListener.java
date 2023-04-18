package com.ruimeng.things.me.contract.download_pdf;

/**
 * Created by HyFun on 2019/05/27.
 * Email: 775183940@qq.com
 * Description:
 */
public interface AndroidDownloadManagerListener {
    void onPrepare();

    void onSuccess(String path);

    void onFailed(Throwable throwable);
}