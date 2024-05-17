import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;

@WebServlet("/*")
public class LogSearchServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String logFilePath = "/opt/service/logs/maintenance/app.log";
        int numLinesToCheck = 200;
        String[] searchStrings = {"DeviceResult=1010", "TempResult=2020", "BodyResult=3030"};

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try (RandomAccessFile file = new RandomAccessFile(logFilePath, "r")) {
            long length = file.length();
            long position = length - 1;
            StringBuilder lastLines = new StringBuilder();

            int linesRead = 0;
            while (position >= 0 && linesRead < numLinesToCheck) {
                file.seek(position);
                int currentByte = file.read();
                if (currentByte == '\n') {
                    String line = lastLines.reverse().toString();
                    for (String searchString : searchStrings) {
                        if (line.contains(searchString)) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.println("<h1>Error detected in log file</h1>");
                            return;
                        }
                    }
                    lastLines.setLength(0);
                    linesRead++;
                } else if (currentByte != '\r') {
                    lastLines.append((char) currentByte);
                }
                position--;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.println("<h1>No error detected in log file</h1>");
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("<h1>Error reading log file</h1>");
            e.printStackTrace(out);
        }
    }
}
