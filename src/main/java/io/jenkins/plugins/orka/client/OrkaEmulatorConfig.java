package io.jenkins.plugins.orka.client;

import java.util.Objects;

public class OrkaEmulatorConfig {
    private String name;

    private String platform;

    private String imageType;

    private String deviceProfile;

    public OrkaEmulatorConfig(String name, String platform, String imageType, String deviceProfile) {
        this.name = name;
        this.platform = platform;
        this.imageType = imageType;
        this.deviceProfile = deviceProfile;
    }

    public String getName() {
        return this.name;
    }

    public String getPlatform() {
        return this.platform;
    }

    public String getImageType() {
        return this.imageType;
    }

    public String getDeviceProfile() {
        return this.deviceProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrkaEmulatorConfig other = (OrkaEmulatorConfig) o;
        return Objects.equals(this.name, other.name) && Objects.equals(this.platform, other.platform)
                && Objects.equals(this.imageType, other.imageType)
                && Objects.equals(this.deviceProfile, other.deviceProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.platform, this.imageType, this.deviceProfile);
    }
}
