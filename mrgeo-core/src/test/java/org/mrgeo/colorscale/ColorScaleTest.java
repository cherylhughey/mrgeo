/*
 * Copyright 2009-2016 DigitalGlobe, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.mrgeo.colorscale;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mrgeo.junit.UnitTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@SuppressWarnings("all") // test code, not included in production
public class ColorScaleTest
{

@Test
@Category(UnitTest.class)
public void testLoadXmlDefaults() throws ColorScale.ColorScaleException
{
  String xml = "<ColorMap name=\"Rainbow\">";
  xml += "<Scaling>Absolute</Scaling>"; //This tag is required, even though ColorScale object is initialized to Absolute
  xml += "<Color value=\"0.0\" color=\"0,0,127\" opacity=\"255\"/>";
  xml += "<Color value=\"0.2\" color=\"0,0,255\"/>";
  xml += "<Color value=\"0.4\" color=\"0,255,255\"/>";
  xml += "<Color value=\"0.6\" color=\"0,255,0\"/>";
  xml += "<Color value=\"0.8\" color=\"255,255,0\"/>";
  xml += "<Color value=\"1.0\" color=\"255,0,0\"/>";
  xml += "</ColorMap>";
  InputStream is = new ByteArrayInputStream(xml.getBytes());
  ColorScale cs = ColorScale.loadFromXML(is);

  Assert.assertTrue(ColorScale.Scaling.Absolute.name().equals(cs.getScaling().name()));
  Assert.assertFalse(cs.getForceValuesIntoRange());
  Assert.assertTrue(cs.getInterpolate());
  Assert.assertEquals(6, cs.size());

  int[] color = new int[4];
  cs.lookup(0.4, color);
  check(color, new int[]{0, 255, 255, 255});

}

@Test
@Category(UnitTest.class)
public void testLoadXmlTags() throws ColorScale.ColorScaleException
{
  String xml = "<ColorMap name=\"Rainbow\">";
  xml += "<Scaling>MinMax</Scaling> <!-- Could also be Absolute -->";
  xml += "<Interpolate>false</Interpolate>";
  xml += "<ForceValuesIntoRange>true</ForceValuesIntoRange>";
  xml += "<NullColor color=\"0,0,0\" opacity=\"0\"/>";
  xml += "<Color value=\"0.0\" color=\"0,0,127\" opacity=\"255\"/>";
  xml += "<!--Color value=\"0.000000000001\" color=\"0,0,127\" opacity=\"255\"/-->";
  xml += "<Color value=\"0.2\" color=\"0,0,255\"/> <!-- if not specified an opacity defaults to 255 -->";
  xml += "<Color value=\"0.4\" color=\"0,255,255\"/>";
  xml += "<Color value=\"0.6\" color=\"0,255,0\"/>";
  xml += "<Color value=\"0.8\" color=\"255,255,0\"/>";
  xml += "<Color value=\"1.0\" color=\"255,0,0\"/>";
  xml += "</ColorMap>";
  InputStream is = new ByteArrayInputStream(xml.getBytes());
  ColorScale cs = ColorScale.loadFromXML(is);

  Assert.assertTrue(ColorScale.Scaling.MinMax.name().equals(cs.getScaling().name()));
  Assert.assertTrue(cs.getForceValuesIntoRange());
  Assert.assertFalse(cs.getInterpolate());
  Assert.assertEquals(6, cs.size());
}

@Test
@Category(UnitTest.class)
public void testLoadXmlTagsEmpty() throws ColorScale.ColorScaleException
{
  String xml = "<ColorMap name=\"Rainbow\">";
  xml += "<Scaling>MinMax</Scaling>";
  xml += "<Interpolate></Interpolate>";
  xml += "<ForceValuesIntoRange></ForceValuesIntoRange>";
  xml += "<NullColor color=\"0,0,0\" opacity=\"0\"/>";
  xml += "<Color value=\"0.0\" color=\"0,0,127\" opacity=\"255\"/>";
  xml += "<Color value=\"0.2\" color=\"0,0,255\"/>";
  xml += "<Color value=\"0.4\" color=\"0,255,255\"/>";
  xml += "<Color value=\"0.6\" color=\"0,255,0\"/>";
  xml += "<Color value=\"0.8\" color=\"255,255,0\"/>";
  xml += "<Color value=\"1.0\" color=\"255,0,0\"/>";
  xml += "</ColorMap>";
  InputStream is = new ByteArrayInputStream(xml.getBytes());
  ColorScale cs = ColorScale.loadFromXML(is);

  Assert.assertTrue(ColorScale.Scaling.MinMax.name().equals(cs.getScaling().name()));
  Assert.assertFalse(cs.getForceValuesIntoRange());
  Assert.assertTrue(cs.getInterpolate());
  Assert.assertEquals(6, cs.size());
}

@Test
@Category(UnitTest.class)
public void testLoadJsonDefaults() throws ColorScale.ColorScaleException
{
  String json =
      "{\"NullColor\":{\"color\":\"0,0,0\",\"opacity\":\"0\"},\"Colors\":[{\"color\":\"255,0,0\",\"value\":\"0.0\"},{\"color\":\"255,255,0\",\"value\":\"0.25\"},{\"color\":\"0,255,255\",\"value\":\"0.75\"},{\"color\":\"255,255,255\",\"value\":\"1.0\"}]}";

  ColorScale cs = ColorScale.loadFromJSON(json);

  Assert.assertTrue(ColorScale.Scaling.Absolute.name().equals(cs.getScaling().name()));
  Assert.assertFalse(cs.getForceValuesIntoRange());
  Assert.assertTrue(cs.getInterpolate());
  Assert.assertEquals(4, cs.size());
}

@Test
@Category(UnitTest.class)
public void testLoadJson() throws ColorScale.ColorScaleException
{
  String json =
      "{\"Interpolate\":\"false\",\"ForceValuesIntoRange\":\"true\",\"NullColor\":{\"color\":\"0,0,0\",\"opacity\":\"0\"},\"Colors\":[{\"color\":\"255,0,0\",\"value\":\"0.0\"},{\"color\":\"255,255,0\",\"value\":\"0.25\"},{\"color\":\"0,255,255\",\"value\":\"0.75\"},{\"color\":\"255,255,255\",\"value\":\"1.0\"}],\"Scaling\":\"MinMax\"}";

  ColorScale cs = ColorScale.loadFromJSON(json);

  Assert.assertTrue(ColorScale.Scaling.MinMax.name().equals(cs.getScaling().name()));
  Assert.assertTrue(cs.getForceValuesIntoRange());
  Assert.assertFalse(cs.getInterpolate());
  Assert.assertEquals(4, cs.size());
}

@Test
@Category(UnitTest.class)
public void testDefault()
{
  ColorScale cs = ColorScale.createDefault();
  Assert.assertTrue(ColorScale.Scaling.MinMax.name().equals(cs.getScaling().name()));
  Assert.assertEquals(12, cs.size());
  cs.clear();
  Assert.assertTrue(ColorScale.Scaling.Absolute.name().equals(cs.getScaling().name()));
  Assert.assertEquals(0, cs.size());

  Assert.assertEquals(Double.NaN, cs.getTransparentValue());
  Assert.assertEquals(null, cs.getMin());
  Assert.assertEquals(null, cs.getMax());
}

@Test
@Category(UnitTest.class)
public void testSetDefault() throws ColorScale.ColorScaleException
{
  String xml = "<ColorMap name=\"Grayscale\">";
  xml += "<Scaling>MinMax</Scaling>";
  xml += "<Interpolate>1</Interpolate>";
  xml += "<ForceValuesIntoRange>1</ForceValuesIntoRange>";
  xml += "<NullColor color=\"0,0,0\" opacity=\"0\"/>";
  xml += "<Color value=\"0.0\" color=\"0,0,0\" opacity=\"255\"/>";
  xml += "<Color value=\"1.0\" color=\"255,255,255\"/>";
  xml += "</ColorMap>";
  InputStream is = new ByteArrayInputStream(xml.getBytes());
  ColorScale cs = ColorScale.loadFromXML(is);
  ColorScale.setDefault(cs);

  ColorScale def = ColorScale.createDefault();

  Assert.assertTrue(ColorScale.Scaling.MinMax.name().equals(def.getScaling().name()));
  Assert.assertEquals(2, def.size());
  Assert.assertEquals(Double.NaN, def.getTransparentValue());
  Assert.assertEquals(null, def.getMin());
  Assert.assertEquals(null, def.getMax());

  //Restore default
  ColorScale.setDefault(null);
}

@Test
@Category(UnitTest.class)
public void testEquals() throws ColorScale.ColorScaleException
{
  String xml = "<ColorMap name=\"Grayscale\">";
  xml += "<Scaling>MinMax</Scaling>";
  xml += "<Interpolate>1</Interpolate>";
  xml += "<ForceValuesIntoRange>0</ForceValuesIntoRange>";
  xml += "<NullColor color=\"0,0,0\" opacity=\"0\"/>";
  xml += "<Color value=\"0.0\" color=\"0,0,0\" opacity=\"255\"/>";
  xml += "<Color value=\"1.0\" color=\"255,255,255\"/>";
  xml += "</ColorMap>";
  InputStream is = new ByteArrayInputStream(xml.getBytes());
  ColorScale csXml = ColorScale.loadFromXML(is);

  String json =
      "{\"Scaling\":\"MinMax\",\"Interpolate\":\"1\",\"ForceValuesIntoRange\":\"0\",\"NullColor\":{\"color\":\"0,0,0\",\"opacity\":\"0\"},\"Colors\":[{\"color\":\"0,0,0\",\"opacity\":\"255\",\"value\":\"0.0\"},{\"color\":\"255,255,255\",\"opacity\":\"255\",\"value\":\"1.0\"}]}";

  ColorScale csJson = ColorScale.loadFromJSON(json);

  Assert.assertTrue(csXml.equals(csJson));
}

@Test
@Category(UnitTest.class)
public void testEqualsFalseWithScaleRange() throws ColorScale.ColorScaleException
{
  String xml = "<ColorMap name=\"Grayscale\">";
  xml += "<Scaling>MinMax</Scaling>";
  xml += "<Interpolate>1</Interpolate>";
  xml += "<ForceValuesIntoRange>0</ForceValuesIntoRange>";
  xml += "<NullColor color=\"0,0,0\" opacity=\"0\"/>";
  xml += "<Color value=\"0.0\" color=\"0,0,0\" opacity=\"255\"/>";
  xml += "<Color value=\"1.0\" color=\"255,255,255\"/>";
  xml += "</ColorMap>";
  InputStream is = new ByteArrayInputStream(xml.getBytes());
  ColorScale csXml = ColorScale.loadFromXML(is);

  String json =
      "{\"Scaling\":\"MinMax\",\"Interpolate\":\"1\",\"ForceValuesIntoRange\":\"0\",\"NullColor\":{\"color\":\"0,0,0\",\"opacity\":\"0\"},\"Colors\":[{\"color\":\"0,0,0\",\"opacity\":\"255\",\"value\":\"0.0\"},{\"color\":\"255,255,255\",\"opacity\":\"255\",\"value\":\"1.0\"}]}";

  ColorScale csJson = ColorScale.loadFromJSON(json);
  csJson.setScaleRange(0, 1000);

  Assert.assertFalse(csXml.equals(csJson));
}

@Test
@Category(UnitTest.class)
public void testMinMaxInterpolateForceValuesIntoRange() throws Exception
{
  ColorScale cs = ColorScale.createDefault();
  cs.setInterpolate(true);
  cs.setForceValuesIntoRange(true);
  cs.setScaleRange(-10, 10);
  cs.setScaling(ColorScale.Scaling.MinMax);
  cs.setTransparentValue(0.0);

  check(cs.lookup(0.0), new int[]{0, 0, 0, 0});

  check(cs.lookup(-15.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-10.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-5.0), new int[]{216, 240, 239, 255});
  check(cs.lookup(5.0), new int[]{18, 124, 57, 255});
  check(cs.lookup(10.0), new int[]{0, 0, 0, 255});
  check(cs.lookup(15.0), new int[]{0, 0, 0, 255});

  Assert.assertEquals(0.0, cs.getTransparentValue());
  Assert.assertEquals(-10.0, cs.getMin());
  Assert.assertEquals(10.0, cs.getMax());

}

@Test
@Category(UnitTest.class)
public void testMinMaxInterpolate() throws Exception
{
  ColorScale cs = ColorScale.createDefault();
  cs.setInterpolate(true);
  cs.setScaleRange(-10, 10);
  cs.setScaling(ColorScale.Scaling.MinMax);
  cs.setTransparentValue(0.0);

  check(cs.lookup(0.0), new int[]{0, 0, 0, 0});

  check(cs.lookup(-15.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-10.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-5.0), new int[]{216, 240, 239, 255});
  check(cs.lookup(5.0), new int[]{18, 124, 57, 255});
  check(cs.lookup(10.0), new int[]{0, 0, 0, 255});
  check(cs.lookup(15.0), new int[]{0, 0, 0, 0});

}

@Test
@Category(UnitTest.class)
public void testMinMaxNoInterpolateForceValuesIntoRange() throws Exception
{
  ColorScale cs = ColorScale.createDefault();
  cs.setInterpolate(false);
  cs.setForceValuesIntoRange(true);
  cs.setScaleRange(-10, 10);
  cs.setScaling(ColorScale.Scaling.MinMax);
  cs.setTransparentValue(0.0);

  check(cs.lookup(0.0), new int[]{0, 0, 0, 0});

  check(cs.lookup(-15.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-10.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-9.0), new int[]{255, 255, 255, 128});
  check(cs.lookup(-5.0), new int[]{229, 245, 249, 255});
  check(cs.lookup(5.0), new int[]{35, 139, 69, 255});
  check(cs.lookup(10.0), new int[]{0, 0, 0, 255});
  check(cs.lookup(15.0), new int[]{0, 0, 0, 255});

}

@Test
@Category(UnitTest.class)
public void testMinMaxNoInterpolate() throws Exception
{
  ColorScale cs = ColorScale.createDefault();
  cs.setInterpolate(false);
  cs.setScaleRange(-10, 10);
  cs.setScaling(ColorScale.Scaling.MinMax);
  cs.setTransparentValue(0.0);

  check(cs.lookup(0.0), new int[]{0, 0, 0, 0});

  check(cs.lookup(-15.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-10.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-9.0), new int[]{255, 255, 255, 128});
  check(cs.lookup(-5.0), new int[]{229, 245, 249, 255});
  check(cs.lookup(5.0), new int[]{35, 139, 69, 255});
  check(cs.lookup(10.0), new int[]{0, 0, 0, 255});
  check(cs.lookup(15.0), new int[]{0, 0, 0, 0});

}

@Test
@Category(UnitTest.class)
public void testAbsoluteNoInterpolateForceValuesIntoRange() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(false);
  cs.setForceValuesIntoRange(true);
  cs.setScaling(ColorScale.Scaling.Absolute);
  cs.setTransparentValue(-1.0);
  cs.put(0.0, 255, 0, 0, 0);
  cs.put(25.0, 172, 0, 64);
  cs.put(50.0, 128, 0, 128);
  cs.put(75.0, 64, 0, 172);
  cs.put(100.0, 0, 0, 255);

  check(cs.lookup(-10.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(-1.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(0.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(1e-7), new int[]{255, 0, 0, 0});
  check(cs.lookup(24.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(25.0), new int[]{172, 0, 64, 255});
  check(cs.lookup(60.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(90.0), new int[]{64, 0, 172, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
  check(cs.lookup(110.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testAbsoluteNoInterpolate() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(false);
  cs.setScaling(ColorScale.Scaling.Absolute);
  cs.setTransparentValue(-1.0);
  cs.put(0.0, 255, 0, 0, 0);
  cs.put(25.0, 172, 0, 64);
  cs.put(50.0, 128, 0, 128);
  cs.put(75.0, 64, 0, 172);
  cs.put(100.0, 0, 0, 255);

  check(cs.lookup(-10.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-1.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(0.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(1e-7), new int[]{255, 0, 0, 0});
  check(cs.lookup(24.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(25.0), new int[]{172, 0, 64, 255});
  check(cs.lookup(60.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(90.0), new int[]{64, 0, 172, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
  check(cs.lookup(110.0), new int[]{0, 0, 0, 0});
}

@Test
@Category(UnitTest.class)
public void testAbsoluteInterpolateForceValuesIntoRange() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setForceValuesIntoRange(true);
  cs.setScaling(ColorScale.Scaling.Absolute);
  cs.setTransparentValue(-1.0);
  cs.put(0.0, 255, 0, 0, 0);
  cs.put(12.0, 172, 0, 64);
  cs.put(50.0, 128, 0, 128);
  cs.put(62.0, 64, 0, 172);
  cs.put(100.0, 0, 0, 255);

  check(cs.lookup(-10.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(-1.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(0.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(1e-7), new int[]{255, 0, 0, 0});
  check(cs.lookup(6.0), new int[]{214, 0, 32, 127});
  check(cs.lookup(12.0), new int[]{172, 0, 64, 255});
  check(cs.lookup(31.0), new int[]{150, 0, 96, 255});
  check(cs.lookup(50.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(56.0), new int[]{96, 0, 150, 255});
  check(cs.lookup(62.0), new int[]{64, 0, 172, 255});
  check(cs.lookup(81.0), new int[]{32, 0, 214, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
  check(cs.lookup(110.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testAbsoluteInterpolate() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Absolute);
  cs.setTransparentValue(-1.0);
  cs.put(0.0, 255, 0, 0, 0);
  cs.put(12.0, 172, 0, 64);
  cs.put(50.0, 128, 0, 128);
  cs.put(62.0, 64, 0, 172);
  cs.put(100.0, 0, 0, 255);

  check(cs.lookup(-10.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(-1.0), new int[]{0, 0, 0, 0});
  check(cs.lookup(0.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(1e-7), new int[]{255, 0, 0, 0});
  check(cs.lookup(6.0), new int[]{214, 0, 32, 127});
  check(cs.lookup(12.0), new int[]{172, 0, 64, 255});
  check(cs.lookup(31.0), new int[]{150, 0, 96, 255});
  check(cs.lookup(50.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(56.0), new int[]{96, 0, 150, 255});
  check(cs.lookup(62.0), new int[]{64, 0, 172, 255});
  check(cs.lookup(81.0), new int[]{32, 0, 214, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
  check(cs.lookup(110.0), new int[]{0, 0, 0, 0});
}

@Test
@Category(UnitTest.class)
public void testQuantilesFallbackToAbsolute1() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, null);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(50.0), new int[]{127, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testQuantilesFallbackToAbsolute2() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { };
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(50.0), new int[]{127, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testQuantilesOne() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 75 };
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 128, 0, 128);
  cs.put(3.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(75.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testMultiOddColorOneQuantile() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 75 };
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 170, 0, 60);
  cs.put(3.0, 128, 0, 128);
  cs.put(4.0, 120, 0, 140);
  cs.put(5.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(75.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testMultiEvenColorOneQuantile() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 75 };
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 170, 0, 60);
  cs.put(3.0, 128, 0, 128);
  cs.put(4.0, 120, 0, 140);
  cs.put(5.0, 80, 0, 180);
  cs.put(6.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(75.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testMultiOddColorTwoQuantiles() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 75, 90 };
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 170, 0, 60);
  cs.put(3.0, 128, 0, 128);
  cs.put(4.0, 120, 0, 140);
  cs.put(5.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(75.0), new int[]{170, 0, 60, 255});
  check(cs.lookup(90.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testMultiEvenColorTwoQuantiles() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 75, 90 };
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 170, 0, 60);
  cs.put(3.0, 128, 0, 128);
  cs.put(4.0, 120, 0, 140);
  cs.put(5.0, 80, 0, 180);
  cs.put(6.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(75.0), new int[]{170, 0, 60, 255});
  check(cs.lookup(90.0), new int[]{120, 0, 140, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testQuantilesTwo() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 70, 90 };
  cs.put(1.0, 255, 0, 0);
  cs.put(2.0, 128, 0, 128);
  cs.put(3.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 255});
  check(cs.lookup(70.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(90.0), new int[]{64, 0, 191, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
}

@Test
@Category(UnitTest.class)
public void testQuantilesThree() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 60, 70, 90 };
  cs.put(1.0, 255, 0, 0, 0);
  cs.put(2.0, 128, 0, 128);
  cs.put(3.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(70.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
  check(cs.lookup(90.0), new int[]{64, 0, 191, 255});
  // 75.0 is 25% of the distance between q2 and q3, so the color
  // value should be 25% less that the color value for q2.
  int expectedRed = (int) Math.round(128 - (0.25 * (128 - 64)));
  int expectedBlue = (int) Math.round(128 - (0.25 * (128 - 191)));
  check(cs.lookup(75.0), new int[]{expectedRed, 0, expectedBlue, 255});
}

@Test
@Category(UnitTest.class)
public void testQuantilesFour() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 60, 65, 70, 90 };
  cs.put(1.0, 255, 0, 0, 0);
  cs.put(2.0, 128, 0, 128);
  cs.put(3.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(65.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
  check(cs.lookup(60.0), new int[]{192, 0, 64, 127});
}

@Test
@Category(UnitTest.class)
public void testQuantilesFive() throws Exception
{
  ColorScale cs = ColorScale.empty();
  cs.setInterpolate(true);
  cs.setScaling(ColorScale.Scaling.Quantile);
  cs.setTransparentValue(-1.0);
  double[] quantiles = { 60, 65, 70, 80, 90 };
  cs.put(1.0, 255, 0, 0, 0);
  cs.put(2.0, 128, 0, 128);
  cs.put(3.0, 0, 0, 255);
  cs.setScaleRangeWithQuantiles(0, 100, quantiles);

  check(cs.lookup(0.0), new int[]{255, 0, 0, 0});
  check(cs.lookup(70.0), new int[]{128, 0, 128, 255});
  check(cs.lookup(100.0), new int[]{0, 0, 255, 255});
  check(cs.lookup(60.0), new int[]{213, 0, 42, 85});
  check(cs.lookup(65.0), new int[]{171, 0, 85, 170});
  check(cs.lookup(80.0), new int[]{86, 0, 170, 255});
  check(cs.lookup(90.0), new int[]{43, 0, 212, 255});

  // Check interpolated colors
  // For 62.5, color values should be halfway between the color values assigned
  // to 60.0 and 65.0
  check(cs.lookup(62.5), new int[]{192, 0, 64, 128});
  // For 30.0, color values should be halfway between the color values assigned
  // to 60.0 and 0
  check(cs.lookup(30.0), new int[]{234, 0, 21, 43});
}

//private void dump(double value, int[] color)
//{
//  System.out.println("Color for value " + value + " is " +  color[0] + ", " + color[1] + ", " + color[2]);
//}

private void check(int[] lookup, int[] is)
{
  for (int i = 0; i < 4; i++)
  {
    Assert.assertEquals(is[i], lookup[i]);
  }
}

}
