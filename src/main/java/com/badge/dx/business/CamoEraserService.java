package com.badge.dx.business;

import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import java.io.IOException;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class CamoEraserService {
  private final Escaper scaper = UrlEscapers.urlFragmentEscaper();

  public void eraseCache(String repo) throws IOException {
    Objects.requireNonNull(repo, "Repository name is required. Format user/repoName");

    Document doc =
        Jsoup.connect("https://github.com/" + scaper.escape(repo))
            .userAgent(
                "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("http://www.google.com")
            .get();

    Elements links = doc.select("img[src]");
    links
        .stream()
        .filter(l -> l.attr("abs:src").startsWith("https://camo"))
        .forEach(
            l -> {
              try {
                new ProcessBuilder("curl", "-X", "PURGE", l.attr("abs:src")).start();
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
  }
}
