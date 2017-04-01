package com.adtime.application.ssh;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * Created by xuanlubin on 2016/12/27.
 */
public class SSHClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(SSHClient.class);

    private static final int TIME_OUT = 600000;

    private Session session = null;
    private Channel channel = null;
    private final String ip;

    @Override
    public void close() throws IOException {
        disConnect();
    }

    private SSHClient(String ip, String user, String pwd, int port) throws JSchException {
        this.ip = ip;

        JSch jsch = new JSch();
        if (port <= 0) {
            //连接服务器，采用默认端口
            session = jsch.getSession(user, ip);
        } else {
            //采用指定的端口连接服务器
            session = jsch.getSession(user, ip, port);
        }

        //如果服务器连接不上，则抛出异常
        if (session == null) {
            throw new RuntimeException("session is null");
        }

        //设置登陆主机的密码
        session.setPassword(pwd);//设置密码
        //设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        //设置登陆超时时间
        session.connect(TIME_OUT);

        logger.info("已连接服务器:{}", ip);
    }

    private SSHClient(String ip, String user, int port, String idRsa) throws JSchException {
        this.ip = ip;

        JSch jsch = new JSch();

        jsch.addIdentity(idRsa);

        if (port <= 0) {
            //连接服务器，采用默认端口
            session = jsch.getSession(user, ip);
        } else {
            //采用指定的端口连接服务器
            session = jsch.getSession(user, ip, port);
        }

        //如果服务器连接不上，则抛出异常
        if (session == null) {
            throw new RuntimeException("session is null");
        }

        //设置登陆主机的密码
        //设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        //设置登陆超时时间
        session.connect(TIME_OUT);

        logger.info("已连接服务器:{}", ip);
    }

    public int exec(String cmd)
            throws Exception {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(cmd);
        channelExec.setInputStream(null);
        channelExec.setErrStream(System.err);
        InputStream in = channelExec.getInputStream();
        channelExec.connect();
        int res = -1;
        StringBuilder buf = new StringBuilder(1024);
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) break;
                buf.append(new String(tmp, 0, i));
            }
            if (channelExec.isClosed()) {
                res = channelExec.getExitStatus();
                logger.info("\r\nIP:{}\r\nExit-status: {}", ip, res);
                break;
            }

        }
        logger.info("\r\nIP:{} \r\n{}", ip, buf.toString());
        channelExec.disconnect();
        return res;
    }

    private void openSFTP() throws JSchException {

        if (channel != null && channel.isConnected()) {
            return;
        }
        //创建sftp通信通道
        channel = session.openChannel("sftp");
        channel.connect(30000);
    }

    public void disConnect() {
        if (null != channel) {
            channel.disconnect();
        }
        if (null != session) {
            session.disconnect();
        }
    }

    class UploadInfo {
        int file = 0;
        int dir = 0;
        long size = 0;
        long start = System.currentTimeMillis();
    }

    /**
     * 上传本地文件到远程服务器
     *
     * @param file       本地文件
     * @param remotePath 远程服务器
     */
    public UploadInfo uploadLocalFile(File file, String remotePath, UploadInfo uploadInfo) throws JSchException, SftpException, IOException {
        if (null == channel) {
            openSFTP();
        }

        if (null == uploadInfo) {
            uploadInfo = new UploadInfo();
        }

        UploadInfo _uploadInfo = uploadInfo;

        ChannelSftp sftp = (ChannelSftp) channel;

        //进入服务器指定的文件夹
        sftp.cd(remotePath);

        if (file.isDirectory()) {
            try {
                sftp.mkdir(file.getName());
                File[] files = file.listFiles();
                if (null != files) {
                    for (File file1 : files) {
                        uploadLocalFile(file1, remotePath + "/" + file.getName(), uploadInfo);
                    }
                }
                uploadInfo.dir++;
            } catch (SftpException e) {
                logger.error("[{}]无法创建目录: {}", ip, file.getName());
                throw e;
            }
        } else {
            //以下代码实现从本地上传一个文件到服务器，如果要实现下载，对换以下流就可以了
            try {
                InputStream is = new FileInputStream(file);
                sftp.put(is, file.getName(), new SftpProgressMonitor() {

                    private long upload = 0;

                    @Override
                    public void init(int i, String s, String s1, long l) {
                        logger.debug("上传文件:{} 远程路径:{}", s, s1);
                    }

                    @Override
                    public boolean count(long l) {
                        upload += l;
                        return true;
                    }

                    @Override
                    public void end() {
                        IOUtils.closeQuietly(is);
                        logger.debug("文件上传完成:{} 文件大小：{}", file, upload);
                        _uploadInfo.size += upload;
                    }
                });
                uploadInfo.file++;
            } catch (SftpException e) {
                logger.error("[{}]文件上传失败: {}", ip, file.getName());
                throw e;
            }
        }
        return uploadInfo;
    }

    public void uploadLocalFile(String localFile, String remotePath) throws JSchException, SftpException, IOException {
        UploadInfo info = uploadLocalFile(new File(localFile), remotePath, null);
        logger.info("[{}]文件上传完毕:新建目录{} 上传文件:{} 文件大小:{} 耗时:{}", ip, info.dir, info.file, info.size, System.currentTimeMillis() - info.start);
    }


    class DownloadInfo {
        int file = 0;
        long size = 0;
    }


    /**
     * 上传本地文件到远程服务器
     *
     * @param remotePath 远程服务器
     */
    public DownloadInfo downloadRemoteFile(String remotePath, boolean rmRemoteFile, OutputStreamFactory streamFactory, Function<ChannelSftp.LsEntry, Boolean> selector) throws JSchException, SftpException, IOException, InterruptedException {
        if (null == channel) {
            openSFTP();
        }

        DownloadInfo downloadInfo = new DownloadInfo();

        ChannelSftp sftp = (ChannelSftp) channel;


        Queue<ChannelSftp.LsEntry> entryList = new LinkedBlockingQueue<>(1000);

        sftp.ls(remotePath, lsEntry -> {
            if (selector.apply(lsEntry)) {
                entryList.add(lsEntry);
            }
            return entryList.size() == 1000 ? 1 : 0;
        });

        if (entryList.isEmpty()) {
            return downloadInfo;
        }

        CountDownLatch latch = new CountDownLatch(entryList.size());


        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                try {
                    new Downloader(downloadInfo).download(remotePath, entryList, rmRemoteFile, latch, streamFactory);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.setName("Downloader-" + i);
            thread.start();
        }

        latch.await();
        return downloadInfo;
    }


    private void deleteFile(ChannelSftp sftp, String file) {
        try {
            logger.debug("删除文件:{}", file);
            sftp.rm(file);
        } catch (SftpException e) {

        }
    }

    private class Downloader {
        private final ChannelSftp ftp;
        private DownloadInfo downloadInfo;

        public Downloader(DownloadInfo downloadInfo) throws JSchException {
            ftp = (ChannelSftp) SSHClient.this.session.openChannel("sftp");
            ftp.connect(30000);
            this.downloadInfo = downloadInfo;
        }

        void download(String remotePath, Queue<ChannelSftp.LsEntry> queue, boolean rmRemoteFile, CountDownLatch count, OutputStreamFactory streamFactory) throws Exception {
            try {
                while (true) {
                    ChannelSftp.LsEntry entry = queue.poll();
                    if (null == entry) {
                        break;
                    }
                    if (entry.getAttrs().getSize() == 0) {
                        if (rmRemoteFile) {
                            deleteFile(ftp, remotePath + "/" + entry.getFilename());
                        }
                        count.countDown();
                        continue;
                    }
                    String remoteFile = remotePath + "/" + entry.getFilename();
                    OutputStream outputStream = streamFactory.create(ip, remoteFile);
                    try {
                        ftp.get(remoteFile, outputStream, new SftpProgressMonitor() {

                            private long current = 0, max;

                            private String remoteFile;

                            @Override
                            public void init(int i, String s, String s1, long l) {
                                max = l;
                                remoteFile = s;
                                logger.debug("开始下载文件:{}", s);
                            }

                            @Override
                            public boolean count(long l) {
                                current += l;
                                return true;
                            }

                            @Override
                            public void end() {
                                downloadInfo.size += current;
                                logger.debug("文件下载完成: {} {}/{}", remoteFile, current, max);
                                if (rmRemoteFile) {
                                    deleteFile(ftp, remoteFile);
                                }
                                count.countDown();
                                streamFactory.finish(ip, remoteFile, outputStream);
                            }
                        });
                        downloadInfo.file++;
                    } catch (Exception e) {
                        logger.warn("[{}] 下载文件[{}]异常:{}", ip, remoteFile, e);
                    }
                }
            } finally {
                ftp.disconnect();
            }
        }
    }

    public static SSHClient connect(String ip, String user, String pwd, int port) throws JSchException {
        return new SSHClient(ip, user, pwd, port);
    }

    public static SSHClient connect(String ip, String user, int port, String idRsa) throws JSchException {
        return new SSHClient(ip, user, port, idRsa);
    }
}
