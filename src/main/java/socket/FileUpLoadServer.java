package socket;

import java.io.*;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;

/**
 * 1、服：利用ServerSocket搭建服务器，开启相应端口，进行长连接操作
 * 2、服：使用ServerSocket.accept()方法进行阻塞，接收客户端请求
 * 3、服：每接收到一个Socket就建立一个新的线程来处理它
 * 4,5 客
 * 6、服：接收客户端输入流，使用RandomAccessFile.seek(long length)随机读取，将游标移动到指定位置进行读写
 * 7、客/服：一个循环输出，一个循环读取写入
 */
public class FileUpLoadServer extends ServerSocket {
    // 文件大小
    private static DecimalFormat df = null;
    // 退出标识
    private boolean quit = false;

    // 设置数字格式，保留一位有效小数
    static {
        // 保留一位小数，整数部分不变
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }

    public FileUpLoadServer(int report) throws IOException {
        super(report);
    }

    /**
     * 使用线程处理每个客户端传输的文件
     *
     * @throws Exception
     */
    public void load() throws Exception {
        System.out.println("【文件上传】服务器：" + this.getInetAddress() + "正在进行中...");
        while(!quit) {
            Socket socket = this.accept();
            /**
             * 我们的服务端处理客户端的连接请求是同步进行的， 每次接收到来自客户端的连接请求后，
             * 都要先跟当前的客户端通信完之后才能再处理下一个连接请求。 这在并发比较多的情况下会严重影响程序的性能，
             * 为此，我们可以把它改为如下这种异步处理与客户端通信的方式
             */
            // 收到请求，验证合法性
            String ip = socket.getInetAddress().toString();
            ip = ip.substring(1, ip.length());
            System.out.println("服务器接收到请求，正在开启验证对方合法性IP：" + ip + "! ");
            // 每接收到一个Socket就建立一个新的线程来处理它
            new Thread(new Task(socket, ip)).start();
        }
    }


    public static String getFormatFileSize(long length) {
        double size = ((double) length) / (1 << 30);
        if (size >= 1) { return df.format(size) + "GB"; }

        size = ((double) length) / (1 << 20);
        if (size >= 1) { return df.format(size) + "MB"; }

        size = ((double) length) / (1 << 10);
        if (size >= 1) { return df.format(size) + "KB"; }

        return length + "B";
    }

    public void quit() {
        this.quit = true;
        try {
            this.close();
        } catch (IOException e) {
            System.out.println("服务器关闭发送异常");
        }
    }


}
