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

package org.mrgeo.services.wcs;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.mrgeo.data.image.MrsImageDataProvider;
import org.mrgeo.image.MrsPyramid;
import org.mrgeo.image.MrsPyramidMetadata;
import org.mrgeo.services.Version;
import org.mrgeo.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class WcsCapabilities
{
private static final Logger log = LoggerFactory.getLogger(WcsCapabilities.class);

/*
 * Adds OGC metadata elements to the the parent element
 */
private static void addHttpElement100(Element parent, String requestUrl, Version version)
{
  Element http = XmlUtils.createElement(parent, "HTTP");

  Element get = XmlUtils.createElement(http, "Get");

  Element onlineResource = XmlUtils.createElement(get, "OnlineResource");
  onlineResource.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
  onlineResource.setAttribute("xlink:type", "simple");
  onlineResource.setAttribute("xlink:href", requestUrl);


  Element post = XmlUtils.createElement(http, "Post");
  onlineResource = XmlUtils.createElement(post, "OnlineResource");
  onlineResource.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
  onlineResource.setAttribute("xlink:type", "simple");
  onlineResource.setAttribute("xlink:href", requestUrl);
}

/*
 * Adds OGC metadata elements to the the parent element
 */
private static void addHttpElement110(Element parent, String requestUrl, String operation)
{
  Element op = XmlUtils.createElement(parent, "ows:Operation");
  op.setAttribute("name", operation);

  Element http = XmlUtils.createElement(XmlUtils.createElement(op, "ows:DCP"),
      "ows:HTTP");
  Element get = XmlUtils.createElement(http, "ows:Get");
  get.setAttribute("xlink:href", requestUrl);

  Element post = XmlUtils.createElement(http, "ows:Post");
  post.setAttribute("xlink:href", requestUrl);
}

/**
 * Generates an XML document for a DescribeTiles request
 *
 * @param version    WMS version
 * @param requestUrl HTTP request url
 * @param layers     list of pyramid directories being served by MrGeo
 * @return XML document list of pyramid directories being served by MrGeo
 * @throws IOException
 * @throws InterruptedException
 * @throws ParserConfigurationException
 */

public Document generateDoc(Version version, String requestUrl,
    MrsImageDataProvider[] layers) throws IOException,
    ParserConfigurationException
{
  Document doc;
  DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
  dBF.setValidating(true);

  DocumentBuilder builder = dBF.newDocumentBuilder();
  doc = builder.newDocument();

  if (version.isLess("1.1.0"))
  {
    generate100(doc, new Version("1.0.0"), requestUrl, layers);
  }
  else
  {
    generate110(doc, new Version("1.1.0"), requestUrl, layers);
  }

  return doc;
}

private void generate110(Document doc, Version version, String requestUrl, MrsImageDataProvider[] layers)
    throws IOException
{
  Element wmc = XmlUtils.createElement(doc, "wcs:Capabilities");
  wmc.setAttribute("version", version.toString());

  wmc.setAttribute("xmlns:wcs", "http://www.opengis.net/wcs/" + version);
  wmc.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
  wmc.setAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
  wmc.setAttribute("xmlns:ows", "http://www.opengis.net/ows/" + version.getMajor() + "." + version.getMinor());
  wmc.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
  //wmc.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
  //wmc.setAttribute("xsi:schemaLocation", "???");.

  Element service = XmlUtils.createElement(wmc, "ows:ServiceIdentification");
  XmlUtils.createTextElement2(service, "ows:Title", "MrGeo Web Coverage Service");
  XmlUtils.createTextElement2(service, "ows:ServiceType", "OGC WCS");
  // XmlUtils.createTextElement2(service, "ows:ServiceTypeVersion", "1.0.0");
  XmlUtils.createTextElement2(service, "ows:ServiceTypeVersion", "1.1.0");

  Element operations = XmlUtils.createElement(wmc, "ows:OperationsMetadata");

  addHttpElement110(operations, requestUrl, "GetCapabilities");
  addHttpElement110(operations, requestUrl, "DescribeCoverage");
  addHttpElement110(operations, requestUrl, "GetCoverage");

  Element contents = XmlUtils.createElement(wmc, "wcs:Contents");
  addLayers110(contents, layers);

}

private void generate100(Document doc, Version version, String requestUrl, MrsImageDataProvider[] layers)
    throws IOException
{
  Element wmc = doc.createElement("WCS_Capabilities");
  wmc.setAttribute("version", version.toString());

  wmc.setAttribute("xmlns", "http://www.opengis.net/wcs");
  wmc.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
  wmc.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
  wmc.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
  wmc.setAttribute("xsi:schemaLocation",
      "http://www.opengis.net/wcs http://schemas.opengeospatial.net/wcs/" + version +
          "/wcsCapabilities.xsd");
  doc.appendChild(wmc);
  // //
  // Service
  // //
  Element service = XmlUtils.createElement(wmc, "Service");
  wmc.appendChild(service);
  // WMT Defined
  XmlUtils.createTextElement2(service, "name", "OGC:WC");
  XmlUtils.createTextElement2(service, "description", "MrGeo Web Coverage Service");
  XmlUtils.createTextElement2(service, "label", "MrGeo Web Coverage Service");
  XmlUtils.createTextElement2(service, "fees", "NONE");
  XmlUtils.createTextElement2(service, "accessConstraints", "NONE");

  // //
  // Capability
  // //
  Element capability = XmlUtils.createElement(wmc, "Capability");

  // Request
  Element requestTag = XmlUtils.createElement(capability, "Request");
  // GetCapabilities
  {
    Element getCapabilities = XmlUtils.createElement(requestTag, "GetCapabilities");
    Element gcDcpType = XmlUtils.createElement(getCapabilities, "DCPType");
    addHttpElement100(gcDcpType, requestUrl, version);
  }
  Element describeCoverage = XmlUtils.createElement(requestTag, "DescribeCoverage");
  Element dcDcpType = XmlUtils.createElement(describeCoverage, "DCPType");
  addHttpElement100(dcDcpType, requestUrl, version);
  Element getCapabilities = XmlUtils.createElement(requestTag, "GetCoverage");
  Element gcDcpType = XmlUtils.createElement(getCapabilities, "DCPType");
  addHttpElement100(gcDcpType, requestUrl, version);

  // Exception
  Element exception = XmlUtils.createElement(capability, "Exception");
  XmlUtils.createTextElement2(exception, "Format", "application/vnd.ogc.se_xml");

  // ContentMetadata
  Element contentMetadata = XmlUtils.createElement(wmc, "ContentMetadata");
  addLayers100(contentMetadata, layers);
}

/*
* Adds data layers to the GetCapabilities response
*/
private void addLayers110(Element parent, MrsImageDataProvider[] providers) throws IOException
{
  double minx = Double.MAX_VALUE;
  double maxx = -Double.MAX_VALUE;
  double miny = Double.MAX_VALUE;
  double maxy = -Double.MAX_VALUE;

  Arrays.sort(providers, new MrsImageComparator());

  for (MrsImageDataProvider provider : providers)
  {
    log.debug("pyramids: " + provider.getResourceName());

    Element layer = XmlUtils.createElement(parent, "wcs:CoverageSummary");

    XmlUtils.createTextElement2(layer, "wcs:Identifier", provider.getResourceName());
    XmlUtils.createTextElement2(layer, "ows:Title", provider.getResourceName());

    try
    {
      MrsPyramidMetadata meta = provider.getMetadataReader().read();

      String abs = meta.getTag("abstract", null);
      if (abs != null)
      {
        XmlUtils.createTextElement2(layer, "ows:Abstract", abs);
      }
    }
    catch (IOException ignored)
    {
    }


    MrsPyramid pyramid = MrsPyramid.open(provider);
    minx = Math.min(minx, pyramid.getBounds().w);
    miny = Math.min(miny, pyramid.getBounds().s);
    maxx = Math.max(maxx, pyramid.getBounds().e);
    maxy = Math.max(maxy, pyramid.getBounds().n);


    Element envelope = XmlUtils.createElement(layer, "ows:WGS84BoundingBox");
    XmlUtils.createTextElement2(envelope, "ows:LowerCorner",
        pyramid.getBounds().w + " " + pyramid.getBounds().s);
    XmlUtils.createTextElement2(envelope, "ows:UpperCorner",
        pyramid.getBounds().e + " " + pyramid.getBounds().n);
  }

}

/*
 * Adds data layers to the GetCapabilities response
 */
@SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC_ANON", justification = "Just a simple inline comparator")
private void addLayers100(Element parent, MrsImageDataProvider[] providers) throws IOException
{
  double minx = Double.MAX_VALUE;
  double maxx = -Double.MAX_VALUE;
  double miny = Double.MAX_VALUE;
  double maxy = -Double.MAX_VALUE;

  Arrays.sort(providers, new Comparator<MrsImageDataProvider>()
  {
    @Override
    public int compare(MrsImageDataProvider o1, MrsImageDataProvider o2)
    {
      return o1.getResourceName().compareTo(o2.getResourceName());
    }
  });

  for (MrsImageDataProvider provider : providers)
  {
    log.debug("pyramids: " + provider.getResourceName());

    Element layer = XmlUtils.createElement(parent, "CoverageOfferingBrief");

    try
    {
      MrsPyramidMetadata meta = provider.getMetadataReader().read();

      String abs = meta.getTag("abstract", null);
      if (abs != null)
      {
        XmlUtils.createTextElement2(layer, "description", abs);
      }
    }
    catch (IOException ignored)
    {
    }

    XmlUtils.createTextElement2(layer, "name", provider.getResourceName());
    XmlUtils.createTextElement2(layer, "label", provider.getResourceName());

    MrsPyramid pyramid = MrsPyramid.open(provider);
    minx = Math.min(minx, pyramid.getBounds().w);
    miny = Math.min(miny, pyramid.getBounds().s);
    maxx = Math.max(maxx, pyramid.getBounds().e);
    maxy = Math.max(maxy, pyramid.getBounds().n);

    Element envelope = XmlUtils.createElement(layer, "lonLatEnvelope");
    envelope.setAttribute("srsName", "WGS84(DD)");
    XmlUtils.createTextElement2(envelope, "gml:pos",
        "" + pyramid.getBounds().w + " " +
            pyramid.getBounds().s);
    XmlUtils.createTextElement2(envelope, "gml:pos",
        "" + pyramid.getBounds().e + " " +
            pyramid.getBounds().n);
    parent.appendChild(layer);
  }
}

@SuppressFBWarnings(value = "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE", justification = "Do not need serialization")
private static class MrsImageComparator implements Comparator<MrsImageDataProvider>
{
  @Override
  public int compare(MrsImageDataProvider o1, MrsImageDataProvider o2)
  {
    return o1.getResourceName().compareTo(o2.getResourceName());
  }
}
}
