package org.teameos.wallpapers;

public class NavDrawerItem {

    private String albumId, albumTitle;

    public NavDrawerItem() {
    }

    public NavDrawerItem(String albumId, String albumTitle) {
        this.albumId = albumId;
        this.albumTitle = albumTitle;
    }

    public String getTitle() {
        return this.albumTitle;
    }

    public void setTitle(String title) {
        this.albumTitle = title;
    }
}
