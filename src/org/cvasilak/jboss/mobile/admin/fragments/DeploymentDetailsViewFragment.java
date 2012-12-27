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

package org.cvasilak.jboss.mobile.admin.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockFragment;
import com.google.gson.JsonElement;
import org.cvasilak.jboss.mobile.admin.JBossAdminApplication;
import org.cvasilak.jboss.mobile.admin.R;
import org.cvasilak.jboss.mobile.admin.net.Callback;

public class DeploymentDetailsViewFragment extends SherlockFragment {

    private static final String TAG = DeploymentDetailsViewFragment.class.getSimpleName();

    private JBossAdminApplication application;

    private ProgressDialog progress;

    private EditText key;
    private EditText name;
    private EditText runtimeName;

    public static DeploymentDetailsViewFragment newInstance(String BYTES_VALUE, String name) {
        DeploymentDetailsViewFragment f = new DeploymentDetailsViewFragment();

        Bundle args = new Bundle();
        args.putString("BYTES_VALUE", BYTES_VALUE);
        args.putString("name", name);

        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "@onCreate()");

        application = (JBossAdminApplication) getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "@onCreateView()");

        View view = inflater.inflate(R.layout.deploymentdetail_form, container, false);

        key = (EditText) view.findViewById(R.id.key);
        key.setText(getArguments().getString("BYTES_VALUE"));

        name = (EditText) view.findViewById(R.id.name);
        name.setText(getArguments().getString("name"));

        runtimeName = (EditText) view.findViewById(R.id.runtimeName);
        runtimeName.setText(getArguments().getString("name"));

        Button save = (Button) view.findViewById(R.id.done);

        save.setOnClickListener(onSave);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private View.OnClickListener onSave = new View.OnClickListener() {
        public void onClick(View v) {

            if (name.getText().toString().equals("")
                    || runtimeName.getText().toString().equals("")) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        getActivity());

                alertDialog
                        .setTitle(R.string.dialog_error_title)
                        .setMessage(R.string.missing_params)
                        .setPositiveButton(R.string.dialog_button_Bummer, null)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();

                return;
            }

            progress = ProgressDialog.show(getSherlockActivity(), "", getString(R.string.enablingDeployment));

            application.getOperationsManager().addDeploymentContent(key.getText().toString(), name.getText().toString(), runtimeName.getText().toString(), new Callback() {
                @Override
                public void onSuccess(JsonElement reply) {
                    progress.dismiss();

                    getFragmentManager().popBackStack();
                }

                @Override
                public void onFailure(Exception e) {
                    progress.dismiss();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                            getActivity());

                    alertDialog
                            .setTitle(R.string.dialog_error_title)
                            .setMessage(e.getMessage())
                            .setPositiveButton(R.string.dialog_button_Bummer, null)
                            .setCancelable(false)
                            .setIcon(android.R.drawable.ic_dialog_alert).show();
                }
            });
        }
    };
}
