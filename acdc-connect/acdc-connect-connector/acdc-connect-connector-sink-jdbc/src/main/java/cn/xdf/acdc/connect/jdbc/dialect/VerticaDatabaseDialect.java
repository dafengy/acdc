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

package cn.xdf.acdc.connect.jdbc.dialect;

import cn.xdf.acdc.connect.core.sink.metadata.SinkRecordField;
import cn.xdf.acdc.connect.jdbc.util.IdentifierRules;
import cn.xdf.acdc.connect.jdbc.util.TableId;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Time;
import org.apache.kafka.connect.data.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link DatabaseDialect} for Vertica.
 */
public class VerticaDatabaseDialect extends GenericDatabaseDialect {

    /**
     * Create a new dialect instance with the given connector configuration.
     *
     * @param config the connector configuration; may not be null
     */
    public VerticaDatabaseDialect(final AbstractConfig config) {
        super(config, new IdentifierRules(".", "\"", "\""));
    }

    @Override
    protected String getSqlType(final SinkRecordField field) {
        if (field.schemaName() != null) {
            switch (field.schemaName()) {
                case Decimal.LOGICAL_NAME:
                    return "DECIMAL(18," + field.schemaParameters().get(Decimal.SCALE_FIELD) + ")";
                case Date.LOGICAL_NAME:
                    return "DATE";
                case Time.LOGICAL_NAME:
                    return "TIME";
                case Timestamp.LOGICAL_NAME:
                    return "TIMESTAMP";
                default:
                    // fall through to non-logical types
                    break;
            }
        }
        switch (field.schemaType()) {
            case INT8:
                return "INT";
            case INT16:
                return "INT";
            case INT32:
                return "INT";
            case INT64:
                return "INT";
            case FLOAT32:
                return "FLOAT";
            case FLOAT64:
                return "FLOAT";
            case BOOLEAN:
                return "BOOLEAN";
            case STRING:
                return "VARCHAR(1024)";
            case BYTES:
                return "VARBINARY(1024)";
            default:
                return super.getSqlType(field);
        }
    }

    @Override
    public List<String> buildAlterTable(
            final TableId table,
            final Collection<SinkRecordField> fields
    ) {
        final List<String> queries = new ArrayList<>(fields.size());
        for (SinkRecordField field : fields) {
            queries.addAll(super.buildAlterTable(table, Collections.singleton(field)));
        }
        return queries;
    }

    /**
     * The provider for {@link VerticaDatabaseDialect}.
     */
    public static class Provider extends DatabaseDialectProvider.SubprotocolBasedProvider {

        public Provider() {
            super(VerticaDatabaseDialect.class.getSimpleName(), "vertica");
        }

        @Override
        public DatabaseDialect create(final AbstractConfig config) {
            return new VerticaDatabaseDialect(config);
        }
    }
}
