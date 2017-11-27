package ca.benow.transmission.model;

import org.json.JSONObject;

public class AddedTorrentInfo extends JSONAccessor {

  public AddedTorrentInfo(JSONObject jsonObject) {
    super(jsonObject);
  }

  public int getId() {
    return obj.getInt("id");
  }

  public String getName() {
    return obj.getString("name");
  }

  public String getHashString() {
    return obj.getString("hashString");
  }

  @Override
  public String toString() {
	return obj.toString(2);
  }
}
