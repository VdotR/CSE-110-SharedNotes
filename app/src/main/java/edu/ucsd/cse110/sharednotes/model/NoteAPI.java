package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try {
            var response = client.newCall(request).execute();
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }

    /**
     * GET /notes/{title}
     */
    @WorkerThread
    public Note get (String title) {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET", null)
                .build();

        try {
            var response = client.newCall(request).execute();
            assert response.body() != null;
            var body = response.body().string();
            Log.i("GET", body);
            return Note.fromJSON(body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @AnyThread
    public Future<Note> getAsync(String title) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> get(title));
        return future;
    }

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    /**
     * PUT /notes/{title}
     */
    @WorkerThread
    public void put(String title, Note note) {
        // URLs cannot contain spaces, so we replace them with %20.
        title = title.replace(" ", "%20");

        String jsonBody = note.toJSON();
        RequestBody reqBody = RequestBody.create(jsonBody, JSON);
        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("PUT", reqBody)
                .build();

        try {
            var response = client.newCall(request).execute();
            assert response.body() != null;
            var body = response.body().string();
            Log.i("PUT", body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AnyThread
    public void putAsync(String title, Note note) {
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> put(title, note));
    }
}
