package ro.pub.cs.systems.eim.practicaltest02v10.network;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02v10.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02v10.general.HttpUtil;
import ro.pub.cs.systems.eim.practicaltest02v10.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02v10.model.PokemonInfo;


public class CommunicationThread extends Thread {

    private final ServerThread serverThread;
    private final Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintWriter out = null;

        try {
            if (socket == null) {
                Log.e(Constants.TAG, "[COMM] Socket is null!");
                return;
            }

            BufferedReader in = Utilities.getReader(socket);
            out = Utilities.getWriter(socket);


            String pokemonName = in.readLine();
            if (pokemonName == null) {
                out.println("Error: missing pokemon name");
                out.println(Constants.IMAGE_PREFIX);
                return;
            }

            pokemonName = pokemonName.trim().toLowerCase();
            if (pokemonName.isEmpty()) {
                out.println("Error: empty pokemon name");
                out.println(Constants.IMAGE_PREFIX);
                return;
            }

            Log.i(Constants.TAG, "[COMM] Request: " + pokemonName);

            PokemonInfo info = serverThread.getFromCache(pokemonName);

            if (info == null) {
                Log.i(Constants.TAG, "[COMM] Cache MISS");
                String url = Constants.POKE_API_BASE + pokemonName;
                String jsonText = HttpUtil.fetchUrl(url);

                if (jsonText == null) {
                    out.println("Error: pokemon not found or network error");
                    out.println(Constants.IMAGE_PREFIX);
                    return;
                }

                JSONObject root = new JSONObject(jsonText);

                JSONArray abilitiesArray = root.getJSONArray("abilities");
                JSONArray typesArray = root.getJSONArray("types");
                String firstAbility = "";
                String firstType = "";

                if (abilitiesArray.length() > 0) {
                    JSONObject abObj = abilitiesArray.getJSONObject(0).getJSONObject("ability");
                    firstAbility = abObj.getString("name");
                }
                String imageUrl = root.getJSONObject("sprites").optString("front_default", "");

                if (typesArray.length() > 0) {
                    JSONObject typeObj = typesArray.getJSONObject(0).getJSONObject("type");
                    firstType = typeObj.getString("name");
                }
                info = new PokemonInfo(pokemonName, firstType, firstAbility, imageUrl);
                serverThread.putInCache(pokemonName, info);

            } else {
                Log.i(Constants.TAG, "[COMM] Cache HIT");
            }

            out.println(Constants.ABILITIES_PREFIX + info.getAbilities());
            out.println(Constants.IMAGE_PREFIX + info.getImageUrl());
            out.println(info.getTypes());
            out.flush();

        } catch (Exception e) {
            Log.e(Constants.TAG, "[COMM] Error: " + e.getMessage());
            if (Constants.DEBUG) e.printStackTrace();
            if (out != null) {
                out.println("Error: " + e.getMessage());
                out.println(Constants.IMAGE_PREFIX);
            }
        } finally {
            try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        }
    }
}
