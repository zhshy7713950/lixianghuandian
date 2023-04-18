package wongxd.common.net;

import androidx.annotation.NonNull;
import okhttp3.*;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.File;
import java.io.IOException;

/**
 * 实现文件上传进度监听
 * 关键点是自定义 支持进度反馈的RequestBody：
 * 重写write方法按照自定义的SEGMENT_SIZE 来写文件，从而监听进度。
 * FileProgressRequestBody 以2KB为单位上传，对外暴露回调ProgressListener来发布进度。
 * <p>
 * <p>
 * https://www.jianshu.com/p/b0a2b1f816e1
 * <p>
 * <p>
 * Created by wongxd on 2018/11/26.
 * https://github.com/wongxd
 * wxd1@live.com
 */
public class FileProgressRequestBody extends RequestBody {


    /**
     * 这是一个示例
     *
     * @param url
     * @param uploadName
     * @param filePath
     * @param fileName
     * @return
     */
    public static Request generateRequest(String url, String uploadName, String filePath, String fileName, ProgressListener listener) {
        // 构造上传请求，模拟表单提交文件

        String formData = String.format("form-data;name=%s; filename=%s", uploadName, fileName);
        FileProgressRequestBody filePart = new FileProgressRequestBody(new File(fileName), "application/octet-stream", listener);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(Headers.of("Content-Disposition", formData), filePart)
                .build();

        // 创建Request对象
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return request;

    }

    public interface ProgressListener {
        void transferred(long size);
    }

    public static final int SEGMENT_SIZE = 2 * 1024; // okio.Segment.SIZE
    protected File file;
    protected ProgressListener listener;
    protected String contentType;

    public FileProgressRequestBody(File file, String contentType, ProgressListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
    }

    protected FileProgressRequestBody() {
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;
            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                this.listener.transferred(total);
            }
        } finally {
            Util.closeQuietly(source);
        }
    }

}
