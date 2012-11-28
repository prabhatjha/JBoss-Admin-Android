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

package org.cvasilak.jboss.mobile.admin.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TwoLineListItem;
import org.cvasilak.jboss.mobile.admin.model.Metric;

import java.util.ArrayList;
import java.util.List;

public class MetricsAdapter extends ArrayAdapter<Metric> {

    public MetricsAdapter(Context context) {
        this(context, new ArrayList<Metric>());
    }

    public MetricsAdapter(Context context, List<Metric> metrics) {
        super(context, android.R.layout.simple_list_item_2, metrics);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TwoLineListItem row;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
        } else {
            row = (TwoLineListItem) convertView;
        }

        Metric metric = getItem(position);

        row.getText1().setText(metric.getName());

        if (metric.getValue() != null)
            row.getText2().setText(metric.getValue());
        else
            row.getText2().setText("");

        return (row);
    }

    @Override
    public boolean isEnabled(int position) {
        // metrics by default are not selectable
        return false;
    }
}