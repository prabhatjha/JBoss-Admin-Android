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

package org.cvasilak.jboss.mobile.admin;

import android.app.Application;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cvasilak.jboss.mobile.admin.model.Server;
import org.cvasilak.jboss.mobile.admin.model.ServersManager;
import org.cvasilak.jboss.mobile.admin.net.JBossOperationsManager;

public class JBossAdminApplication extends Application {

    private static final String TAG = "JBossAdminApplication";

    private ServersManager serversManager;

    private JBossOperationsManager operationsManager;

    private Gson gjson;

    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "@onCreate()");

        gjson = new GsonBuilder().setPrettyPrinting().create();

        serversManager = new ServersManager(getApplicationContext());
        try {
            serversManager.load();
        } catch (Exception e) {
            Log.d(TAG, "exception on load", e);
            // an error occured during loading of "list-of-servers" file, ignore it
        }
    }

    public ServersManager getServersManager() {
        return serversManager;
    }

    public void setCurrentActiveServer(Server currentActiveServer) {
        this.operationsManager = new JBossOperationsManager(getApplicationContext(), currentActiveServer);
    }

    public JBossOperationsManager getOperationsManager() {
        return operationsManager;
    }

    public Gson getJSONParser() {
        return gjson;
    }
}
