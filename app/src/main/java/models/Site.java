package models;

/**
 * Created by valentin on 06/03/2018.
 */

public class Site {

    private int id;
    private String site;
    private String lat;
    private String lng;
    private String created_at;
    private String updated_at;

    public Site(int id, String site, String lat, String lng, String created_at, String updated_at) {
        this.id = id;
        this.site = site;
        this.lat = lat;
        this.lng = lng;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
