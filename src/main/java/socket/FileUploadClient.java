package socket;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FileUploadClient extends Socket {
    private Logger logger = LoggerFactory.getLogger(FileUploadClient.class);

    private Socket client;            // 客户端
    private static long status = 0;   // 进度条
    private boolean quit = false;     // 退出


    public FileUploadClient(String ip, Integer report) throws IOException {
        super(ip, report);
        this.client = this;
        if (client.getLocalPort() > 0) {
            System.out.println("Client [port: " + client.getLocalPort() + " ] 成功连接服务器");
        } else {
            System.out.println("服务器连接失败");
        }
    }

    public int sendFile(String filepath) {
        DataOutputStream dos = null;  // 上传服务器：输出流
        DataInputStream dis = null;   // 获取服务器：输入流
        Long serverLength = -1L;      // 存储在服务器的文件长度，默认-1
        FileInputStream fis = null;    // 读取文件：输入流

        // 获取：要上传的文件
        File file = new File(filepath);

        // ========================== 节点：文件是否存在 =========================
        if (file.exists()) {
            // 发送：文件名称、文件长度
            try {
                dos = new DataOutputStream(client.getOutputStream());
            } catch (IOException e2) {
                logger.error("Socket客户端：1. 读取数据输出流dos发生错误");
                e2.printStackTrace();
            }

            try {
                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();
            } catch (IOException e2) {
                logger.error("Socket客户端：2. dos向服务器发送文件名，长度发生错误");
                e2.printStackTrace();
            }

            // 获取：已上传文件的长度
            try {
                dis = new DataInputStream(client.getInputStream());
            } catch (IOException e2) {
                logger.error("Socket客户端：3. 读取数据输入流dis发生错误");
                e2.printStackTrace();
            }
            while (serverLength == -1) {
                try {
                    serverLength = dis.readLong();
                } catch (IOException e2) {
                    logger.error("Socket客户端：4. 读取服务端长度发生错误");
                    e2.printStackTrace();
                }
            }

            // 读取：需要上传的文件
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e2) {
                logger.error("Socket客户端：5. 读取本地需要上传的文件发生失败，请确认文件是否存在");
                e2.printStackTrace();
            }

            // 发送：向服务器传输输入流
            try {
                dos = new DataOutputStream(client.getOutputStream());
            } catch (IOException e2) {
                logger.error("Socket客户端：6. 向服务器传输数据输出流dos发生错误");
                e2.printStackTrace();
            }

            System.out.println("=============== 开始传输文件 ================");

            byte[] bytes = new byte[1024];
            int length = 1024;
            long progress = serverLength;

            // 设置游标，本地文件读取位置。（跳过已传输过的位置）
            if (serverLength == -1L) { serverLength = 0L; }
            try {
                fis.skip(serverLength);
            } catch (IOException e2) {
                logger.error("Socket客户端：7. 设置游标位置发生错误，请确认文件流是否被篡改");
                e2.printStackTrace();
            }

            // 读取文件到数据输出流dos并上传
            try {
                while (((length = fis.read(bytes, 0, bytes.length)) != -1) && quit != true) {
                    dos.write(bytes, 0, length);
                    dos.flush();
                    progress += length;
                    status = (100 * progress / file.length());
                }
            } catch (IOException e) {
                logger.error("Socket客户端：8. 读取文件发生错误并写入数据输出流dos发生错误，请确认文件流是否被篡改");
                e.printStackTrace();
            } finally {
                // 关闭资源
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        logger.error("Socket客户端：9. 关闭文件输入流fis异常");
                        e1.printStackTrace();
                    }
                }
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException e1) {
                        logger.error("Socket客户端：10. 关闭数据输出流dos异常");
                        e1.printStackTrace();
                    }
                }
                try {
                    client.close();
                } catch (IOException e1) {
                    logger.error("Socket客户端：11. 关闭客户端异常");
                    e1.printStackTrace();
                }
            }

            System.out.println("========= 文件传输成功 =========");
        } else {
            logger.error("Socket客户端：0. 文件不存在");
            return -1;
        }
        return 1;
    }


    /**
     * 进度条
     */
    public void statusInfo() {
        Timer time = new Timer();
        time.schedule(new TimerTask() {
            long num = 0;

            @Override
            public void run() {
                if (status > num) {
                    System.out.println("当前进度为： " + status + "%");
                    num = status;
                }
                if (status == 101) { System.gc(); }
            }
        }, 0, 100);
    }

    /**
     * 退出
     */
    public void quit() {
        this.quit = true;
        try {
            this.close();
        } catch (IOException e) {
            System.out.println("服务器关闭发送异常，原因未知");
        }
    }


    public static void main(String[] args) throws IOException {
        String ip = "127.0.0.1";
        String port = "10086";
        FileUploadClient client = new FileUploadClient(ip, 10086);
        String filepath = "/Users/huangbaixi/Desktop/2.jpg";
        client.sendFile(filepath);
    }

}
