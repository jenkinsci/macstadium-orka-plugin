package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class ImageResponse extends ResponseBase {
    @SerializedName("items")
    private List<Image> images;

    public ImageResponse(List<Image> images, String message) {
        super(message);
        this.images = images;
    }

    public List<Image> getImages() {
        return this.images != null ? Collections.unmodifiableList(this.images) : Collections.emptyList();
    }

}
