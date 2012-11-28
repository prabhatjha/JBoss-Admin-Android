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

package org.cvasilak.jboss.mobile.admin.model;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ServersManager {

    private static final String TAG = "ServersManager";

    private List<Server> servers;
    private Context context;

    private Gson gjson;

    public ServersManager(Context context) {
        this.context = context;
        this.gjson = ((JBossAdminApplication) context.getApplicationContext()).getJSONParser();
    }

    public void load() throws Exception {
        servers = new ArrayList<Server>();

        // if file does not exist start from scratch
        if (!context.getFileStreamPath("servers.archive").exists())
            return;

        Log.d(TAG, "initializing servers list from servers.archive file");

        FileInputStream fin = context.openFileInput("servers.archive");
        BufferedReader reader = new BufferedReader(new InputStreamReader(fin));

        StringBuilder json = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        Type collectionType = new TypeToken<List<Server>>() {
        }.getType();

        servers = gjson.fromJson(json.toString(), collectionType);
    }

    public void save() throws Exception {
        // output json to file
        FileOutputStream fout = context.openFileOutput("servers.archive",
                Context.MODE_PRIVATE);

        fout.write(gjson.toJson(servers).getBytes());
        fout.close();
    }

    public void addServer(Server server) {
        servers.add(server);
    }

    public void removeServerAtIndex(int index) {
        servers.remove(index);
    }

    public Server serverAtIndex(int index) {
        return servers.get(index);
    }

    public List<Server> getServers() {
        return servers;
    }
}
