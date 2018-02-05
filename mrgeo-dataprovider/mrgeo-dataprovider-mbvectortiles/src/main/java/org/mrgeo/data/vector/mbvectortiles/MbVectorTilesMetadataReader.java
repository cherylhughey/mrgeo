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

package org.mrgeo.data.vector.mbvectortiles;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteStatement;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.mrgeo.data.vector.VectorMetadata;
import org.mrgeo.data.vector.VectorMetadataReader;
import org.mrgeo.geometry.Geometry;
import org.mrgeo.geometry.GeometryFactory;
import org.mrgeo.utils.tms.Bounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;

public class MbVectorTilesMetadataReader implements VectorMetadataReader
{
  private static Logger log = LoggerFactory.getLogger(MbVectorTilesMetadataReader.class);
  private VectorMetadata metadata;
  private MbVectorTilesDataProvider dataProvider;

  public MbVectorTilesMetadataReader(MbVectorTilesDataProvider provider)
  {
    this.dataProvider = provider;
  }

  @Override
  public VectorMetadata read() throws IOException
  {
    if (metadata == null)
    {
      try {
        metadata = loadMetadata();
      } catch (SQLException e) {
        throw new IOException(e);
      }
    }
    return metadata;
  }

  @Override
  public VectorMetadata reload() throws IOException
  {
    return null;
  }

  @SuppressFBWarnings(value = {"SQL_INJECTION_JDBC", "SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING"}, justification = "User supplied queries are a requirement")
  private VectorMetadata loadMetadata() throws SQLException, IOException
  {
    VectorMetadata metadata = new VectorMetadata();
    // TODO: Need to implement this. But I'm not sure how to because the
    // set of attributes can be different for every feature. We would
    // have to run through all the features to get MBR. Or the other
    // option for the bbox is to get the bbox from the zoom/x/y values.
//    MbVectorTilesSettings dbSettings = dataProvider.parseResourceName();
//    SQLiteConnection conn = null;
//    try {
//      conn = MbVectorTilesDataProvider.getDbConnection(dbSettings);
//      SQLiteStatement st = conn.prepare("SELECT order_id FROM orders WHERE quantity >= ?");
//      try {
//        st.bind(1, minimumQuantity);
//        while (st.step()) {
//          orders.add(st.columnLong(0));
//        }
//      } finally {
//        st.dispose();
//      }
//      conn.dispose();
//
//      try (Statement st = conn.prepareStatement(dbSettings.getQuery(),
//              ResultSet.TYPE_FORWARD_ONLY,
//              ResultSet.CONCUR_READ_ONLY)) {
//        try (ResultSet rs = ((PreparedStatement) st).executeQuery()) {
//          ResultSetMetaData dbMetadata = rs.getMetaData();
//          for (int c=1; c < dbMetadata.getColumnCount(); c++) {
//            metadata.addAttribute(dbMetadata.getColumnLabel(c));
//          }
//        }
//      }
//      // Now get the minimum bounding rectangle
//      String mbrQuery = dbSettings.getMBRQuery();
//      if (mbrQuery == null || mbrQuery.isEmpty()) {
//        // Look for the first occurrence of SELECT ... FROM and replace it with
//        // SELECT ST_AsText(ST_Extent(geom)) FROM. Make sure to match case insensitively,
//        // but the non-replaced portion of the string must retain its case (hence the
//        // use of (?i) to inline the case insensitive match).
//        mbrQuery = dbSettings.getQuery().replaceFirst("(?i)SELECT .* FROM",
//                "SELECT ST_AsText(ST_Extent(" + dbSettings.getGeomColumnLabel() + ")) FROM");
//      }
//      try (Statement st = conn.prepareStatement(mbrQuery,
//              ResultSet.TYPE_FORWARD_ONLY,
//              ResultSet.CONCUR_READ_ONLY)) {
//        try (ResultSet rs = ((PreparedStatement) st).executeQuery()) {
//          if (rs.next()) {
//            String mbrWkt = rs.getString(1);
//            WKTReader wktReader = new WKTReader();
//            Geometry geom = null;
//            try {
//              geom = GeometryFactory.fromJTS(wktReader.read(mbrWkt));
//              if (geom != null) {
//                Bounds mbr = geom.getBounds();
//                metadata.setBounds(mbr);
//              }
//              else {
//                log.warn("Unable to convert WKT returned from Postgres to MrGeo geometry: " + mbrWkt);
//              }
//            } catch (ParseException e) {
//              log.warn("Unable to parse WKT returned form Postgres: " + mbrWkt);
//            }
//          }
//        }
//      }
//    }
    return metadata;
  }
}
