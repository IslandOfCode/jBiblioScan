package it.islandofcode.mybarcodescanner.it.islandofcode.mybarcodescanner.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import it.islandofcode.mybarcodescanner.ProcessNetData;

//AsyncTask<PARAMETRI, PROGRESSO, RISULTATO>
//Il primo è l'input, il secondo come comunicare i progressi e il terzo è l'output

public class NetworkOp extends AsyncTask<String, Void, String> implements Serializable {

    public static final String DISCONNECTION = "BYE";

    private ProcessNetData dataProcessor;

    public NetworkOp(ProcessNetData PND){
        this.dataProcessor = PND;
    }


    @Override
    protected String doInBackground(String... strings) {
        String response;

        String path = strings[0];
        Log.d("JBIBLIO","PATH NETOP: " +path);

        if(!isCancelled() && path.length()>0){
            try{
                URL url = new URL(path);
                response = downloadUrl(url);
                Log.d("JBIBLIO","RESPONSE NETOP DOWNLOADURL: " +response);
                if(response != null && !response.isEmpty())
                    return response;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("JBIBLIO", "doInBackground: " + Objects.requireNonNull(e.getMessage()));
            }
        }
        Log.d("JBIBLIO","RESPONSE NETOP DOWNLOADURL NULL!!!!");
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);
        if(s==null){
            Log.e("JBIBLIO", "Stringa NULLA di ritorno dall'operazione di rete");
        }
        this.dataProcessor.process(s);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    private String downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(5000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(5000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();

            //publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
            //TODO decommentami
            /*int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }*/
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            //publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                result = readStream(stream, 500);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    /**
     * Converts the contents of an InputStream to a String.
     */
    private String readStream(InputStream stream, int maxReadSize)
            throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuffer buffer = new StringBuffer();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }


}
