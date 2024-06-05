package com.mohistmc.banner.plugins;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.config.BannerConfig;
import com.mohistmc.tools.IOUtil;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

public class BannerProxySelector extends ProxySelector {

    private final ProxySelector defaultSelector;

    public BannerProxySelector(ProxySelector defaultSelector) {
        this.defaultSelector = defaultSelector;
    }

    @Override
    public List<Proxy> select(URI uri) {
        if (BannerConfig.networkmanager_debug) {
            BannerMCStart.LOGGER.error(uri.toString());
        }

        String uriString = uri.toString();
        String defaultMsg = "[NetworkManager] Network protection and blocked by network rules!";
        boolean intercept = false;
        if (BannerConfig.networkmanager_intercept != null) {
            for (String config_uri : BannerConfig.networkmanager_intercept) {
                if (uriString.contains(config_uri)) {
                    intercept = true;
                    break;
                }
            }
        }
        if (intercept) {
            try {
                IOUtil.throwException(new IOException(defaultMsg));
            } catch (Throwable ignored) {
            }
        } else {
            return this.defaultSelector.select(uri);
        }
        return null;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        this.defaultSelector.connectFailed(uri, sa, ioe);
    }

    public ProxySelector getDefaultSelector() {
        return this.defaultSelector;
    }
}
