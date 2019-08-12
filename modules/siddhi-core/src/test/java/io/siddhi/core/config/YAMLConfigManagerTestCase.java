/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.core.config;

import io.siddhi.core.util.config.ConfigReader;
import io.siddhi.core.util.config.YAMLConfigManager;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class YAMLConfigManagerTestCase {
    private static final Logger log = Logger.getLogger(YAMLConfigManagerTestCase.class);

    @Test
    public void yamlConfigManagerTest1() {
        log.info("YAMLConfigManagerTest1");

        String baseDir = Paths.get(".").toString();
        Path path = Paths.get(baseDir, "src", "test", "resources", "systemProperties.yaml");

        YAMLConfigManager yamlConfigManager = new YAMLConfigManager(path.toAbsolutePath().toString());
        yamlConfigManager.init();

        Map<String, String> testConfigs = yamlConfigManager.extractSystemConfigs("test");
        Assert.assertEquals(testConfigs.size(), 0);

        Map<String, String> source1Ref = yamlConfigManager.extractSystemConfigs("source1");
        Assert.assertEquals(source1Ref.size(), 3);
        Assert.assertEquals(source1Ref.get("type"), "source-type");
        Assert.assertEquals(source1Ref.get("property1"), "value1");
        Assert.assertEquals(source1Ref.get("property2"), "value2");
        Assert.assertNull(source1Ref.get("property3"));

        String partitionById = yamlConfigManager.extractProperty("partitionById");
        Assert.assertEquals(partitionById, "TRUE");

        String shardID1 = yamlConfigManager.extractProperty("shardId1");
        Assert.assertNull(shardID1);

        ConfigReader testConfigReader = yamlConfigManager.generateConfigReader("test", "test");
        Assert.assertEquals(testConfigReader.getAllConfigs().size(), 0);

        ConfigReader configReader = yamlConfigManager.generateConfigReader("store", "rdbms");
        Assert.assertEquals(configReader.getAllConfigs().size(), 5);
        Assert.assertEquals(configReader.readConfig("mysql.batchEnable", "test"), "true");
        Assert.assertEquals(configReader.readConfig("test", "test"), "test");

    }
}
