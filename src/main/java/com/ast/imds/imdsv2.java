package com.ast.imds;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Fetches the temporary IAM credentials attached to an EC2 instance. */
public final class imdsv2 {
    private static final String IMDS = "http://169.254.169.254";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private imdsv2() {
    }

    /** Returns the credential document from IMDSv2 as JSON. */
    public static String fetchCredentials() throws IOException, InterruptedException {
        String token = send(HttpRequest.newBuilder(URI.create(IMDS + "/latest/api/token"))
                .timeout(TIMEOUT)
                .header("X-aws-ec2-metadata-token-ttl-seconds", "60")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build());

        String roleName = send(metadataRequest(
                "/latest/meta-data/iam/security-credentials/", token)).trim();
        if (roleName.isEmpty()) {
            throw new IOException("No IAM role is attached to this instance");
        }

        return send(metadataRequest(
                "/latest/meta-data/iam/security-credentials/" + roleName, token));
    }

    private static HttpRequest metadataRequest(String path, String token) {
        return HttpRequest.newBuilder(URI.create(IMDS + path))
                .timeout(TIMEOUT)
                .header("X-aws-ec2-metadata-token", token)
                .GET()
                .build();
    }

    private static String send(HttpRequest request)
            throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP.send(
                request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("IMDS request failed with HTTP " + response.statusCode());
        }
        return response.body();
    }

    public static void main(String[] args) throws Exception {
        String credentialJson = fetchCredentials();
        System.out.println("IMDSv2 credentials retrieved successfully:");
        System.out.println(credentialJson);
    }
}

