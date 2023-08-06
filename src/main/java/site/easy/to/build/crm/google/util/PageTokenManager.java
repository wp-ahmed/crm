package site.easy.to.build.crm.google.util;

import java.util.HashMap;
import java.util.Map;

public class PageTokenManager {
    private final Map<Integer, String> pageTokens;

    public PageTokenManager() {
        pageTokens = new HashMap<>();
    }

    public String getPageToken(Integer pageNumber) {
        return pageTokens.get(pageNumber);
    }

    public void setPageToken(Integer pageNumber, String pageToken) {
        pageTokens.put(pageNumber, pageToken);
    }

    public Integer findClosestPageNumber(Integer pageNumber) {
        if (pageTokens.isEmpty()) {
            return null;
        }
        Integer closestPageNumber = null;
        int minDifference = Integer.MAX_VALUE;

        for (Integer key : pageTokens.keySet()) {
            int difference = Math.abs(key - pageNumber);
            if (difference < minDifference) {
                minDifference = difference;
                closestPageNumber = key;
            }
        }

        return closestPageNumber;
    }
}