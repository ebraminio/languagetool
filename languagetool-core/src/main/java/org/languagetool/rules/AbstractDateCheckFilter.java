/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules;

import org.languagetool.rules.patterns.RuleFilter;

import java.util.Calendar;
import java.util.Map;

/**
 * Accepts rule matches if a date doesn't match the accompanying weekday, e.g. if {@code Monday, 8 November 2003}
 * isn't actually a Monday. Replaces {@code \realDay} with the real day of the date in the rule's message.
 * @since 2.7
 */
public abstract class AbstractDateCheckFilter implements RuleFilter {

  /**
   * Implement so that Sunday returns {@code 1}, Monday {@code 2} etc.
   * @param localizedWeekDayString a week day name or abbreviation thereof
   */
  protected abstract int getDayOfWeek(String localizedWeekDayString);

  /**
   * Get the localized name of the day of week for the given date.
   */
  protected abstract String getDayOfWeek(Calendar date);

  /**
   * Implement so that January returns {@code 1}, February {@code 2} etc.
   * @param localizedMonth name of a month or abbreviation thereof
   */
  protected abstract int getMonth(String localizedMonth);
  
  protected abstract Calendar getCalendar();

  /**
   * @param args a map with values for {@code year}, {@code month}, {@code day} (day of month), {@code weekDay}  
   */
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String,String> args) {
    int dayOfWeekFromString = getDayOfWeek(getRequired("weekDay", args));
    Calendar dateFromDate = getDate(args);
    int dayOfWeekFromDate;
    try {
      dayOfWeekFromDate = dateFromDate.get(Calendar.DAY_OF_WEEK);
    } catch (IllegalArgumentException ignore) {
      // happens with 'dates' like '32.8.2014' - those should be caught by a different rule
      return null;
    }
    if (dayOfWeekFromString != dayOfWeekFromDate) {
      String realDayName = getDayOfWeek(dateFromDate);
      String message = match.getMessage().replace("\\realDay", realDayName);
      RuleMatch newMatch = new RuleMatch(match.getRule(), match.getFromPos(), match.getToPos(), message, match.getShortMessage());
      return newMatch;
    } else {
      return null;
    }
  }

  protected String getRequired(String key, Map<String, String> map) {
    String result = map.get(key);
    if (result == null) {
      throw new IllegalArgumentException("Missing key '" + key + "'");
    }
    return result;
  }

  private Calendar getDate(Map<String, String> args) {
    int year = Integer.parseInt(getRequired("year", args));
    int month = getMonthFromArguments(args);
    int dayOfMonth = Integer.parseInt(getRequired("day", args));
    Calendar calendar = getCalendar();
    calendar.setLenient(false);  // be strict about validity of dates
    //noinspection MagicConstant
    calendar.set(year, month, dayOfMonth, 0, 0, 0);
    return calendar;
  }

  private int getMonthFromArguments(Map<String, String> args) {
    String monthStr = getRequired("month", args);
    int month;
    if (monthStr.matches("\\d+")) {
      month = Integer.parseInt(monthStr);
    } else {
      month = getMonth(monthStr);
    }
    return month - 1;
  }

}
