/*******************************************************************************
 * Copyright (C) 2018-2019 Arpit Shah and Artos Contributors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.theartos;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for storing framework Configuration. During test
 * suit execution XML file will be searched at location ./conf
 */
public class ReportPortalConfigParser {

	public static final String CONFIG_BASE_DIR = "." + File.separator + "conf" + File.separator;
	final File fXmlFile = new File(CONFIG_BASE_DIR + "reportportal_configuration.xml");

	// Default Values
	private String Project_Name = "RPT_PRJ_1";
	private String TestSuite_Name = "TEST_SUITE_1";
	private String Release_Name = "01.02.0002";
	private String Base_URL = "http://58.22.3.239:8080";
	private String UUID = "0acb493c-37d2-4afc-a029-3c7aad01fb78";

	/**
	 * Constructor
	 * 
	 * @param createIfNotPresent enables creation of default configuration file if
	 *                           not present
	 */
	public ReportPortalConfigParser(boolean createIfNotPresent) {
		readXMLConfig(createIfNotPresent);
	}

	/**
	 * Reads Report Portal configuration file and set global values so framework
	 * configurations is available to everyone
	 * 
	 * @param createIfNotPresent enables creation of default configuration file if
	 *                           not present
	 */
	public void readXMLConfig(boolean createIfNotPresent) {

		try {
			if (!fXmlFile.exists() || !fXmlFile.isFile()) {
				if (createIfNotPresent) {
					System.err.println(
							"WARNING : " + fXmlFile.getAbsolutePath() + " not found.\nWARNING : Creating default "
									+ fXmlFile.getAbsolutePath() + ". Please populate it with correct information");
					fXmlFile.getParentFile().mkdirs();
					writeDefaultConfig(fXmlFile);
				}
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			readOrganisationInfo(doc);
		} catch (FileNotFoundException fe) {
			System.out.println(fe.getMessage() + "\n" + "Fall back to Default Organisation values");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ===============================================================
	// Write
	// ===============================================================

	/**
	 * Writes default configuration file
	 * 
	 * @param fXmlFile Destination file object
	 * @throws Exception
	 */
	private void writeDefaultConfig(File fXmlFile) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("configuration");
		doc.appendChild(rootElement);

		addReportPortalInfo(doc, rootElement);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(fXmlFile);

		transformer.transform(source, result);

	}

	private void addReportPortalInfo(Document doc, Element rootElement) {
		// ReportPortal Info elements
		Element orgnization_info = doc.createElement("reportportal_info");
		rootElement.appendChild(orgnization_info);

		// Properties of ReportPOrtal Info
		{
			Element property = doc.createElement("property");
			property.appendChild(doc.createTextNode(getProject_Name()));
			orgnization_info.appendChild(property);

			Attr attr = doc.createAttribute("name");
			attr.setValue("Project_Name");
			property.setAttributeNode(attr);
		}
		{
			Element property = doc.createElement("property");
			property.appendChild(doc.createTextNode(getTestSuite_Name()));
			orgnization_info.appendChild(property);

			Attr attr = doc.createAttribute("name");
			attr.setValue("TestSuite_Name");
			property.setAttributeNode(attr);
		}
		{
			Element property = doc.createElement("property");
			property.appendChild(doc.createTextNode(getRelease_Name()));
			orgnization_info.appendChild(property);

			Attr attr = doc.createAttribute("name");
			attr.setValue("Release_Name");
			property.setAttributeNode(attr);
		}
		{
			Element property = doc.createElement("property");
			property.appendChild(doc.createTextNode(getBase_URL()));
			orgnization_info.appendChild(property);

			Attr attr = doc.createAttribute("name");
			attr.setValue("Base_URL");
			property.setAttributeNode(attr);
		}
		{
			Element property = doc.createElement("property");
			property.appendChild(doc.createTextNode(getUUID()));
			orgnization_info.appendChild(property);

			Attr attr = doc.createAttribute("name");
			attr.setValue("UUID");
			property.setAttributeNode(attr);
		}
	}

	// ===============================================================
	// Read
	// ===============================================================
	/**
	 * Reads reportportalInfo from config file
	 * 
	 * @param doc Document object of an XML file
	 */
	private void readOrganisationInfo(Document doc) {
		// System.out.println("Root element :" +
		// doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("reportportal_info");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);

//			Element element = (Element) nNode;

			NodeList nChildList = nNode.getChildNodes();
			for (int i = 0; i < nChildList.getLength(); i++) {
				Node nChildNode = nChildList.item(i);
				if (nChildNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nChildNode;
					// System.out.println(eElement.getNodeName());
					// System.out.println(eElement.getAttribute("name"));
					// System.out.println(eElement.getAttribute("name") +
					// ":" +
					// eElement.getTextContent());
					if ("Project_Name".equals(eElement.getAttribute("name"))) {
						setProject_Name(eElement.getTextContent());
					} else if ("TestSuite_Name".equals(eElement.getAttribute("name"))) {
						setTestSuite_Name(eElement.getTextContent());
					} else if ("Release_Name".equals(eElement.getAttribute("name"))) {
						setRelease_Name(eElement.getTextContent());
					} else if ("Base_URL".equals(eElement.getAttribute("name"))) {
						setBase_URL(eElement.getTextContent());
					} else if ("UUID".equals(eElement.getAttribute("name"))) {
						setUUID(eElement.getTextContent());
					}
				}
			}

			break;
		}
	}

	public String getTestSuite_Name() {
		return TestSuite_Name;
	}

	public void setTestSuite_Name(String testSuite_Name) {
		TestSuite_Name = testSuite_Name;
	}

	public String getRelease_Name() {
		return Release_Name;
	}

	public void setRelease_Name(String release_Name) {
		Release_Name = release_Name;
	}

	public String getProject_Name() {
		return Project_Name;
	}

	public void setProject_Name(String project_Name) {
		Project_Name = project_Name;
	}

	public String getBase_URL() {
		return Base_URL;
	}

	public void setBase_URL(String base_URL) {
		Base_URL = base_URL;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uUID) {
		UUID = uUID;
	}

	public static String getConfigBaseDir() {
		return CONFIG_BASE_DIR;
	}
}
