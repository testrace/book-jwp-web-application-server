package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;

import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream();
            Reader inputStreamReader = new InputStreamReader(in);BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            String headerLine;
            String url = null;
            while ((headerLine = bufferedReader.readLine()) != null) {
                log.debug("headerLine = {}", headerLine);
                if (headerLine.startsWith("GET")) {
                    String[] requestUri = headerLine.split(" ");
                    url = requestUri[1];
                    log.debug("url = {}", url);
                    break;
                }
            }

            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            DataOutputStream dos = new DataOutputStream(out);
//            byte[] body = "Hello World".getBytes();
            response200Header(dos, body.length, url.split("\\.")[1].toLowerCase());
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String type) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            if ("html".equals(type)) {
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            } else if ("css".equals(type)) {
                dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            } else if ("js".equals(type)) {
                dos.writeBytes("Content-Type: text/javascript;charset=utf-8\r\n");
            } else if (type.startsWith("woff")) {
                dos.writeBytes("Content-Type: application/font-woff;charset=utf-8\r\n");
            }
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
