package tw.com.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtil {
	private static final Logger logger = LogManager.getLogger(XMLUtil.class);

	private static String getTypeConvertVal(String type, String value) {
		Object object = null;
		java.util.Date utilDate = null;
		java.sql.Date sqlDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

		switch (type) {
		case "String":
			object = String.valueOf(value);
			break;
		case "BigDecimal":
			object = new BigDecimal(value);
			break;
		case "boolean":
			object = Boolean.valueOf(value);
			break;
		case "byte":
			object = Byte.valueOf(value);
			break;
		case "short":
			object = Short.valueOf(value);
			break;
		case "int":
			object = Integer.valueOf(value);
			break;
		case "long":
			object = Long.valueOf(value);
			break;
		case "float":
			object = Float.valueOf(value);
			break;
		case "double":
			object = Double.valueOf(value);

			break;
		case "byte[]":
			byte[] b = value.getBytes(StandardCharsets.UTF_8);
			object = b;
			break;
		case "Date":

			try {
				utilDate = sdf.parse(value);
				sqlDate = new java.sql.Date(utilDate.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			object = sqlDate;
			break;
		case "Time":
			java.sql.Time time = null;
			try {
				utilDate = sdf.parse(value);
				time = new java.sql.Time(utilDate.getTime());
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			object = time;
			break;
		case "Timestamp":
			Timestamp timestamp = null;
			try {
				utilDate = sdf.parse(value);
				timestamp = new java.sql.Timestamp(utilDate.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			object = timestamp;
			break;
		}
		return value = object.toString();
	}

	public static Document getDocument(String path) {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}
		File file = new File(path);
		Document document = null;
		try {
			document = dombuilder.parse(file);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		return document;
	}

	/*
	 * 提供XML字串得到Document物件
	 * */
	public static Document getDocumentForXml(String xml) throws Exception {

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}

		InputSource is = null;

		try {
			is = new InputSource(new StringReader(xml));
		} catch (Exception e) {
		}
		Document doc = null;
		try {
			doc = dombuilder.parse(is);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		return doc;
	}
	
	public static void delete(String configPath, String convertPath) {

		// 得到自定義的設定物件
		Document configDoc = getDocument(configPath);
		Element configRoot = configDoc.getDocumentElement();

		// 得到要進行轉換的物件
		Document convertDoc = getDocument(convertPath);

		// 從config XML中撈取刪除相關資訊
		NodeList delete = configRoot.getElementsByTagName("Delete");

		// 拿到要進行轉換的XML所有節點
		NodeList root = convertDoc.getElementsByTagName("*");

		NodeList insertList = null, table = null;
		Map<String, String> map = null;
		StringBuffer sBuffer = new StringBuffer();
		String tableName = null, sqlStr = null;

		for (int i = 0; i < delete.getLength(); i++) {
			Node delete_node = (Node) delete.item(i);
			if ("Delete".equals(delete_node.getNodeName())) {
				insertList = delete_node.getChildNodes();
				for (int j = 0; j < insertList.getLength(); j++) {
					Node insert_node = (Node) insertList.item(j);
					if ("Table".equals(insert_node.getNodeName())) {
						NamedNodeMap namedNodeMap = insert_node.getAttributes();

						table = insert_node.getChildNodes();

						for (int k = 0; k < namedNodeMap.getLength(); k++) {
							Node attr = namedNodeMap.item(k);
							String attrName = attr.getNodeName();
							String attrVal = attr.getNodeValue();
							if ("name".equals(attrName)) {
								tableName = attrVal;
							}
						}
						for (int l = 0; l < table.getLength(); l++) {
							Node field = (Node) table.item(l);
							if (field.getNodeType() == Node.ELEMENT_NODE) {
								map = new HashMap<String, String>();
							}
							NodeList fieldInfo = field.getChildNodes();
							for (int m = 0; m < fieldInfo.getLength(); m++) {
								Node node = (Node) fieldInfo.item(m);
								if (node.getNodeType() == Node.ELEMENT_NODE) {
									String nodeName = node.getNodeName();
									String value = node.getTextContent();
									if ("Source".equals(nodeName)) {
										map.put("source", value);
									}
									if ("Destination".equals(nodeName)) {
										map.put("destination", value);
									}
									if ("Type".equals(nodeName)) {
										map.put("type", value);
									}
								}
							}
						}

						for (int n = 0; n < root.getLength(); n++) {

							Element element = (Element) root.item(n);
							if (element.getNodeType() == Node.ELEMENT_NODE) {
								String nodeName = element.getNodeName();

								if (nodeName.equals(map.get("source"))) {

									String destination = map.get("destination");
									String value = element.getTextContent();
									String type = map.get("type");

									type = Util.getFieldType(type);
									value = getTypeConvertVal(type, value);

									sBuffer.append("DELETE FROM ").append(tableName).append(" WHERE ")
											.append(destination).append(" = \"").append(value).append("\"; ");
									sqlStr = sBuffer.toString();
									logger.debug(sqlStr);

									sqlExec(configDoc, sqlStr);
									sBuffer.setLength(0);
								}
							}

						}

					}
				}
			}
		}
	}

	private static void sqlExec(Document configDoc, String sqlStr) {

		Element configRoot = configDoc.getDocumentElement();

		// 從config XML中撈取資料庫連線所需資訊
		NodeList databaseConnectionFactory = configRoot.getElementsByTagName("databaseConnectionFactory");
		NodeList databaseInfo = databaseConnectionFactory.item(0).getChildNodes();

		String jdbcDriver = null;
		String dbURL = null;
		String dbUserName = null;
		String dbPassword = null;

		for (int i = 0; i < databaseInfo.getLength(); i++) {
			Node node = (Node) databaseInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = node.getNodeName();
				String value = node.getTextContent();

				jdbcDriver = nodeName.equals("jdbcDriver") ? value : jdbcDriver;
				dbURL = nodeName.equals("dbURL") ? value : dbURL;
				dbUserName = nodeName.equals("dbUserName") ? value : dbUserName;
				dbPassword = nodeName.equals("dbPassword") ? value : dbPassword;
			}
		}
		logger.debug("jdbcDriver: {} \\ dbURL: {} \\ dbUserName: {} \\ dbPassword: {}", jdbcDriver, dbURL, dbUserName,
				dbPassword);

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			Class.forName(jdbcDriver);
			con = DriverManager.getConnection(dbURL, dbUserName, dbPassword);

			pstmt = con.prepareStatement(sqlStr);

			pstmt.execute();

		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("A database error occured. " + cnfe.getMessage());
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (SQLException se) {
				logger.error("SQLException:".concat(se.getMessage()));
			} catch (Exception e) {
				logger.error("Exception:".concat(e.getMessage()));
			}
		}
	}

	public static void insert(String configPath, String xml) throws Exception {

		// 得到自定義的設定物件
		Document configDoc = getDocument(configPath);
		Element configRoot = configDoc.getDocumentElement();

		// 得到要進行轉換的物件
		Document convertDoc = getDocumentForXml(xml);

		// 從config XML中撈取新增相關資訊
		NodeList insert = configRoot.getElementsByTagName("Insert");

		// 從config XML中撈取新增相關資訊
		String tableName = null, sqlStr = null;

		// 拿到要進行轉換的XML所有節點
		NodeList root = convertDoc.getElementsByTagName("*");

		NodeList insertList = null, table = null;
		Map<String, String> map = null;
		StringBuffer sBuffer = new StringBuffer();

		for (int i = 0; i < insert.getLength(); i++) {
			Node insert_all_node = (Node) insert.item(i);
			if ("Insert".equals(insert_all_node.getNodeName())) {

				insertList = insert_all_node.getChildNodes();

				for (int j = 0; j < insertList.getLength(); j++) {
					Node insert_node = (Node) insertList.item(j);
					if ("Table".equals(insert_node.getNodeName())) {

						List<Map<String, String>> configList = new ArrayList<>();

						NamedNodeMap namedNodeMap = insert_node.getAttributes();

						table = insert_node.getChildNodes();

						for (int k = 0; k < namedNodeMap.getLength(); k++) {
							Node attr = namedNodeMap.item(k);
							String attrName = attr.getNodeName();
							String attrVal = attr.getNodeValue();
							if ("name".equals(attrName)) {
								tableName = attrVal;
							}
						}

						sBuffer.append("INSERT INTO ").append(tableName).append("(");

						// 從Table設定中撈取各欄位資訊，並轉成Map型態，儲存進List中
						for (int l = 0; l < table.getLength(); l++) {
							Node field = (Node) table.item(l);
							if (field.getNodeType() == Node.ELEMENT_NODE) {
								map = new HashMap<String, String>();
								NodeList fieldInfo = field.getChildNodes();
								for (int m = 0; m < fieldInfo.getLength(); m++) {
									Node node = (Node) fieldInfo.item(m);
									if (node.getNodeType() == Node.ELEMENT_NODE) {
										String nodeName = node.getNodeName();
										String value = node.getTextContent();
										if ("Source".equals(nodeName)) {
											map.put("source", value);
										}
										if ("Destination".equals(nodeName)) {
											map.put("destination", value);
										}
										if ("Type".equals(nodeName)) {
											map.put("type", value);
										}
									}
								}
								configList.add(map);
							}
						}

						for (int o = 0; o < configList.size(); o++) {

							Map<String, String> configMap = configList.get(o);
							for (int n = 0; n < root.getLength(); n++) {

								Element element = (Element) root.item(n);
								if (element.getNodeType() == Node.ELEMENT_NODE) {
									String nodeName = element.getNodeName();
									if (nodeName.equals(configMap.get("source"))) {
										String destination = configMap.get("destination");
										sBuffer.append(destination).append(",");
									}
								}

							}

							if (o == (configList.size() - 1)) {

								sBuffer.setLength(sBuffer.length() - 1);
								sBuffer.append(") VALUES (");
							}
						}

						for (int o = 0; o < configList.size(); o++) {
							Map<String, String> configMap = configList.get(o);

							for (int n = 0; n < root.getLength(); n++) {

								Element element = (Element) root.item(n);
								if (element.getNodeType() == Node.ELEMENT_NODE) {
									String nodeName = element.getNodeName();

									if (nodeName.equals(configMap.get("source"))) {
										String value = element.getTextContent();
										String type = configMap.get("type");

										type = Util.getFieldType(type);
										value = getTypeConvertVal(type, value);

										sBuffer.append("\"").append(value).append("\"").append(",");
									}

								}

							}
							if (o == (configList.size() - 1)) {

								sBuffer.setLength(sBuffer.length() - 1);
								sBuffer.append(");");
							}
						}

						sqlStr = sBuffer.toString();
						logger.debug(sqlStr);
						sqlExec(configDoc, sqlStr);
						sBuffer.setLength(0);
					}
				}
			}
		}
	}

	public static void update(String configPath, String convertPath) {

		// 得到自定義的設定物件
		Document configDoc = getDocument(configPath);
		Element configRoot = configDoc.getDocumentElement();

		// 得到要進行轉換的物件
		Document convertDoc = getDocument(convertPath);

		// 從config XML中撈取修改相關資訊
		NodeList insert = configRoot.getElementsByTagName("Update");

		// 從config XML中撈取新增相關資訊
		String tableName = null, sqlStr = null;

		// 拿到要進行轉換的XML所有節點
		NodeList root = convertDoc.getElementsByTagName("*");

		NodeList updateList = null, table = null;
		Map<String, String> map = null;
		StringBuffer sBuffer = new StringBuffer();

		for (int i = 0; i < insert.getLength(); i++) {
			Node insert_all_node = (Node) insert.item(i);
			if ("Update".equals(insert_all_node.getNodeName())) {

				updateList = insert_all_node.getChildNodes();

				for (int j = 0; j < updateList.getLength(); j++) {
					Node insert_node = (Node) updateList.item(j);
					if ("Table".equals(insert_node.getNodeName())) {

						List<Map<String, String>> configList = new ArrayList<>();
						List<Map<String, String>> conditionList = new ArrayList<>();

						NamedNodeMap namedNodeMap = insert_node.getAttributes();

						table = insert_node.getChildNodes();

						for (int k = 0; k < namedNodeMap.getLength(); k++) {
							Node attr = namedNodeMap.item(k);
							String attrName = attr.getNodeName();
							String attrVal = attr.getNodeValue();
							if ("name".equals(attrName)) {
								tableName = attrVal;
							}
						}

						sBuffer.append("UPDATE ").append(tableName).append(" SET ");

						// 從Table設定中撈取各欄位資訊，並轉成Map型態，儲存進List中
						for (int l = 0; l < table.getLength(); l++) {
							Node item = (Node) table.item(l);
							String itemName = item.getNodeName();
							if ("Field".equals(itemName)) {

								map = new HashMap<String, String>();
								NodeList fieldInfo = item.getChildNodes();
								for (int m = 0; m < fieldInfo.getLength(); m++) {
									Node node = (Node) fieldInfo.item(m);
									if (node.getNodeType() == Node.ELEMENT_NODE) {
										String nodeName = node.getNodeName();
										String value = node.getTextContent();
										if ("Source".equals(nodeName)) {
											map.put("source", value);
										}
										if ("Destination".equals(nodeName)) {
											map.put("destination", value);
										}
										if ("Type".equals(nodeName)) {
											map.put("type", value);
										}
									}
								}
								configList.add(map);
							}
							if ("Condition".equals(itemName)) {

								map = new HashMap<String, String>();
								NodeList conditionInfo = item.getChildNodes();
								for (int m = 0; m < conditionInfo.getLength(); m++) {
									Node node = (Node) conditionInfo.item(m);
									if (node.getNodeType() == Node.ELEMENT_NODE) {
										String nodeName = node.getNodeName();
										String value = node.getTextContent();
										if ("Source".equals(nodeName)) {
											map.put("source", value);
										}
										if ("Destination".equals(nodeName)) {
											map.put("destination", value);
										}
										if ("Type".equals(nodeName)) {
											map.put("type", value);
										}
										if ("Relation".equals(nodeName)) {
											map.put("relation", value);
										}
									}
								}
								conditionList.add(map);
							}
						}

						for (int o = 0; o < configList.size(); o++) {

							Map<String, String> configMap = configList.get(o);
							for (int n = 0; n < root.getLength(); n++) {

								Element element = (Element) root.item(n);
								if (element.getNodeType() == Node.ELEMENT_NODE) {
									String nodeName = element.getNodeName();
									if (nodeName.equals(configMap.get("source"))) {
										String destination = configMap.get("destination");
										String value = element.getTextContent();
										String type = configMap.get("type");

										type = Util.getFieldType(type);
										value = getTypeConvertVal(type, value);

										sBuffer.append(destination).append("= \"").append(value).append("\", ");
									}
								}

							}
							if (o == (configList.size() - 1)) {

								sBuffer.setLength(sBuffer.length() - 2);
								sBuffer.append(" WHERE ");
							}
						}
						System.out.println("conditionList: " + conditionList);

						for (int o = 0; o < conditionList.size(); o++) {

							Map<String, String> conditionMap = conditionList.get(o);
							for (int n = 0; n < root.getLength(); n++) {

								Element element = (Element) root.item(n);
								if (element.getNodeType() == Node.ELEMENT_NODE) {
									String nodeName = element.getNodeName();
									if (nodeName.equals(conditionMap.get("source"))) {
										String relation= conditionMap.get("relation");
										String destination = conditionMap.get("destination");
										String value = element.getTextContent();
										String type = conditionMap.get("type");

										type = Util.getFieldType(type);
										value = getTypeConvertVal(type, value);

										sBuffer.append(destination).append(relation).append(" \"").append(value).append("\";");
									}
								}

							}
							if (o == (configList.size() - 1)) {

								sBuffer.setLength(sBuffer.length() - 2);
								sBuffer.append(" WHERE");
							}
						}

						sqlStr = sBuffer.toString();
						logger.debug(sqlStr);
						 sqlExec(configDoc, sqlStr);
						sBuffer.setLength(0);
					}
				}
			}
		}
	}

	public static String getXml(String input) throws Exception {
		String xml = "";
		List<String> list = null;
		Map<String, String> map = null;
		List<Map<String, String>> converterConfigList = new ArrayList<>();
		
		if (Character.isJSONValid(input)) {
			JSONObject json = new JSONObject(input);
			xml = XML.toString(json);
			list = new ArrayList<String>();
		}
		if (Character.isXMLLike(input)) {
			xml = input;

 
			Document doc = getDocumentForXml(xml);
			
			Element root = doc.getDocumentElement();

			NodeList fields = root.getChildNodes();

			for (int i = 0; i < fields.getLength(); i++) {
				Node node = (Node) fields.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					map = new HashMap<String, String>();
					NodeList settings = node.getChildNodes();
					for (int j = 0; j < settings.getLength(); j++) {
						Node setting = (Node) settings.item(j);
						if (setting.getNodeType() == Node.ELEMENT_NODE) {
							String nodeName = setting.getNodeName();
							String textContent = setting.getTextContent();
							map.put(nodeName, textContent);
						}
					}
					converterConfigList.add(map);
				}
			}
			
//			for (int i = 0; i < fields.getLength(); i++) {
//				Node node = (Node) fields.item(i);
//				if (node.getNodeType() == Node.ELEMENT_NODE) {
//					map = new HashMap<String, String>();
//					NodeList settings = node.getChildNodes();
//					for (int j = 0; j < settings.getLength(); j++) {
//						Node setting = (Node) settings.item(j);
//						if (setting.getNodeType() == Node.ELEMENT_NODE) {
//							String nodeName = setting.getNodeName();
//							String textContent = setting.getTextContent();
//							map.put(nodeName, textContent);
//						}
//					}
//					converterConfigList.add(map);
//				}
//			}
		}
		
		for(int i =0;i<converterConfigList.size();i++){
			Map<String, String> map2 = converterConfigList.get(i);
			System.out.println(map2);
		}

		System.out.println("----------");
		if (!"".equals(xml)) {

			Document doc = getDocumentForXml(xml);

			NodeList all_nodeList = doc.getElementsByTagName("*");

			for (int i = 0; i < all_nodeList.getLength(); i++) {

				Element element = (Element) all_nodeList.item(i);
				String nodeName = element.getNodeName();

				if (Character.isJSONValid(input)) {
					if ("_".equals(nodeName.substring(0, 1))) {

						((Element) element.getParentNode()).setAttribute(nodeName.substring(1, nodeName.length()),
								element.getTextContent());

						list.add(nodeName);
					}
				}

				for (int j = 0; j < converterConfigList.size(); j++) {

					 map = converterConfigList.get(j);

					if (!"true".equals(map.get("isAttribute"))) {
						if (map.get("source").equals(nodeName)) {

							NodeList nodeList = doc.getElementsByTagName(nodeName);
							for (int k = 0; k < nodeList.getLength();) {
								doc.renameNode(nodeList.item(k), "", map.get("destination"));
							}
						}
					} else {
						NamedNodeMap namedNodeMap = element.getAttributes();
						for (int l = 0; l < namedNodeMap.getLength(); ++l) {
							Node attr = namedNodeMap.item(l);
							String attrName = attr.getNodeName();
							String attrVal = attr.getNodeValue();

							if (map.get("source").equals(attrName)) {
								element.removeAttribute(attrName);
								element.setAttribute(map.get("destination"), attrVal);
							}
						}
					}
				}
			}
			if (Character.isJSONValid(input)) {
				Element root = doc.getDocumentElement();
				for (int i = 0; i < list.size(); i++) {

					root.getElementsByTagName(list.get(i)).item(0).getParentNode()
							.removeChild(root.getElementsByTagName(list.get(i)).item(0));
				}
			}
			xml = XMLConverter.docToString(doc);

		}
		return xml;
	}
}
