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

package org.cvasilak.jboss.mobile.admin.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.model.Server;
import org.cvasilak.jboss.mobile.admin.model.ServersManager;

public class ServerDetailActivity extends Activity {

    private static final String TAG = ServerDetailActivity.class.getSimpleName();

    private EditText name;
    private EditText hostname;
    private EditText port;
    private CheckBox isSSLSecured;
    private EditText username;
    private EditText password;

    private ServersManager serversManager;
    private Server server;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        setContentView(R.layout.serverdetail_form);

        serversManager = ((JBossAdminApplication) getApplication()).getServersManager();

        name = (EditText) findViewById(R.id.name);
        hostname = (EditText) findViewById(R.id.hostname);
        port = (EditText) findViewById(R.id.port);
        isSSLSecured = (CheckBox) findViewById(R.id.isSSLSecured);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        Button save = (Button) findViewById(R.id.save);

        save.setOnClickListener(onSave);

        int serverIndex = getIntent().getIntExtra(ServersViewActivity.ID_EXTRA, -1);

        if (serverIndex != -1) {  // we are asked to edit an existing server
            server = serversManager.serverAtIndex(serverIndex);

            name.setText(server.getName());
            hostname.setText(server.getHostname());
            port.setText(String.valueOf(server.getPort()));
            isSSLSecured.setChecked(server.isSSLSecured());
            username.setText(server.getUsername());
            password.setText(server.getPassword());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private View.OnClickListener onSave = new View.OnClickListener() {
        public void onClick(View v) {

            if (name.getText().toString().equals("")
                    || hostname.getText().toString().equals("")
                    || port.getText().toString().equals("")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        ServerDetailActivity.this);

                alertDialog
                        .setTitle(R.string.dialog_error_title)
                        .setMessage(R.string.not_enough_params)
                        .setPositiveButton(R.string.dialog_button_Bummer, null)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();

                return;
            }

            if (server == null) {
                server = new Server();

                serversManager.addServer(server);
            }

            server.setName(name.getText().toString());
            server.setHostname(hostname.getText().toString());
            server.setPort(Integer.parseInt(port.getText().toString()));
            server.setSSLSecured(isSSLSecured.isChecked());
            server.setUsername(username.getText().toString());
            server.setPassword(password.getText().toString());

            try {
                serversManager.save();
            } catch (Exception e) { // error occured during save
                Log.d(TAG, "exception on save", e);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        ServerDetailActivity.this);

                alertDialog
                        .setTitle(R.string.dialog_error_title)
                        .setMessage(R.string.error_on_save)
                        .setPositiveButton(R.string.dialog_button_Bummer, null)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }

            finish();
        }
    };
}
