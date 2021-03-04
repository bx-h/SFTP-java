package socket;

import java.io.*;
import java.net.Socket;

class Task implements Runnable {
    private Socket sk; // 当前连接
    private String ips; // 当前ip地址

    public Task(Socket socket, String ip) {
        this.sk = socket;
        this.ips = ip;
    }

    @Override
    public void run() {
        Socket socket = sk;                 //  重新定义，请不要移出run()方法外部，否则连接两会被重置
        String ip = ips;                    //  重新定义，同上IP会变
        long serverLength = -1L;            //  定义：存放在服务器里的文件长度，默认没有为-1
        char pathChar = File.separatorChar; //  获取：系统路径分隔符
        String panFu = "/Users/huangbaixi/Desktop/target";                //  路径：存储文件盘符(待修改！！)
        DataInputStream dis = null;         //  获取：客户端输出流
        DataOutputStream dos = null;        //  发送：向客户端输入流
        FileOutputStream fos = null;        //  读取：服务器本地文件流
        RandomAccessFile rantmpfile = null;  //  操作类：随机读取

        try {
            // 获取
            dis = new DataInputStream(socket.getInputStream());
            // 发送
            dos = new DataOutputStream(socket.getOutputStream());
            // 定义客户端传过来的文件名
            String fileName = "";
            while ("".equals(fileName)) {
                // 读取客户端传来的数据
                fileName = dis.readUTF();
                System.out.println("服务器获取客户端文件名称：" + fileName);
                // panFu在这里使用，组成路径
                File file = new File(panFu + pathChar + "receive" + pathChar + "" + ip + pathChar + fileName);
                if (file.exists()) {
                    serverLength = file.length();
                    dos.writeLong(serverLength);
                    System.out.println("向客户端返回文件长度：" + serverLength + " B");
                } else {
                    serverLength = 0L;
                    dos.writeLong(serverLength);
                    System.out.println("文件不存在");
                    System.out.println("向客户端返回文件长度：" + serverLength + " B");
                }
            }

            System.out.println("服务器建立新线程处理客户端请求，对方IP：" + ip + "，传输正在进行中...");
            // 从客户端获取输入流
            dis = new DataInputStream(socket.getInputStream());
            // 文件名和长度
            long fileLength = dis.readLong();
            File directory = new File(panFu + pathChar + "receive" + pathChar + "" + ip + pathChar);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            int length = 0;
            byte[] bytes = new byte[1024];
            File file = new File(directory.getAbsolutePath() + pathChar + fileName);
            if (!file.exists()) {
                // 不存在
                fos = new FileOutputStream(file);
                // 开始接收文件
                while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                }
            } else {
                // 存储在服务器中的文件长度
                long fileSize = file.length(), pointSize = 0;
                // 判断是否已下载完成
                if (fileLength >= fileSize) {
                    // 断点下载
                    pointSize = fileSize;
                } else {
                    // 重新下载
                    file.delete();
                    file.createNewFile();
                }
                rantmpfile = new RandomAccessFile(file, "rw");
                /*
                 * java.io.InputStream.skip() 用法：跳过 n 个字节（丢弃） 如果 n
                 * 为负，则不跳过任何字节。
                 */
                // dis.skip(pointSize); （已从客户端读取进度）
                /*
                 * 资源，文件定位（游标、指针） 将ras的指针设置到8，则读写ras是从第9个字节读写到
                 */
                rantmpfile.seek(pointSize);
                while ((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    rantmpfile.write(bytes, 0, length);
                }

            }
            System.out.println("=========文件接收成功[File Name: " + fileName + "] [ClientIP: " + ip + "] [Size: " + FileUpLoadServer.getFormatFileSize(file.length()) + "] =======");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) { fos.close(); }
                if (dis != null) { dis.close(); }
                if (rantmpfile != null) { rantmpfile.close(); }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Socket关闭失败！");
            }
            /**
             *   文件传输完毕：执行后续操作（略）
             */
            //TODO 录入数据库，记录文件信息：文件ID、文件名称、文件路径、文件MD5值
            //FileInfo fi= new FileInfo()
            //fi.setId(UUID.randomUUID().toString().replace("-", ""));
            //fi.setFileName("断点续传：xxxxxxx号文件.zip");
            //fi.setFilePath("D:\receive\296.245.235.254\断点续传：xxxxxxx号文件.zip");
            //fi.setMD5Val("578F48BF49EA461A9FDDFA3E68A72EF8");

        }
    }
}