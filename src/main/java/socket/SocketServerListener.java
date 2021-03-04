package socket;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Timer;
import java.util.TimerTask;

public class SocketServerListener extends HttpServlet {
    // 用来保证在反序列时，发送方发送的和接受方接收的是可兼容的对象。
    private static final long serialVersionUID = -999999999999999999L;

    @Override
    public void init() throws ServletException {
        super.init();
        for (int i = 0; i < 3; i++) {
            if("instart".equals(FinalVariables.IS_START_SERVER)) {
                open();
                break;
            }
        }
    }


    public void open() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @SuppressWarnings("resource")
            @Override
            public void run() {
                try {
                    FileUpLoadServer fileUpLoadServer = new FileUpLoadServer(FinalVariables.SERVER_PORT);
                    fileUpLoadServer.load();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
    }
}

