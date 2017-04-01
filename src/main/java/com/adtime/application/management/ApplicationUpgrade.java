package com.adtime.application.management;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class ApplicationUpgrade implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationUpgrade.class);

    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(10);

    private List<RemoteServer> serverList;

    public ApplicationUpgrade(List<RemoteServer> serverList) {
        this.serverList = serverList;
    }

    private void execAll(Consumer<RemoteServer> clientConsumer) {
        CountDownLatch latch = new CountDownLatch(serverList.size());
        for (RemoteServer server : serverList) {
            SERVICE.execute(() -> {
                try {
                    if (null == server.getSshClient()) {
                        try {
                            server.connect();
                        } catch (Exception e) {
                            logger.error("连接服务器异常:::" + server.getIp(), e);
                        }
                    }
                    clientConsumer.accept(server);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void upload(String localFile, String remotePath) {
        execAll(server -> {
            try {
                server.getSshClient().uploadLocalFile(localFile, remotePath);
            } catch (JSchException | IOException | SftpException e) {
                logger.error("文件上传异常!!!!" + server.getIp(), e);
            }
        });
    }

    public void exec(String cmd) {
        execAll(server -> {
            try {
                logger.info("exec {} {}", server.getIp(), cmd);
                server.getSshClient().exec(cmd);
            } catch (Exception e) {
                logger.error("执行命令异常!!!!", e);
            }
        });
    }

    @Override
    public void close() throws IOException {
        execAll(server -> server.getSshClient().disConnect());
    }
}
