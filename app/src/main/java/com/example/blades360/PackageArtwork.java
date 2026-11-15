package com.example.blades360;

import java.util.Locale;
import java.util.regex.*;

/** Package labels are separate from the shared, unmodified parent cover. */
final class PackageArtwork {
  static String titleId(String name) {
    Matcher m=Pattern.compile("(?i)(?<![0-9a-f])010[0-9a-f]{13}(?![0-9a-f])").matcher(name);
    return m.find()?m.group().toUpperCase(Locale.ROOT):"";
  }
  static String parentId(String id) {
    if(id.isEmpty())return "";
    long n=Long.parseUnsignedLong(id,16), suffix=n&0xfffL;
    // Base applications end in 000. Updates end in 800; add-ons use 001..7ff.
    if(suffix==0x800L)return String.format(Locale.ROOT,"%016X",n-0x800L);
    if(suffix>0&&suffix<0x800L)return String.format(Locale.ROOT,"%016X",(n&~0xfffL)-0x1000L);
    return "";
  }
  static String badge(String system,String name) {
    if(system.equals("Nintendo Switch")) {
      String id=titleId(name);
      if(!id.isEmpty()) {
        long suffix=Long.parseUnsignedLong(id,16)&0xfffL;
        if(suffix==0x800L)return "UPDATE";
        if(suffix>0&&suffix<0x800L)return "DLC";
        if(suffix==0)return "";
      }
    }
    String words=name.toLowerCase(Locale.ROOT).replace('_',' ');
    if(Pattern.compile("\\bdlc\\b").matcher(words).find())return "DLC";
    if(Pattern.compile("\\b(update|upd|patch)\\b").matcher(words).find())return "UPDATE";
    if(system.equals("Nintendo Switch")&&words.matches(".*\\s+v?[0-9]+\\.[0-9]+(?:\\.[0-9]+)?\\.(nsp|xci)$"))return "UPDATE";
    return "";
  }

  /** Conservative title key for grouping ID-less releases, never a fuzzy match. */
  static String baseTitle(String name) {
    String s=name.replaceFirst("(?i)\\.[a-z0-9]{1,5}$","").replace('_',' ');
    s=s.replaceAll("\\[[^]]*\\]", " ").replaceAll("\\([^)]*\\)"," ");
    s=s.replaceAll("(?i)\\b(?:nsw2u|switch-xci)\\.(?:com|xyz)\\b", " ");
    s=s.replaceFirst("(?i)^v[- ]+fantasy life i", "fantasy life i");
    s=s.replaceFirst("(?i)\\s+(?:edition upgrade digital deluxe|extra mount unicorn|pre order bonus)\\s+dlc$", "");
    s=s.replaceFirst("(?i)\\s+dlc\\b.*$", "");
    s=s.replaceFirst("(?i)\\s+(?:update|upd|patch)\\b.*$", "");
    s=s.replaceFirst("(?i)\\s+v?[0-9]+\\.[0-9]+(?:\\.[0-9]+)?(?:\\s+v[0-9]+)?$", "");
    s=s.trim().replaceFirst("(?i)\\s+v[0-9]+$", "");
    s=java.text.Normalizer.normalize(s,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return s.toLowerCase(Locale.ROOT).replace("'", "").replace("\u2019", "").replaceAll("\\bplus\\b", " ").replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
  }
}
