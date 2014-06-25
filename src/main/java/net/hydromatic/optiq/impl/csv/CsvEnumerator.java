/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package net.hydromatic.optiq.impl.csv;

import net.hydromatic.linq4j.Enumerator;

import au.com.bytecode.opencsv.CSVReader;

import org.apache.commons.lang3.time.FastDateFormat;

import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;


/** Enumerator that reads from a CSV file. */
class CsvEnumerator implements Enumerator<Object> {
  private final CSVReader reader;
  private final RowConverter rowConverter;
  private Object current;

  private static final FastDateFormat TIME_FORMAT_DATE;
  private static final FastDateFormat TIME_FORMAT_TIME;
  private static final FastDateFormat TIME_FORMAT_TIMESTAMP;

  static {

    TimeZone gmt = TimeZone.getTimeZone("GMT");
    TIME_FORMAT_DATE = FastDateFormat.getInstance("yyyy-MM-dd", gmt);
    TIME_FORMAT_TIME = FastDateFormat.getInstance("hh:mm:ss", gmt);
    TIME_FORMAT_TIMESTAMP = FastDateFormat.getInstance(
        "yyyy-MM-dd hh:mm:ss", gmt);
  }

  public CsvEnumerator(File file, CsvFieldType[] fieldTypes) {
    this(file, fieldTypes, identityList(fieldTypes.length));
  }

  public CsvEnumerator(File file, CsvFieldType[] fieldTypes, int[] fields) {
    this.rowConverter = fields.length == 1
        ? new SingleColumnRowConverter(fieldTypes[fields[0]], fields[0])
        : new ArrayRowConverter(fieldTypes, fields);
    try {
      this.reader = new CSVReader(new FileReader(file));
      this.reader.readNext(); // skip header row
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Object current() {
    return current;
  }

  public boolean moveNext() {
    try {
      final String[] strings = reader.readNext();
      if (strings == null) {
        current = null;
        reader.close();
        return false;
      }
      current = rowConverter.convertRow(strings);
      return true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void reset() {
    throw new UnsupportedOperationException();
  }

  public void close() {
    try {
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException("Error closing CSV reader", e);
    }
  }

  /** Returns an array of integers {0, ..., n - 1}. */
  static int[] identityList(int n) {
    int[] integers = new int[n];
    for (int i = 0; i < n; i++) {
      integers[i] = i;
    }
    return integers;
  }

  private abstract static class RowConverter {
    abstract Object convertRow(String[] rows);

    protected Object convert(CsvFieldType fieldType, String string) {
      if (fieldType == null) {
        return string;
      }
      switch (fieldType) {
      default:
      case STRING:
        return string;
      case BOOLEAN:
        if (string.length() == 0) {
          return null;
        }
        return Boolean.parseBoolean(string);
      case BYTE:
        if (string.length() == 0) {
          return null;
        }
        return Byte.parseByte(string);
      case SHORT:
        if (string.length() == 0) {
          return null;
        }
        return Short.parseShort(string);
      case INT:
        if (string.length() == 0) {
          return null;
        }
        return Integer.parseInt(string);
      case LONG:
        if (string.length() == 0) {
          return null;
        }
        return Long.parseLong(string);
      case FLOAT:
        if (string.length() == 0) {
          return null;
        }
        return Float.parseFloat(string);
      case DOUBLE:
        if (string.length() == 0) {
          return null;
        }
        return Double.parseDouble(string);
      case DATE:
        if (string.length() == 0) {
          return null;
        }

        try {
          Date date = TIME_FORMAT_DATE.parse(string);
          return new java.sql.Date(date.getTime());
        } catch (ParseException e) {
          return null;
        }

      case TIME:
        if (string.length() == 0) {
          return null;
        }

        try {
          Date date = TIME_FORMAT_TIME.parse(string);
          return new java.sql.Time(date.getTime());
        } catch (ParseException e) {
          return null;
        }

      case TIMESTAMP:
        if (string.length() == 0) {
          return null;
        }
        try {
          Date date = TIME_FORMAT_TIMESTAMP.parse(string);
          return new java.sql.Timestamp(date.getTime());
        } catch (ParseException e) {
          return null;
        }
      }
    }
  }

  private static class ArrayRowConverter extends RowConverter {

    private final CsvFieldType[] fieldTypes;
    private final int[] fields;

    private ArrayRowConverter(CsvFieldType[] fieldTypes, int[] fields) {
      this.fieldTypes = fieldTypes;
      this.fields = fields;
    }

    public Object convertRow(String[] strings) {
      final Object[] objects = new Object[fields.length];
      for (int i = 0; i < fields.length; i++) {
        int field = fields[i];
        objects[i] = convert(fieldTypes[field], strings[field]);
      }
      return objects;
    }
  }

  private static class SingleColumnRowConverter extends RowConverter {

    private final CsvFieldType fieldType;
    private final int fieldIndex;

    private SingleColumnRowConverter(CsvFieldType fieldType, int fieldIndex) {
      this.fieldType = fieldType;
      this.fieldIndex = fieldIndex;
    }

    public Object convertRow(String[] strings) {
      return convert(fieldType, strings[fieldIndex]);
    }
  }

}

// End CsvEnumerator.java
