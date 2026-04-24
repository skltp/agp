/*
 * Copyright © 2014-2026 Inera.
 * Copyright owner URL: https://www.inera.se/
 * SKLTP overview page: https://inera.atlassian.net/wiki/spaces/SKLTP/overview
 * This library is free software under the GNU Lesser General Public License v3.0.
 * Please refer to the full license files at the project root.
 */
package se.skltp.aggregatingservices.utils;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadSafeSimpleDateFormat {
  private final ThreadLocal<SoftReference<DateFormat>> tl = new ThreadLocal();
  private String pattern;

  private DateFormat getDateFormat() {
    SoftReference<DateFormat> ref = this.tl.get();
    if (ref != null) {
      DateFormat result = ref.get();
      if (result != null) {
        return result;
      }
    }

    DateFormat result = new SimpleDateFormat(this.pattern);
    ref = new SoftReference(result);
    this.tl.set(ref);
    return result;
  }

  public ThreadSafeSimpleDateFormat(String pattern) {
    this.pattern = pattern;
  }

  public String format(Date date) {
    return this.getDateFormat().format(date);
  }

  public Date parse(String date) throws ParseException {
    return this.getDateFormat().parse(date);
  }
}