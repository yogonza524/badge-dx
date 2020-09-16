package com.badge.dx;

import java.io.IOException;
import java.util.Objects;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class CamoErasertTest {

  @Test
  @Disabled
  public void eraseCacheTest() throws IOException {
    RestTemplate restTemplate = new RestTemplate();
    CloseableHttpClient client = HttpClients.createDefault();

    String repo = "yogonza524/roman-code";

    Objects.requireNonNull(repo, "Repository name is required. Format user/repoName");
    Document doc =
        Jsoup.connect("https://github.com/" + repo)
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
                Process process =
                    new ProcessBuilder("curl", "-X", "PURGE", l.attr("abs:src")).start();

                //                try {
                //                    final BufferedReader reader = new BufferedReader(
                //                            new InputStreamReader(process.getInputStream()));
                //                    String line = null;
                //                    while ((line = reader.readLine()) != null) {
                //                        System.out.println(line);
                //                    }
                //                    reader.close();
                //                } catch (final Exception e) {
                //                    e.printStackTrace();
                //                }

              } catch (IOException e) {
                e.printStackTrace();
              }
            });
  }
}
