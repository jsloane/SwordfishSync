package ca.benow.transmission.model;

import org.json.JSONObject;


public class SessionStatus extends JSONAccessor {

  public class SessionStats extends JSONAccessor {

    public SessionStats(JSONObject jsonObject) {
      super(jsonObject);
    }

    public int getUploadedBytes() {
      return obj.getInt("uploadedBytes");
    }

    public int getDownloadedBytes() {
      return obj.getInt("downloadedBytes");
    }

    public int getFilesAdded() {
      return obj.getInt("filesAdded");
    }

    public int getSessionCount() {
      return obj.getInt("sessionCount");
    }

    public int getSecondsActive() {
      return obj.getInt("secondsActive");
    }

  }

  public SessionStatus(JSONObject jsonObject) {
    super(jsonObject);
  }

  public int getActiveTorrentCount() {
    return obj.getInt("activeTorrentCount");
  }

  public int getDownloadSpeed() {
    return obj.getInt("downloadSpeed");
  }

  public int getPausedTorrentCount() {
    return obj.getInt("pausedTorrentCount");
  }

  public int getTorrentCount() {
    return obj.getInt("torrentCount");
  }

  public int getUploadSpeed() {
    return obj.getInt("uploadSpeed");
  }

  public SessionStats getCumulativeStats() {
    return new SessionStats(obj.getJSONObject("cumulative-stats"));
  }

  public SessionStats getCurrentStats() {
    return new SessionStats(obj.getJSONObject("current-stats"));
  }
}
