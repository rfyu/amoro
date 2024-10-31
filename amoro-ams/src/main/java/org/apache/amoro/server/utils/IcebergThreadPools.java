/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.utils;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.iceberg.util.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class IcebergThreadPools {

  private static final Logger LOG = LoggerFactory.getLogger(IcebergThreadPools.class);
  private static volatile ExecutorService planningExecutor;
  private static volatile ExecutorService commitExecutor;

  public static void init(Configurations serviceConfig) {
    int planningThreadPoolSize =
        Math.max(
            Runtime.getRuntime().availableProcessors() / 2,
            serviceConfig.getInteger(AmoroManagementConf.TABLE_MANIFEST_IO_PLANNING_THREAD_COUNT));
    if (planningExecutor == null) {
      synchronized (IcebergThreadPools.class) {
        if (planningExecutor == null) {
          planningExecutor =
              ThreadPools.newWorkerPool("iceberg-planning-pool", planningThreadPoolSize);
        }
      }
    }

    int commitThreadPoolSize =
        Math.max(
            Runtime.getRuntime().availableProcessors() / 2,
            serviceConfig.getInteger(AmoroManagementConf.TABLE_MANIFEST_IO_COMMIT_THREAD_COUNT));
    if (commitExecutor == null) {
      synchronized (IcebergThreadPools.class) {
        if (commitExecutor == null) {
          commitExecutor = ThreadPools.newWorkerPool("iceberg-commit-pool", commitThreadPoolSize);
        }
      }
    }

    LOG.info(
        "init iceberg thread pool success, planningExecutor size:{},commitExecutor size:{}",
        planningThreadPoolSize,
        commitThreadPoolSize);
  }

  public static ExecutorService getPlanningExecutor() {
    return Objects.requireNonNull(planningExecutor, "planningExecutor must not null");
  }

  public static ExecutorService getCommitExecutor() {
    return Objects.requireNonNull(commitExecutor, "commitExecutor must not null");
  }
}
