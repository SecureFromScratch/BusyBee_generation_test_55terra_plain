package com.securefromscratch.busybee.preview;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
class TaskPreviewMetadataFetcher {

    GeneratedTaskPreview fetch(String url) {
        URI uri = validRemoteUri(url);
        rejectNonPublicHosts(uri.getHost());
        try {
            Document document = Jsoup.connect(uri.toString())
                    .timeout(5_000)
                    .maxBodySize(1_000_000)
                    .followRedirects(false)
                    .get();
            String title = nonBlank(document.title(), uri.toString());
            String description = metadata(document, "meta[name=description]", "meta[property=og:description]");
            String image = metadata(document, "meta[property=og:image]", "meta[name=twitter:image]");
            return new GeneratedTaskPreview(uri.toString(), title, description, image.isBlank() ? null : image);
        } catch (java.io.IOException exception) {
            throw new InvalidPreviewRequestException("Preview metadata could not be retrieved.");
        }
    }

    private URI validRemoteUri(String url) {
        try {
            URI uri = new URI(url);
            if (("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null) {
                return uri;
            }
        } catch (URISyntaxException exception) {
            throw new InvalidPreviewRequestException("URL is invalid.");
        }

        throw new InvalidPreviewRequestException("URL is invalid.");
    }

    private void rejectNonPublicHosts(String host) {
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new InvalidPreviewRequestException("URL host is not allowed.");
                }
            }
        } catch (UnknownHostException exception) {
            throw new InvalidPreviewRequestException("URL host could not be resolved.");
        }
    }

    private String metadata(Document document, String primarySelector, String fallbackSelector) {
        Element element = document.selectFirst(primarySelector);
        if (element == null) {
            element = document.selectFirst(fallbackSelector);
        }
        return element == null ? "" : element.attr("content").trim();
    }

    private String nonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
