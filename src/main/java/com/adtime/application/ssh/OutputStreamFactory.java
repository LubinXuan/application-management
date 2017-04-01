package com.adtime.application.ssh;

import java.io.OutputStream;

/**
 * Created by xuanlubin on 2017/1/5.
 */
public interface OutputStreamFactory {
    OutputStream create(String server, String remoteFile) throws Exception;

    void finish(String server, String remoteFile, OutputStream outputStream);
}
