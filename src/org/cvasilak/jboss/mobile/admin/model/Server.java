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

import java.io.Serializable;

public class Server implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String hostname;
    private int port;
    private boolean isSSLSecured;
    private String username;
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSSLSecured() {
        return isSSLSecured;
    }

    public void setSSLSecured(boolean isSSLSecured) {
        this.isSSLSecured = isSSLSecured;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostPort() {
        StringBuilder builder = new StringBuilder();

        builder.append(isSSLSecured ? "https://" : "http://");

        if (username != null && password != null)
            builder.append(username).append(":").append(password).append("@");

        builder.append(hostname).append(":").append(port);

        return builder.toString();
    }
}
