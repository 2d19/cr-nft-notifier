package com.bla.services;

import com.bla.models.CollectionOfInterest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageService {
  /**
   * Transform collections of interest to a readable (telegram) format. See {@link TelegramService}
   *
   * @param collectionsOfInterest collections to be transformed to readable html.
   * @return text representation of collections of interest.
   */
  public String generateMessage(final List<CollectionOfInterest> collectionsOfInterest) {

    final var stringBuilder = new StringBuilder();
    stringBuilder.append("<b>Collections of interest</b>: \n");

    for (var i = 0; i < collectionsOfInterest.size(); i++) {
      stringBuilder
          .append(i + 1)
          .append(". ")
          .append(collectionsOfInterest.get(i).getName())
          .append(", <i>floor</i> = ")
          .append(collectionsOfInterest.get(i).getFloorPrice())
          .append(", <i>change</i> = ")
          .append(collectionsOfInterest.get(i).getOneDayChange())
          .append(" \n");
    }
    stringBuilder.append("\nData gathered from <a href=\"https://opensea.io/\">opensea</a>");
    return URLEncoder.encode(stringBuilder.toString(), StandardCharsets.UTF_8);
  }
}
