/*
 * Copyright 2009-2017. DigitalGlobe, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mrgeo.core;

/**
 * MrGeoConstants is a class for maintaining the constants
 * for the runtime environment of MrGeo.
 */
public class MrGeoConstants
{

  
  /*
   * Start conatants
   */


/*
 * Global configuration keys
 */
@Deprecated
public static final String MRGEO_HOME = "MRGEO_HOME";
@Deprecated
public static final String MRGEO_HOME_CONF_DIR = "/conf";

public static final String MRGEO_CONF_DIR = "MRGEO_CONF_DIR";
public static final String MRGEO_COMMON_HOME = "MRGEO_COMMON_HOME";
public static final String MRGEO_CONF = "mrgeo.conf";


public static final String MRGEO_SETTINGS = "settings.properties";
public static final String MRGEO_DEVELOPMENT_MODE = "development.mode";

public static final String MRGEO_CLUSTER = "cluster";
  /*
   * Accumulo configuration keys
   */
//  public static final String MRGEO_ACC_USER = "accumulo.user";
//  public static final String MRGEO_ACC_ZOO = "accumulo.zooservers";
//  public static final String MRGEO_ACC_PASSWORD = "accumulo.password";
//  public static final String MRGEO_ACC_INST = "accumulo.instance";
//  public static final String MRGEO_ACC_TABLE = "accumulo.table";
//  public static final boolean MRGEO_ACC_PASSB64 = false;

/*
 * HDFS configuration keys
 */
public static final String MRGEO_HDFS_TSV = "tsv.base";
public static final String MRGEO_HDFS_VECTOR = "vector.base";
public static final String MRGEO_HDFS_COLORSCALE = "colorscale.base";
public static final String MRGEO_HDFS_IMAGE = "image.base";
public static final String MRGEO_HDFS_KML = "kml.base";

public static final String MRGEO_HDFS_DISTRIBUTED_CACHE = "distributed.base";

/* Spark configuration keys
 *
 */
public static final String MRGEO_USE_KRYO = "use.kryo.serialization";
public static final String MRGEO_AUTOPERSISTANCE = "autopersistance";

public static final String MRGEO_MAXMEMORY = "max.memory";
public static final String MRGEO_MAXEXECUTORS = "max.executors";
public static final String MRGEO_MAXCORES = "max.cores";

/*
 * Runtime configuration keys
 */
public static final String MRGEO_JAR = "jar.path";
public static final String GDAL_PATH = "gdal.path";
public static final String SQLITE_NATIVE_PATH = "sqlite.native.path";

public static final String DEPENDENCY_CLASSPATH = "dependency.classpath";

/*
 * Legion configuration keys
 */
public static final String MRGEO_LEGION_PROC = "legion.processortype.preference";

/*
 * Image configuration keys
 */
public static final String MRGEO_MRS_TILESIZE = "mrsimage.tilesize";
public static final int MRGEO_MRS_TILESIZE_DEFAULT_INT = 512;
public static final String MRGEO_MRS_TILESIZE_DEFAULT = Integer.toString(MRGEO_MRS_TILESIZE_DEFAULT_INT);

public static final String MRGEO_DEFAULT_COLORSCALE = "default-colorscale";
/*
 * Security classification keys
 */
public static final String MRGEO_PROTECTION_LEVEL_REQUIRED = "protection.level.required";
public static final String MRGEO_PROTECTION_LEVEL_DEFAULT = "protection.level.default";
public static final String MRGEO_PROTECTION_LEVEL = "protectionLevel";

/*
 * WMS/WCS Keys
 */
public static final String MRGEO_WCS_CAPABILITIES_CACHE = "wcs.capabilities.cache";
public static final String MRGEO_WCS_CAPABILITIES_REFRESH = "wcs.capabilities.refresh";

/*
 * Postgres keys
 */
public static final String MRGEO_POSTGRES_PARTITION_RECORDS = "mrgeo.postgres.partition.records";

/*
 * Mb Tiles keys
 */
public static final String MRGEO_MBTILES_PARTITION_RECORDS = "mrgeo.mbtiles.partition.records";

/**
 * The default constructor for the class is private.  Additionally,
 * nothing can instantiate this class.
 */
private MrGeoConstants()
{
  // nothing will instantiate this class - not even within this class
  throw new AssertionError();
}
}
