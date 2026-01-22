package ro.pub.cs.systems.eim.practicaltest02v10.network;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import ro.pub.cs.systems.eim.practicaltest02v10.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02v10.general.Utilities;


public class ClientThread extends Thread {

    private final String address;
    private final int port;
    private final String pokemonName;

    private final TextView abilitiesTextView;

    private final TextView typesTextView;
    private final ImageView pokemonImageView;

    public ClientThread(String address, int port, String pokemonName,
                        TextView abilitiesTextView, TextView typeTextView, ImageView pokemonImageView) {
        this.address = address;
        this.port = port;
        this.pokemonName = pokemonName;
        this.abilitiesTextView = abilitiesTextView;
        this.typesTextView = typeTextView;
        this.pokemonImageView = pokemonImageView;
    }

    @Override
    public void run() {
        Socket socket = null;

        try {
            socket = new Socket(address, port);

            BufferedReader in = Utilities.getReader(socket);
            PrintWriter out = Utilities.getWriter(socket);

            out.println(pokemonName);
            out.flush();

            String abilitiesLine = in.readLine();
            String imageLine = in.readLine();
            String typeLine = in.readLine();

            if (abilitiesLine == null) abilitiesLine = "Abilities: (no response)";
            final String abilitiesFinal = abilitiesLine;

            if (typeLine == null) abilitiesLine = "Type: (no response)";
            final String typeFinal = typeLine;

            final String imageUrl = extractImageUrl(imageLine);

            abilitiesTextView.post(() -> abilitiesTextView.setText(abilitiesFinal));
            typesTextView.post(() -> typesTextView.setText(typeFinal));


            if (imageUrl != null && !imageUrl.isEmpty()) {
                downloadAndSetImage(imageUrl);
            } else {
                pokemonImageView.post(() -> pokemonImageView.setImageDrawable(null));
            }

        } catch (Exception e) {
            Log.e(Constants.TAG, "[CLIENT] Error: " + e.getMessage());
            if (Constants.DEBUG) e.printStackTrace();
            final String err = "Error: " + e.getMessage();
            abilitiesTextView.post(() -> abilitiesTextView.setText(err));
            pokemonImageView.post(() -> pokemonImageView.setImageDrawable(null));
        } finally {
            try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        }
    }

    private String extractImageUrl(String line) {
        if (line == null) return null;
        line = line.trim();
        if (!line.startsWith(Constants.IMAGE_PREFIX)) return null;
        return line.substring(Constants.IMAGE_PREFIX.length()).trim();
    }

    private void downloadAndSetImage(String imageUrl) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream input = null;
            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                pokemonImageView.post(() -> pokemonImageView.setImageBitmap(bitmap));

            } catch (Exception e) {
                Log.e(Constants.TAG, "[CLIENT] Image error: " + e.getMessage());
                if (Constants.DEBUG) e.printStackTrace();
                pokemonImageView.post(() -> pokemonImageView.setImageDrawable(null));
            } finally {
                try { if (input != null) input.close(); } catch (Exception ignored) {}
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}
