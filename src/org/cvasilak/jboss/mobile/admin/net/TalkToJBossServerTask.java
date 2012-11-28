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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.model.Server;
import org.cvasilak.jboss.mobile.admin.net.ssl.CustomHTTPClient;
import org.cvasilak.jboss.mobile.admin.util.ParametersMap;

public class TalkToJBossServerTask extends AsyncTask<ParametersMap, Void, JsonObject> {

    private static final String TAG = "TalkToJBossServerTask";

    private Context context;

    private AbstractHttpClient client;

    private Server server;

    private Reply callback;

    private Exception exception;

    private boolean isTaskFinished = false;

    private Gson gjson;
    private JsonParser parser;

    public TalkToJBossServerTask(Context context, Server server, Reply callback) {
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
        super.onPreExecute();
    }

    @Override
    protected JsonObject doInBackground(ParametersMap... objects) {
        if (client == null) {
            return null;
        }

        ParametersMap params = objects[0];

        // ask the server to pretty print
        params.add("json.pretty", Boolean.TRUE);

        try {
            String json = gjson.toJson(params);
            StringEntity entity = new StringEntity(json, "UTF-8");
            entity.setContentType("application/json");

            HttpPost httpRequest = new HttpPost(server.getHostPort() + "/management");
            httpRequest.setEntity(entity);

            Log.d(TAG, "fetching " + httpRequest.getURI());
            Log.d(TAG, "--------> " + json);

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
    protected void onPostExecute(JsonObject json) {
        super.onPostExecute(json);

        isTaskFinished = true;

        if (!json.isJsonObject() || !json.has("outcome"))
            callback.onFailure(new RuntimeException("Invalid Response from server!"));
        else {
            String outcome = json.get("outcome").getAsString();

            if (callback != null) {
                if (outcome.equals("success")) {
                    callback.onSuccess(json.get("result"));
                } else {
                    JsonElement elem = json.get("failure-description");

                    if (elem.isJsonPrimitive()) {
                        callback.onFailure(new RuntimeException(elem.getAsString()));
                    } else if (elem.isJsonObject())
                        callback.onFailure(new RuntimeException(elem.getAsJsonObject().get("domain-failure-description").getAsString()));
                }
            }
        }

    }

    @Override
    protected void onCancelled() {
        isTaskFinished = true;

        if (callback != null)
            callback.onFailure(this.exception);
    }

    public boolean isTaskFinished() {
        return isTaskFinished;
    }

    interface Reply {

        public void onSuccess(JsonElement json);

        public void onFailure(Exception e);
    }
}
