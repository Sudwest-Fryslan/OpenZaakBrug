/*
 * Copyright 2020-2021 The Open Zaakbrug Contributors
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package nl.haarlem.translations.zdstozgw.utils.xpath;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XpathDocument {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Document document;
	private XPath xPath;

	public XpathDocument(Document document) {
		this.document = document;
		createXpathWithNamespaces();
	}

	private void createXpathWithNamespaces() {
		this.xPath = XPathFactory.newInstance().newXPath();
		this.xPath.setNamespaceContext(new NamespaceContext() {
			@Override
			public Iterator getPrefixes(String arg0) {
				return null;
			}

			@Override
			public String getPrefix(String arg0) {
				return null;
			}

			@Override
			public String getNamespaceURI(String ns) {
				if ("stuf".equals(ns)) {
					return "http://www.egem.nl/StUF/StUF0301";
				}
				if ("zds".equals(ns)) {
					return "http://www.stufstandaarden.nl/koppelvlak/zds0120";
				}
				if ("zkn".equals(ns)) {
					return "http://www.egem.nl/StUF/sector/zkn/0310";
				}
				if ("bg".equals(ns)) {
					return "http://www.egem.nl/StUF/sector/bg/0310";
				}
				return null;
			}
		});
	}

	public void insertNode(String expression, Node node) {
		Node target = null;

		try {
			target = (Node) this.xPath.compile(expression).evaluate(this.document, XPathConstants.NODE);

			target.appendChild(node);

		} catch (XPathExpressionException e) {
			log.error(e.getMessage());
		}

	}

	public void setNodeValue(String expression, String value) {
		Node node = null;
		try {
			node = (Node) this.xPath.compile(expression).evaluate(this.document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			log.error(e.getMessage());
		}
		node.setTextContent(value);
	}

	public void setNodeEmpty(String expression) {
		Node node = null;
		try {
			node = (Node) this.xPath.compile(expression).evaluate(this.document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			log.error(e.getMessage());
		}
		((Element) node).setAttributeNS("http://www.egem.nl/StUF/StUF0301", "noValue", "geenWaarde");
		((Element) node).setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "nil", "true");
	}

	public String getAttributeValue(String nodeExpression, String nameSpace, String attributeName) {
		Node node = null;
		try {
			node = (Node) this.xPath.compile(nodeExpression).evaluate(this.document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			log.error(e.getMessage());
		}
		return ((Element) node).getAttributeNodeNS(nameSpace, attributeName).getTextContent();
	}

	public String getNodeValue(String expression) {
		String value = "";
		try {
			value = (String) this.xPath.compile(expression).evaluate(this.document, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			log.error(e.getMessage());
		}
		return value;
	}
}