package io.jenkins.plugins.orka.client;

import java.util.Collections;
import java.util.List;

public class ImageResponse extends ResponseBase {
    private List<String> images;

    public ImageResponse(List<String> images, String message, OrkaError[] errors) {
        super(message, errors);
        this.images = images;
    }

    public List<String> getImages() {
        return this.images != null ? Collections.unmodifiableList(this.images) : Collections.emptyList();
    }

}