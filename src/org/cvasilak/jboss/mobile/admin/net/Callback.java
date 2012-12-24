package org.cvasilak.jboss.mobile.admin.net;

import com.google.gson.JsonElement;

public interface Callback {

    public void onSuccess(JsonElement reply);

    public void onFailure(Exception e);
}




