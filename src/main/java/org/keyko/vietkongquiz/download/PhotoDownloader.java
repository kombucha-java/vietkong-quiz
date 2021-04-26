package org.keyko.vietkongquiz.download;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Component
public class PhotoDownloader {
    @Value("${persist.image.dir:/tmp/quiz}")
    private String persistImageDir;

    public File download(URI uri, String fileName) {

        try (CloseableHttpClient httpclient = HttpClients
                .custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build()
        ) {
            HttpGet get = new HttpGet(uri);
            String pathToFile = persistImageDir +
                    (persistImageDir.endsWith("/") ? "" : "/") +
                    fileName +
                    ".jpg";
            File newFile = new File(pathToFile);
            FileUtils.touch(newFile);

            return httpclient.execute(get, new FileDownloadResponseHandler(newFile));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static class FileDownloadResponseHandler implements ResponseHandler<File> {
        private final File target;

        public FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(HttpResponse response) throws IOException {
            try (InputStream source = response.getEntity().getContent()) {
                FileUtils.copyInputStreamToFile(source, this.target);
            }

            return this.target;
        }

    }

}
