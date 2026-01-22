package ro.pub.cs.systems.eim.practicaltest02v10.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ro.pub.cs.systems.eim.practicaltest02v10.R;
import ro.pub.cs.systems.eim.practicaltest02v10.network.ClientThread;
import ro.pub.cs.systems.eim.practicaltest02v10.network.ServerThread;

public class PracticalTest02v10MainActivity extends AppCompatActivity {

    private EditText pokemonNameEditText;
    private Button getInfoButton;

    private EditText serverPortEditText;
    private Button startServerButton;

    private TextView abilitiesTextView;

    private TextView typesTextView;
    private ImageView pokemonImageView;

    private ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02v10_main);

        pokemonNameEditText = findViewById(R.id.pokemon_name_edit_text);
        getInfoButton = findViewById(R.id.get_info_button);

        serverPortEditText = findViewById(R.id.server_port_edit_text);
        startServerButton = findViewById(R.id.start_server_button);

        abilitiesTextView = findViewById(R.id.abilities_text_view);
        typesTextView = findViewById(R.id.types_text_view);
        pokemonImageView = findViewById(R.id.pokemon_image_view);

        startServerButton.setOnClickListener(v -> {
            String portStr = serverPortEditText.getText().toString().trim();
            if (portStr.isEmpty()) {
                Toast.makeText(this, "Server port required!", Toast.LENGTH_SHORT).show();
                return;
            }
            int port = Integer.parseInt(portStr);

            serverThread = new ServerThread(port);
            if (serverThread.getServerSocket() == null) {
                Toast.makeText(this, "Could not start server!", Toast.LENGTH_SHORT).show();
                return;
            }
            serverThread.start();
            Toast.makeText(this, "Server started!", Toast.LENGTH_SHORT).show();
        });

        getInfoButton.setOnClickListener(v -> {
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(this, "Start server first!", Toast.LENGTH_SHORT).show();
                return;
            }

            String name = pokemonNameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Pokemon name required!", Toast.LENGTH_SHORT).show();
                return;
            }

            String portStr = serverPortEditText.getText().toString().trim();
            int port = Integer.parseInt(portStr);

            String addr = "127.0.0.1";

            abilitiesTextView.setText("Loading...");
            typesTextView.setText("Loading...");
            pokemonImageView.setImageDrawable(null);

            new ClientThread(addr, port, name, abilitiesTextView, typesTextView,  pokemonImageView).start();
        });
    }

    @Override
    protected void onDestroy() {
        if (serverThread != null) serverThread.stopThread();
        super.onDestroy();
    }
}