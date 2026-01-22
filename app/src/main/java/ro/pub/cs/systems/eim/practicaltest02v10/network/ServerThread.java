package ro.pub.cs.systems.eim.practicaltest02v10.network;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import ro.pub.cs.systems.eim.practicaltest02v10.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02v10.model.PokemonInfo;


public class ServerThread extends Thread {

    private final int port;
    private ServerSocket serverSocket;

    // cache: key = pokemon name (lowercase)
    private final HashMap<String, PokemonInfo> cache = new HashMap<>();

    public ServerThread(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            Log.e(Constants.TAG, "[SERVER] Could not create ServerSocket: " + e.getMessage());
            if (Constants.DEBUG) e.printStackTrace();
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public synchronized PokemonInfo getFromCache(String name) {
        return cache.get(name);
    }

    public synchronized void putInCache(String name, PokemonInfo info) {
        cache.put(name, info);
    }

    @Override
    public void run() {
        if (serverSocket == null) {
            Log.e(Constants.TAG, "[SERVER] ServerSocket is null. Server not started.");
            return;
        }

        try {
            while (!isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER] Waiting for a client...");
                Socket socket = serverSocket.accept();
                new CommunicationThread(this, socket).start();
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "[SERVER] accept() error: " + e.getMessage());
            if (Constants.DEBUG) e.printStackTrace();
        }
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try { serverSocket.close(); } catch (IOException ignored) {}
        }
    }
}
