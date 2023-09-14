package org.example;

import java.net.*;
import java.io.*;

public class ServiceFacade {

    public static ServerSocket serverSocket = null;
    public static ReflexCalculator reflexCalculator = new ReflexCalculator();
    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        System.out.println("Listo para recibir ...");
        Boolean serverUp = true;
        while (serverUp) {
            Socket clientSocket = serverSocket.accept();
            Thread t = new Thread(() -> {
                try {
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String inputLine, outputLine = "";
                    boolean fristLine = true;
                    String path = "";

                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received: " + inputLine);
                        if (fristLine) {
                            fristLine = false;
                            path = inputLine.split(" ")[1];
                        }
                        if (!in.ready()) {
                            break;
                        }
                    }

                    if (path.startsWith("/calculadora")) {
                        outputLine = browser();
                    }else if (path.startsWith("/computar")){
                        String comand = path.split("=")[0];
                        double num = Double.parseDouble(path.split("=")[1]);
                        String resp= String.valueOf(reflexCalculator.calculatorFacade(comand));
                        outputLine = "HTTP/1.1 200 OK\r\n"
                                + "Content-Type: text/html\r\n"
                                + "\r\n"
                                + "<!DOCTYPE html>\n"
                                + "<html>\n"
                                + "<head>\n"
                                + "<meta charset=\"UTF-8\">\n"
                                + "<title>Title of the document</title>\n"
                                + "</head>\n"
                                + "<body>\n"
                                + resp
                                + "\n"
                                + "</body>\n"
                                + "</html>\n";
                    }


                    out.println(outputLine);

                    out.close();
                    in.close();
                    clientSocket.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });
            t.start();
        }
        serverSocket.close();
    }

    public static String browser(){
        return "HTTP/1.1 200 Ok\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Calculadora</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Ingrese el numero</h1>\n" +
                "        <form action=\"/hello\">\n" +
                "            <label for=\"name\">Number:</label><br>\n" +
                "            <input type=\"text\" id=\"name\" name=\"name\" value=\"Cos(0.57)\"><br><br>\n" +
                "            <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let nameVar = document.getElementById(\"name\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/computar?comando=\"+nameVar);\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n" +
                "\n" +
                "    </body>\n" +
                "</html>";
    }
}