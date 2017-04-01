package com.adtime.application.ssh;

/**
 * Created by xuanlubin on 2017/1/5.
 */
public class SSHClientTest {

    public static void main(String[] args) throws Exception {
        SSHClient sshClient = SSHClient.connect("192.168.168.104","appuser",22,"E:\\project\\application-management\\id_rsa");
        sshClient.exec("ls");
        System.out.printf("");
        sshClient.disConnect();
    }

}