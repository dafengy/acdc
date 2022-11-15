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

package cn.xdf.acdc.connect.hdfs.format.avro;

import cn.xdf.acdc.connect.hdfs.HdfsWriterCoordinator;
import cn.xdf.acdc.connect.hdfs.hive.HiveTestBase;
import cn.xdf.acdc.connect.hdfs.hive.HiveTestUtils;
import cn.xdf.acdc.connect.hdfs.hive.HiveUtil;
import cn.xdf.acdc.connect.hdfs.initialize.StoreConfig;
import cn.xdf.acdc.connect.hdfs.partitioner.Partitioner;
import cn.xdf.acdc.connect.hdfs.storage.HdfsStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.ql.metadata.Table;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTaskContext;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AvroHiveUtilTest extends HiveTestBase {

    private HiveUtil hive;

    private Map<String, String> localProps = new HashMap<>();

    @Override
    protected Map<String, String> createProps() {
        Map<String, String> props = super.createProps();
        props.putAll(localProps);
        return props;
    }

    /**
     * Set up.
     * @throws Exception exception on set up
     */
    public void setUp() throws Exception {
        super.setUp();
        StoreConfig storeConfig = defaultStoreContext.getStoreConfig();
        HdfsStorage storage = defaultStoreContext.getFileOperator().storage();
        hive = new AvroHiveUtil(storeConfig, connectorConfig, hiveMetaStore, storage);
    }

    @Test
    public void testCreateTable() throws Exception {
        setUp();
        prepareData(TOPIC, PARTITION);
        Partitioner partitioner = HiveTestUtils.getPartitioner(parsedConfig);
        StoreConfig storeConfig = defaultStoreContext.getStoreConfig();

        Schema schema = createSchema();
        hive.createTable(schema, partitioner);
        String location = "partition=" + String.valueOf(PARTITION);
        hiveMetaStore.addPartition(storeConfig.database(), storeConfig.table(), location);

        Struct expectedRecord = createRecord(schema);
        List<String> expectedResult = new ArrayList<>();
        List<String> expectedColumnNames = new ArrayList<>();
        for (Field field : schema.fields()) {
            expectedColumnNames.add(field.name());
            expectedResult.add(String.valueOf(expectedRecord.get(field.name())));
        }

        Table table = hiveMetaStore.getTable(storeConfig.database(), storeConfig.table());
        List<String> actualColumnNames = new ArrayList<>();
        for (FieldSchema column : table.getSd().getCols()) {
            actualColumnNames.add(column.getName());
        }

        assertEquals(expectedColumnNames, actualColumnNames);
        List<FieldSchema> partitionCols = table.getPartitionKeys();
        assertEquals(1, partitionCols.size());
        assertEquals("partition", partitionCols.get(0).getName());

        String result = HiveTestUtils.runHive(
            hiveExec,
            "SELECT * FROM " + storeConfig.table()
        );
        String[] rows = result.split("\n");
        // Only 6 of the 7 records should have been delivered due to flush_size = 3
        assertEquals(6, rows.length);
        for (String row : rows) {
            String[] parts = HiveTestUtils.parseOutput(row);
            int j = 0;
            for (String expectedValue : expectedResult) {
                assertEquals(expectedValue, parts[j++]);
            }
        }
    }

    @Test
    public void testAlterSchema() throws Exception {
        setUp();
        prepareData(TOPIC, PARTITION);
        Partitioner partitioner = HiveTestUtils.getPartitioner(parsedConfig);
        Schema schema = createSchema();
        StoreConfig storeConfig = defaultStoreContext.getStoreConfig();
        hive.createTable(schema, partitioner);

        String location = "partition=" + String.valueOf(PARTITION);
        hiveMetaStore.addPartition(storeConfig.database(), storeConfig.table(), location);
        Struct expectedRecord = createRecord(schema);
        List<String> expectedResult = new ArrayList<>();
        List<String> expectedColumnNames = new ArrayList<>();
        for (Field field : schema.fields()) {
            expectedColumnNames.add(field.name());
            expectedResult.add(String.valueOf(expectedRecord.get(field.name())));
        }

        Table table = hiveMetaStore.getTable(storeConfig.database(), storeConfig.table());
        List<String> actualColumnNames = new ArrayList<>();
        for (FieldSchema column : table.getSd().getCols()) {
            actualColumnNames.add(column.getName());
        }

        assertEquals(expectedColumnNames, actualColumnNames);

        Schema newSchema = createNewSchema();

        hive.alterSchema(newSchema);

        String result = HiveTestUtils.runHive(
            hiveExec,
            "SELECT * from " + storeConfig.table()
        );
        String[] rows = result.split("\n");
        // Only 6 of the 7 records should have been delivered due to flush_size = 3
        assertEquals(6, rows.length);
        for (String row : rows) {
            String[] parts = HiveTestUtils.parseOutput(row);
            int j = 0;
            for (String expectedValue : expectedResult) {
                assertEquals(expectedValue, parts[j++]);
            }
        }
    }

    private void prepareData(final String topic, int partition) throws Exception {
        TopicPartition tp = new TopicPartition(topic, partition);
        HdfsWriterCoordinator hdfsWriter = createWriter(context);
        hdfsWriter.recover(tp);

        List<SinkRecord> sinkRecords = createSinkRecords(7);

        hdfsWriter.write(sinkRecords);
        hdfsWriter.close();
        hdfsWriter.stop();
    }

    private HdfsWriterCoordinator createWriter(final SinkTaskContext context) {
        return new HdfsWriterCoordinator(connectorConfig, context);
    }
}
