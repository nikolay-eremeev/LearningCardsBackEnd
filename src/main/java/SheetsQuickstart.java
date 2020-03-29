import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import java.io.PrintStream;
import java.io.*;
import java.net.*;
import java.util.Date;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.Scanner;

import java.nio.charset.StandardCharsets;

public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String HOST_INFO_FILE_PATH = "/host_info.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                        .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static String getVocab() throws IOException, GeneralSecurityException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = SheetsQuickstart.class.getResourceAsStream(HOST_INFO_FILE_PATH);

        final String spreadsheetId = "1DS0Nzcu4TQ4jqT9zhZQFyj9XYUggkzUCGANhjVjCpdU";
        final String range = "Saved translations!A1:E";

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();

        List<List<Object>> values = response.getValues();
        List<String> retStrings = new ArrayList<String>();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List<Object> row : values) {
                List<String> tmp = new ArrayList<String>();
                for (Object entry : row) {
                    tmp.add(entry.toString());
                }
                retStrings.add(String.join("\t", tmp));
            }
        }
        return String.join(":::", retStrings);
    }

    private static volatile Thread thread;

    public static void main(String[] args) {

        int port = 6868;

        try {
            PrintStream outStream = new PrintStream(System.out, true, "UTF-8");
            outStream.println("Hello world!");
            outStream.println("хуйхуйхуй");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Caught exception: " + e.getMessage());
        }

        thread = new Thread() {
            public void run() {
                while (true) {
                    try (ServerSocket serverSocket = new ServerSocket(port)) {
                        String retval = getVocab();
                        System.out.println("Server is listening on port " + port);
                        System.out.println("Press x and then Enter to escape the Programm!!!");
                        System.out.flush();
                        Socket socket = serverSocket.accept();
                        System.out.println("New client connected");
                        OutputStream output = socket.getOutputStream();

                        PrintWriter writer = new PrintWriter(
                                new BufferedWriter(new OutputStreamWriter(output, "UTF-8")), true);

                        writer.println(retval);
                        System.out.println("Data has been sent");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.start();

        (new Scanner(System.in)).nextLine();

        thread.interrupt();

    }

}
