/*
 * JBoss Admin
 * Copyright 2012, Christos Vasilakis, and individual contributors.
 * See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.cvasilak.jboss.mobile.admin.net;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.model.Server;
import org.cvasilak.jboss.mobile.admin.net.ssl.CustomHTTPClient;
import org.cvasilak.jboss.mobile.admin.util.CustomMultiPartEntity;

import java.io.File;

public class UploadToJBossServerTask extends AsyncTask<File, Integer, JsonElement> {

    private static final String TAG = UploadToJBossServerTask.class.getSimpleName();

    private Context context;

    private AbstractHttpClient client;

    private Server server;

    private Callback callback;

    private Exception exception;

    private boolean isTaskFinished = false;

    private Gson gjson;
    private JsonParser parser;

    private ProgressDialog progressDialog;


    public UploadToJBossServerTask(Context context, Server server, Callback callback) {
        this.context = context;
        this.client = CustomHTTPClient.getHttpClient();
        this.server = server;
        this.callback = callback;

        this.gjson = ((JBossAdminApplication) context.getApplicationContext()).getJSONParser();
        this.parser = new JsonParser();

        // enable digest authentication
        if (server.getUsername() != null && !server.getUsername().equals("")) {
            Credentials credentials = new UsernamePasswordCredentials(server.getUsername(), server.getPassword());

            client.getCredentialsProvider().setCredentials(new AuthScope(server.getHostname(), server.getPort(), AuthScope.ANY_REALM), credentials);
        }
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.uploading));
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    @Override
    protected JsonElement doInBackground(File... objects) {
        if (client == null) {
            return null;
        }

        final File file = objects[0];
        final long length = file.length();

        try {
            CustomMultiPartEntity multipart = new CustomMultiPartEntity(new CustomMultiPartEntity.ProgressListener() {
                @Override
                public void transferred(long num) {
                    //Log.d(TAG, "length=" + length + " num=" + num);
                    publishProgress((int) ((num * 100) / length));
                }
            });

            multipart.addPart(file.getName(), new FileBody(file));

            HttpPost httpRequest = new HttpPost(server.getHostPort() + "/management/add-content");
            httpRequest.setEntity(multipart);

            HttpResponse serverResponse = client.execute(httpRequest);

            BasicResponseHandler handler = new BasicResponseHandler();
            String response = handler.handleResponse(serverResponse);

            Log.d(TAG, "<-------- " + response);

            return parser.parse(response).getAsJsonObject();

        } catch (Exception e) {
            this.exception = e;
            cancel(true);
        }

        return null;
    }

    @Override
    protected void onPostExecute(JsonElement reply) {
        super.onPostExecute(reply);

        progressDialog.dismiss();

        isTaskFinished = true;

        if (callback == null)
            return;

        if (!reply.isJsonObject())
            callback.onFailure(new RuntimeException("Invalid Response from server!"));

        JsonObject jsonObj = (JsonObject) reply;

        if (!jsonObj.has("outcome"))
            callback.onFailure(new RuntimeException("Invalid Response from server!"));
        else {
            String outcome = jsonObj.get("outcome").getAsString();

            if (outcome.equals("success")) {
                callback.onSuccess(jsonObj.get("result"));
            } else {
                JsonElement elem = jsonObj.get("failure-description");

                if (elem.isJsonPrimitive()) {
                    callback.onFailure(new RuntimeException(elem.getAsString()));
                } else if (elem.isJsonObject())
                    callback.onFailure(new RuntimeException(elem.getAsJsonObject().get("domain-failure-description").getAsString()));
            }
        }

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress((int) (progress[0]));
    }

    @Override
    protected void onCancelled() {
        progressDialog.dismiss();

        isTaskFinished = true;

        if (callback != null)
            callback.onFailure(this.exception);
    }

    public boolean isTaskFinished() {
        return isTaskFinished;
    }

    public void attach(Callback callback) {
        this.callback = callback;
    }

    public void detach() {
        this.callback = null;
    }
}
