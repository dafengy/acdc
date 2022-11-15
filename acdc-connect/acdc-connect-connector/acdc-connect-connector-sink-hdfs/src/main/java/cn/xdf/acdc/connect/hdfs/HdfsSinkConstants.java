/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.xdf.acdc.connect.hdfs;

import java.util.regex.Pattern;

public class HdfsSinkConstants {

    public static final String COMMMITTED_FILENAME_SEPARATOR = "+";

    // groups: topic, partition, start offset, end offset, extension
    // Also see legalChars in Topic.scala
    public static final Pattern COMMITTED_FILENAME_PATTERN = Pattern.compile(
        "([a-zA-Z0-9\\._\\-]+)\\+(\\d+)\\+(\\d+)\\+(\\d+)(.\\w+)?"
    );

    public static final int PATTERN_TABLE_GROUP = 1;

    public static final int PATTERN_PARTITION_GROUP = 2;

    public static final int PATTERN_START_OFFSET_GROUP = 3;

    public static final int PATTERN_END_OFFSET_GROUP = 4;

    // +tmp is a invalid topic name, naming the tmp directory this way to avoid conflicts.
    public static final String TEMPFILE_DIRECTORY = "+tmp";

    public static final String WAL_LOG_DIRECTORY = "+wal_log";

    public static final String WAL_LOG_FILE_NAME = "log";

    public static final String DATA_DIRECTORY = "data";

    public static final String DB_SEPARATOR_REG = "\\.";

    public static final String DB_SEPARATOR = ".";

    public static final String UTF8_CHARACTER = "UTF-8";
}
